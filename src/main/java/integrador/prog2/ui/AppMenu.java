package integrador.prog2.ui;

import java.util.Scanner;

public class AppMenu {
    private final Scanner scanner;
    private final CategoriaMenu categoriaMenu;
    private final ProductoMenu productoMenu;
    private final DireccionMenu direccionMenu;
    private final UsuarioMenu usuarioMenu;
    private final PedidoMenu pedidoMenu;

    public AppMenu(Scanner scanner, CategoriaMenu categoriaMenu, ProductoMenu productoMenu,
                   DireccionMenu direccionMenu, UsuarioMenu usuarioMenu, PedidoMenu pedidoMenu) {
        this.scanner = scanner;
        this.categoriaMenu = categoriaMenu;
        this.productoMenu = productoMenu;
        this.direccionMenu = direccionMenu;
        this.usuarioMenu = usuarioMenu;
        this.pedidoMenu = pedidoMenu;
    }

    public void iniciar() {
        int opcion;
        do {
            System.out.println("\n========================================");
            System.out.println("   SISTEMA DE PEDIDOS - FOOD STORE");
            System.out.println("========================================");
            System.out.println("1. Categorías");
            System.out.println("2. Productos");
            System.out.println("3. Direcciones");
            System.out.println("4. Usuarios");
            System.out.println("5. Pedidos");
            System.out.println("0. Salir");
            opcion = EntradaUtil.leerEntero(scanner, "Seleccione: ");

            switch (opcion) {
                case 1 -> categoriaMenu.mostrar();
                case 2 -> productoMenu.mostrar();
                case 3 -> direccionMenu.mostrar();
                case 4 -> usuarioMenu.mostrar();
                case 5 -> pedidoMenu.mostrar();
                case 0 -> System.out.println("Programa finalizado.");
                default -> System.out.println("Opción fuera de rango.");
            }
        } while (opcion != 0);
    }
}
