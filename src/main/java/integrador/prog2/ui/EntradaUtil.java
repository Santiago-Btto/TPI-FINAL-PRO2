package integrador.prog2.ui;

import java.util.Scanner;

public final class EntradaUtil {
    private EntradaUtil() {
    }

    public static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Ingrese un número entero.");
            }
        }
    }

    public static Long leerLong(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("ID inválido.");
            }
        }
    }

    public static Double leerDouble(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Número decimal inválido.");
            }
        }
    }

    public static String leerObligatorio(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = scanner.nextLine().trim();
            if (!valor.isBlank()) return valor;
            System.out.println("El valor no puede estar vacío.");
        }
    }

    public static String leerOpcional(Scanner scanner, String mensaje) {
        System.out.print(mensaje);
        String valor = scanner.nextLine().trim();
        return valor.isBlank() ? null : valor;
    }

    public static boolean confirmar(Scanner scanner, String mensaje) {
        System.out.print(mensaje + " (S/N): ");
        return scanner.nextLine().trim().equalsIgnoreCase("S");
    }
}
