package integrador.prog2;

import integrador.prog2.service.CategoriaService;
import integrador.prog2.service.DireccionService;
import integrador.prog2.service.PedidoService;
import integrador.prog2.service.ProductoService;
import integrador.prog2.service.UsuarioService;
import integrador.prog2.ui.AppMenu;
import integrador.prog2.ui.CategoriaMenu;
import integrador.prog2.ui.DireccionMenu;
import integrador.prog2.ui.PedidoMenu;
import integrador.prog2.ui.ProductoMenu;
import integrador.prog2.ui.UsuarioMenu;

import java.util.Scanner;

public class Main { // probar aca
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        CategoriaService categoriaService = new CategoriaService();
        ProductoService productoService = new ProductoService();
        DireccionService direccionService = new DireccionService();
        UsuarioService usuarioService = new UsuarioService();
        PedidoService pedidoService = new PedidoService();

        AppMenu appMenu = new AppMenu(
                scanner,
                new CategoriaMenu(categoriaService, scanner),
                new ProductoMenu(productoService, categoriaService, scanner),
                new DireccionMenu(direccionService, scanner),
                new UsuarioMenu(usuarioService, direccionService, scanner),
                new PedidoMenu(pedidoService, usuarioService, productoService, scanner));

        try {
            appMenu.iniciar();
        } finally {
            scanner.close();
        }
    }
}
