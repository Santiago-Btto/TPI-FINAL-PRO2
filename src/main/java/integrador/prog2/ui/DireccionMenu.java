package integrador.prog2.ui;

import integrador.prog2.entities.Direccion;
import integrador.prog2.service.DireccionService;

import java.util.List;
import java.util.Scanner;

public class DireccionMenu {
    private final DireccionService service;
    private final Scanner scanner;

    public DireccionMenu(DireccionService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n=== DIRECCIONES (1-1 con usuario) ===");
            System.out.println("1. Listar");
            System.out.println("2. Crear");
            System.out.println("3. Editar");
            System.out.println("4. Eliminar");
            System.out.println("0. Volver");
            opcion = EntradaUtil.leerEntero(scanner, "Seleccione: ");
            try {
                switch (opcion) {
                    case 1 -> listar();
                    case 2 -> crear();
                    case 3 -> editar();
                    case 4 -> eliminar();
                    case 0 -> { }
                    default -> System.out.println("Opción fuera de rango.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    public void listar() {
        List<Direccion> direcciones = service.listarActivos();
        if (direcciones.isEmpty()) System.out.println("No hay direcciones cargadas.");
        else direcciones.forEach(System.out::println);
    }

    private void crear() {
        Direccion direccion = service.crear(
                EntradaUtil.leerObligatorio(scanner, "Calle: "),
                EntradaUtil.leerObligatorio(scanner, "Número: "),
                EntradaUtil.leerObligatorio(scanner, "Ciudad: "),
                EntradaUtil.leerObligatorio(scanner, "Código postal: "));
        System.out.println("Dirección creada con ID " + direccion.getId());
    }

    private void editar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID a editar: ");
        service.editar(id,
                EntradaUtil.leerOpcional(scanner, "Calle (Enter mantiene): "),
                EntradaUtil.leerOpcional(scanner, "Número (Enter mantiene): "),
                EntradaUtil.leerOpcional(scanner, "Ciudad (Enter mantiene): "),
                EntradaUtil.leerOpcional(scanner, "Código postal (Enter mantiene): "));
        System.out.println("Dirección actualizada.");
    }

    private void eliminar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID a eliminar: ");
        if (EntradaUtil.confirmar(scanner, "¿Confirma la baja lógica?")) {
            service.eliminar(id);
            System.out.println("Dirección eliminada lógicamente.");
        }
    }
}
