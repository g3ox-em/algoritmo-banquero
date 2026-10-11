import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class icedcave {

    static final Scanner TECLADO = new Scanner(System.in);

    static int n = 0;
    static int m = 0;
    static int[] disponible;
    static int[][] maximo;
    static int[][] asignado;
    static int[][] necesidad;

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Selecciona una opción: ", 1, 7);
            switch (opcion) {
                case 1:
                    cargarEjemploSeguro();
                    break;
                case 2:
                    cargarEjemploInterbloqueo();
                    break;
                case 3:
                    ingresarManual();
                    break;
                case 4:
                    if (hayDatos()) {
                        mostrarEstado();
                    }
                    break;
                case 5:
                    verificarEstado();
                    break;
                case 6:
                    solicitarRecursos();
                    break;
                default:
                    break;
            }
        } while (opcion != 7);
        System.out.println("\nHasta pronto.");
    }

    static void mostrarMenu() {
        System.out.println();
        System.out.println(linea(52, '='));
        System.out.println("SIMULADOR: ALGORITMO DEL BANQUERO");
        System.out.println(linea(52, '='));
        System.out.println("1. Cargar ejemplo (estado seguro)");
        System.out.println("2. Cargar ejemplo (estado con interbloqueo)");
        System.out.println("3. Ingresar datos manualmente");
        System.out.println("4. Mostrar estado actual del sistema");
        System.out.println("5. Verificar estado seguro / detectar interbloqueo");
        System.out.println("6. Solicitar recursos para un proceso");
        System.out.println("7. Salir");
        System.out.println(linea(52, '-'));
    }

    static void cargarEjemploSeguro() {
        n = 5;
        m = 3;
        disponible = new int[] {3, 3, 2};
        asignado = new int[][] {{0, 1, 0}, {2, 0, 0}, {3, 0, 2}, {2, 1, 1}, {0, 0, 2}};
        maximo = new int[][] {{7, 5, 3}, {3, 2, 2}, {9, 0, 2}, {2, 2, 2}, {4, 3, 3}};
        calcularNecesidad();
        System.out.println("\nEjemplo cargado: 5 procesos y 3 tipos de recursos.");
        mostrarEstado();
    }

    static void cargarEjemploInterbloqueo() {
        n = 3;
        m = 2;
        disponible = new int[] {0, 0};
        asignado = new int[][] {{1, 0}, {0, 1}, {1, 1}};
        maximo = new int[][] {{1, 1}, {1, 1}, {2, 2}};
        calcularNecesidad();
        System.out.println("\nEjemplo cargado: 3 procesos y 2 tipos de recursos.");
        mostrarEstado();
    }

    static void ingresarManual() {
        n = leerEntero("Número de procesos (1-10): ", 1, 10);
        m = leerEntero("Número de tipos de recursos (1-10): ", 1, 10);
        System.out.println("Tipos de recursos: " + letras());
        disponible = leerVector("Recursos disponibles: ", m);
        asignado = new int[n][];
        maximo = new int[n][];
        for (int i = 0; i < n; i++) {
            asignado[i] = leerVector("P" + i + " - Asignado: ", m);
            while (true) {
                maximo[i] = leerVector("P" + i + " - Máximo:   ", m);
                if (menorIgual(asignado[i], maximo[i])) {
                    break;
                }
                System.out.println("  El máximo no puede ser menor que lo asignado. Intenta de nuevo.");
            }
        }
        calcularNecesidad();
        mostrarEstado();
    }

    static void calcularNecesidad() {
        necesidad = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                necesidad[i][j] = maximo[i][j] - asignado[i][j];
            }
        }
    }

    static List<Integer> buscarSecuenciaSegura(int[] disp, int[][] asig, int[][] nec, boolean detalle) {
        int[] work = disp.clone();
        boolean[] terminado = new boolean[n];
        List<Integer> secuencia = new ArrayList<Integer>();
        boolean avanzo = true;
        int paso = 1;

        if (detalle) {
            System.out.println("Work (inicial) = Disponible = " + vec(work));
        }
        while (avanzo) {
            avanzo = false;
            for (int i = 0; i < n; i++) {
                if (!terminado[i] && menorIgual(nec[i], work)) {
                    if (detalle) {
                        System.out.println("Paso " + paso + ": P" + i + " puede terminar  (Necesidad "
                                + vec(nec[i]) + " <= Work " + vec(work) + ")");
                    }
                    for (int j = 0; j < m; j++) {
                        work[j] += asig[i][j];
                    }
                    if (detalle) {
                        System.out.println("P" + i + " libera sus recursos -> Work = " + vec(work));
                    }
                    terminado[i] = true;
                    secuencia.add(i);
                    avanzo = true;
                    paso++;
                }
            }
        }

        if (detalle && secuencia.size() < n) {
            System.out.println("Procesos que NO pueden terminar:");
            for (int i = 0; i < n; i++) {
                if (!terminado[i]) {
                    System.out.println("  P" + i + ": Necesidad " + vec(nec[i]) + " > Work " + vec(work));
                }
            }
        }
        return secuencia;
    }

    static void verificarEstado() {
        if (!hayDatos()) {
            return;
        }
        System.out.println("\nVerificación de estado seguro / detección de interbloqueo");
        List<Integer> secuencia = buscarSecuenciaSegura(disponible, asignado, necesidad, true);
        System.out.println();
        if (secuencia.size() == n) {
            System.out.println("RESULTADO: el sistema está en ESTADO SEGURO.");
            System.out.println("Secuencia segura: " + formatearSecuencia(secuencia));
        } else {
            System.out.println("RESULTADO: ESTADO INSEGURO -> hay INTERBLOQUEO (o riesgo de él).");
            StringBuilder bloqueados = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if (!secuencia.contains(i)) {
                    bloqueados.append("P").append(i).append(" ");
                }
            }
            System.out.println("Procesos bloqueados: " + bloqueados.toString().trim());
        }
    }

    static void solicitarRecursos() {
        if (!hayDatos()) {
            return;
        }
        int i = leerEntero("Proceso que solicita (0-" + (n - 1) + "): ", 0, n - 1);
        System.out.println("Tipos de recursos: " + letras());
        int[] solicitud = leerVector("Solicitud de P" + i + ": ", m);
        System.out.println("\nP" + i + " solicita " + vec(solicitud));

        if (!menorIgual(solicitud, necesidad[i])) {
            System.out.println("DENEGADA: P" + i + " excede su necesidad máxima " + vec(necesidad[i]) + ".");
            return;
        }
        
        if (!menorIgual(solicitud, disponible)) {
            System.out.println("P" + i + " debe ESPERAR: no hay recursos suficientes (Disponible "
                    + vec(disponible) + ").");
            return;
        }
        
        int[] dispHip = disponible.clone();
        int[][] asigHip = copiar(asignado);
        int[][] necHip = copiar(necesidad);
        for (int j = 0; j < m; j++) {
            dispHip[j] -= solicitud[j];
            asigHip[i][j] += solicitud[j];
            necHip[i][j] -= solicitud[j];
        }
        System.out.println("Asignación hipotética realizada. Comprobando si el nuevo estado es seguro...\n");
        List<Integer> secuencia = buscarSecuenciaSegura(dispHip, asigHip, necHip, true);
        System.out.println();
        if (secuencia.size() == n) {
            disponible = dispHip;
            asignado = asigHip;
            necesidad = necHip;
            System.out.println("CONCEDIDA: el estado resultante es seguro.");
            System.out.println("Secuencia segura: " + formatearSecuencia(secuencia));
            mostrarEstado();
        } else {
            System.out.println("DENEGADA: conceder la solicitud dejaría al sistema en estado INSEGURO.");
            System.out.println("P" + i + " debe esperar y el estado anterior se conserva.");
        }
    }

    static void mostrarEstado() {
        int w = Math.max(2 * m + 1, 9) + 2;
        String formato = "%-9s| %-" + w + "s| %-" + w + "s| %-" + w + "s%n";
        System.out.println();
        System.out.println("Tipos de recursos: " + letras());
        System.out.println(linea(9 + 3 * (w + 2), '-'));
        System.out.printf(formato, "Proceso", "Asignado", "Máximo", "Necesidad");
        System.out.println(linea(9 + 3 * (w + 2), '-'));
        for (int i = 0; i < n; i++) {
            System.out.printf(formato, "P" + i, vec(asignado[i]), vec(maximo[i]), vec(necesidad[i]));
        }
        System.out.println(linea(9 + 3 * (w + 2), '-'));
        System.out.println("Disponible: " + vec(disponible));
    }

    static String formatearSecuencia(List<Integer> secuencia) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < secuencia.size(); k++) {
            if (k > 0) {
                sb.append(" -> ");
            }
            sb.append("P").append(secuencia.get(k));
        }
        return sb.toString();
    }

    static String vec(int[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int j = 0; j < v.length; j++) {
            if (j > 0) {
                sb.append(" ");
            }
            sb.append(v[j]);
        }
        return sb.append("]").toString();
    }

    static String letras() {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < m; j++) {
            if (j > 0) {
                sb.append(" ");
            }
            sb.append((char) ('A' + j));
        }
        return sb.toString();
    }

    static String linea(int largo, char c) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < largo; k++) {
            sb.append(c);
        }
        return sb.toString();
    }

    static boolean hayDatos() {
        if (n == 0) {
            System.out.println("\nPrimero carga o ingresa los datos (opciones 1, 2 o 3).");
            return false;
        }
        return true;
    }

    static boolean menorIgual(int[] a, int[] b) {
        for (int j = 0; j < a.length; j++) {
            if (a[j] > b[j]) {
                return false;
            }
        }
        return true;
    }

    static int[][] copiar(int[][] matriz) {
        int[][] copia = new int[matriz.length][];
        for (int i = 0; i < matriz.length; i++) {
            copia[i] = matriz[i].clone();
        }
        return copia;
    }

    static int leerEntero(String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            String texto = TECLADO.nextLine().trim();
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("  Ingresa un número entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Entrada no válida.");
            }
        }
    }

    static int[] leerVector(String mensaje, int tam) {
        while (true) {
            System.out.print(mensaje);
            String[] partes = TECLADO.nextLine().trim().split("\\s+");
            if (partes.length != tam) {
                System.out.println("  Ingresa exactamente " + tam + " números separados por espacios.");
                continue;
            }
            int[] v = new int[tam];
            try {
                for (int j = 0; j < tam; j++) {
                    v[j] = Integer.parseInt(partes[j]);
                    if (v[j] < 0) {
                        throw new NumberFormatException();
                    }
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  Solo se permiten enteros no negativos.");
            }
        }
    }
}