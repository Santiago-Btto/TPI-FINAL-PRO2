package integrador.prog2.ui;

import integrador.prog2.entities.Categoria;
import integrador.prog2.entities.Producto;
import integrador.prog2.service.CategoriaService;
import integrador.prog2.service.ProductoService;

import java.util.List;
import java.util.Scanner;

public class ProductoMenu {
    private final ProductoService service;
    private final CategoriaService categoriaService;
    private final Scanner scanner;

    public ProductoMenu(ProductoService service, CategoriaService categoriaService, Scanner scanner) {
        this.service = service;
        this.categoriaService = categoriaService;
        this.scanner = scanner;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n=== PRODUCTOS ===");
            System.out.println("1. Listar");
            System.out.println("2. Crear");
            System.out.println("3. Editar");
            System.out.println("4. Eliminar");
            System.out.println("5. Listar por categoría");
            System.out.println("6. Buscar por nombre");
            System.out.println("0. Volver");
            opcion = EntradaUtil.leerEntero(scanner, "Seleccione: ");
            try {
                switch (opcion) {
                    case 1 -> listar();
                    case 2 -> crear();
                    case 3 -> editar();
                    case 4 -> eliminar();
                    case 5 -> listarPorCategoria();
                    case 6 -> buscarPorNombre();
                    case 0 -> { }
                    default -> System.out.println("Opción fuera de rango.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    public void listar() {
        List<Producto> productos = service.listarActivos();
        if (productos.isEmpty()) System.out.println("No hay productos cargados.");
        else productos.forEach(System.out::println);
    }

    private void crear() {
        String nombre = EntradaUtil.leerObligatorio(scanner, "Nombre: ");
        String descripcion = EntradaUtil.leerObligatorio(scanner, "Descripción: ");
        Double precio = EntradaUtil.leerDouble(scanner, "Precio: ");
        int stock = EntradaUtil.leerEntero(scanner, "Stock: ");
        String imagen = EntradaUtil.leerOpcional(scanner, "Imagen/URL: ");
        boolean disponible = EntradaUtil.confirmar(scanner, "¿Disponible?");
        Categoria categoria = seleccionarCategoria();

        Producto producto = service.crear(nombre, precio, descripcion, stock, imagen, disponible, categoria);
        System.out.println("Producto creado con ID " + producto.getId());
    }

    private void editar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID a editar: ");
        String nombre = EntradaUtil.leerOpcional(scanner, "Nombre (Enter mantiene): ");
        String descripcion = EntradaUtil.leerOpcional(scanner, "Descripción (Enter mantiene): ");
        String precioTexto = EntradaUtil.leerOpcional(scanner, "Precio (Enter mantiene): ");
        String stockTexto = EntradaUtil.leerOpcional(scanner, "Stock (Enter mantiene): ");
        String imagen = EntradaUtil.leerOpcional(scanner, "Imagen (Enter mantiene): ");
        String disponibleTexto = EntradaUtil.leerOpcional(scanner, "Disponible S/N (Enter mantiene): ");
        String categoriaTexto = EntradaUtil.leerOpcional(scanner, "ID categoría (Enter mantiene): ");

        Double precio = precioTexto == null ? null : Double.parseDouble(precioTexto.replace(',', '.'));
        Integer stock = stockTexto == null ? null : Integer.parseInt(stockTexto);
        Boolean disponible = disponibleTexto == null ? null : disponibleTexto.equalsIgnoreCase("S");
        Categoria categoria = categoriaTexto == null ? null : categoriaService.buscarPorId(Long.parseLong(categoriaTexto));

        service.editar(id, nombre, precio, descripcion, stock, imagen, disponible, categoria);
        System.out.println("Producto actualizado.");
    }

    private void eliminar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID a eliminar: ");
        if (EntradaUtil.confirmar(scanner, "¿Confirma la baja lógica?")) {
            service.eliminar(id);
            System.out.println("Producto eliminado lógicamente.");
        }
    }

    private void listarPorCategoria() {
        Categoria categoria = seleccionarCategoria();
        List<Producto> productos = service.listarPorCategoria(categoria);
        if (productos.isEmpty()) System.out.println("La categoría no tiene productos activos.");
        else productos.forEach(System.out::println);
    }

    private void buscarPorNombre() {
        String texto = EntradaUtil.leerObligatorio(scanner, "Texto a buscar: ");
        List<Producto> productos = service.buscarPorNombre(texto);
        if (productos.isEmpty()) System.out.println("No se encontraron productos.");
        else productos.forEach(System.out::println);
    }

    private Categoria seleccionarCategoria() {
        List<Categoria> categorias = categoriaService.listarActivos();
        if (categorias.isEmpty()) throw new IllegalStateException("Primero debe crear una categoría.");
        categorias.forEach(System.out::println);
        return categoriaService.buscarPorId(EntradaUtil.leerLong(scanner, "ID de categoría: "));
    }
}
