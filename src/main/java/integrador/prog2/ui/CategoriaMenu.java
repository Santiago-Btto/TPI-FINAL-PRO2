package integrador.prog2.ui;

import integrador.prog2.entities.Categoria;
import integrador.prog2.service.CategoriaService;

import java.util.List;
import java.util.Scanner;

public class CategoriaMenu {
    private final CategoriaService service;
    private final Scanner scanner;

    public CategoriaMenu(CategoriaService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n=== CATEGORÍAS ===");
            System.out.println("1. Listar");
            System.out.println("2. Crear");
            System.out.println("3. Editar");
            System.out.println("4. Eliminar");
            System.out.println("5. Buscar por nombre");
            System.out.println("0. Volver");
            opcion = EntradaUtil.leerEntero(scanner, "Seleccione: ");
            try {
                switch (opcion) {
                    case 1 -> listar();
                    case 2 -> crear();
                    case 3 -> editar();
                    case 4 -> eliminar();
                    case 5 -> buscarPorNombre();
                    case 0 -> { }
                    default -> System.out.println("Opción fuera de rango.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    public void listar() {
        List<Categoria> categorias = service.listarActivos();
        if (categorias.isEmpty()) System.out.println("No hay categorías cargadas.");
        else categorias.forEach(System.out::println);
    }

    private void crear() {
        String nombre = EntradaUtil.leerObligatorio(scanner, "Nombre: ");
        String descripcion = EntradaUtil.leerObligatorio(scanner, "Descripción: ");
        Categoria categoria = service.crear(nombre, descripcion);
        System.out.println("Categoría creada con ID " + categoria.getId());
    }

    private void editar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID a editar: ");
        String nombre = EntradaUtil.leerOpcional(scanner, "Nuevo nombre (Enter mantiene): ");
        String descripcion = EntradaUtil.leerOpcional(scanner, "Nueva descripción (Enter mantiene): ");
        service.editar(id, nombre, descripcion);
        System.out.println("Categoría actualizada.");
    }

    private void eliminar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID a eliminar: ");
        if (EntradaUtil.confirmar(scanner, "¿Confirma la baja lógica?")) {
            service.eliminar(id);
            System.out.println("Categoría eliminada lógicamente.");
        }
    }

    private void buscarPorNombre() {
        String nombre = EntradaUtil.leerObligatorio(scanner, "Nombre exacto: ");
        System.out.println(service.buscarPorNombre(nombre));
    }
}
