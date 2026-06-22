package integrador.prog2.ui;

import integrador.prog2.entities.Pedido;
import integrador.prog2.entities.Producto;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;
import integrador.prog2.service.PedidoService;
import integrador.prog2.service.ProductoService;
import integrador.prog2.service.UsuarioService;

import java.util.List;
import java.util.Scanner;

public class PedidoMenu {
    private final PedidoService service;
    private final UsuarioService usuarioService;
    private final ProductoService productoService;
    private final Scanner scanner;

    public PedidoMenu(PedidoService service, UsuarioService usuarioService,
                      ProductoService productoService, Scanner scanner) {
        this.service = service;
        this.usuarioService = usuarioService;
        this.productoService = productoService;
        this.scanner = scanner;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n=== PEDIDOS ===");
            System.out.println("1. Listar");
            System.out.println("2. Crear con detalles");
            System.out.println("3. Ver detalle");
            System.out.println("4. Actualizar estado y forma de pago");
            System.out.println("5. Eliminar");
            System.out.println("6. Listar por usuario");
            System.out.println("7. Demostrar rollback por stock insuficiente");
            System.out.println("0. Volver");
            opcion = EntradaUtil.leerEntero(scanner, "Seleccione: ");
            try {
                switch (opcion) {
                    case 1 -> listar();
                    case 2 -> crear();
                    case 3 -> verDetalle();
                    case 4 -> actualizar();
                    case 5 -> eliminar();
                    case 6 -> listarPorUsuario();
                    case 7 -> demostrarRollback();
                    case 0 -> { }
                    default -> System.out.println("Opción fuera de rango.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    public void listar() {
        List<Pedido> pedidos = service.listarActivos();
        if (pedidos.isEmpty()) System.out.println("No hay pedidos cargados.");
        else pedidos.forEach(System.out::println);
    }

    private void crear() {
        Usuario usuario = seleccionarUsuario();
        FormaPago formaPago = seleccionarFormaPago();
        Pedido pedido = service.iniciarPedido(usuario, formaPago);

        boolean seguir;
        do {
            Producto producto = seleccionarProducto();
            int cantidad = EntradaUtil.leerEntero(scanner, "Cantidad: ");
            service.agregarDetalle(pedido, cantidad, producto);
            System.out.printf("Detalle agregado. Total provisorio: $%.2f%n", pedido.getTotal());
            seguir = EntradaUtil.confirmar(scanner, "¿Agregar otro producto?");
        } while (seguir);

        service.confirmarPedido(pedido);
        System.out.println("Pedido creado con ID " + pedido.getId() + " y total $" + pedido.getTotal());
    }

    private void verDetalle() {
        listar();
        Pedido pedido = service.buscarPorId(EntradaUtil.leerLong(scanner, "ID del pedido: "));
        System.out.println(pedido);
        if (pedido.getDetalles().isEmpty()) {
            System.out.println("El pedido no tiene detalles activos.");
        } else {
            pedido.getDetalles().forEach(System.out::println);
        }
    }

    private void actualizar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID del pedido: ");
        Estado estado = seleccionarEstado();
        FormaPago formaPago = seleccionarFormaPago();
        service.actualizarEstadoYFormaPago(id, estado, formaPago);
        System.out.println("Pedido actualizado.");
    }

    private void eliminar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID a eliminar: ");
        if (EntradaUtil.confirmar(scanner, "¿Confirma la baja lógica del pedido y sus detalles?")) {
            service.eliminar(id);
            System.out.println("Pedido eliminado lógicamente.");
        }
    }

    private void listarPorUsuario() {
        Usuario usuario = seleccionarUsuario();
        List<Pedido> pedidos = service.listarPorUsuario(usuario);
        if (pedidos.isEmpty()) System.out.println("El usuario no tiene pedidos activos.");
        else pedidos.forEach(System.out::println);
    }

    private void demostrarRollback() {
        Usuario usuario = seleccionarUsuario();
        Producto producto = seleccionarProducto();
        int cantidadInvalida = producto.getStock() + 1;

        Pedido pedido = service.iniciarPedido(usuario, FormaPago.EFECTIVO);
        service.agregarDetalle(pedido, cantidadInvalida, producto);

        System.out.println("Se intentará guardar una cabecera y luego un detalle con cantidad " +
                cantidadInvalida + ", superior al stock " + producto.getStock() + ".");
        try {
            service.confirmarPedido(pedido);
            System.out.println("La prueba no generó el error esperado.");
        } catch (RuntimeException e) {
            System.out.println("ERROR ESPERADO: " + e.getMessage());
            System.out.println("ROLLBACK EJECUTADO: la cabecera, los detalles y el descuento de stock no se confirmaron.");
        }
    }

    private Usuario seleccionarUsuario() {
        List<Usuario> usuarios = usuarioService.listarActivos();
        if (usuarios.isEmpty()) throw new IllegalStateException("Primero debe crear un usuario.");
        usuarios.forEach(System.out::println);
        return usuarioService.buscarPorId(EntradaUtil.leerLong(scanner, "ID de usuario: "));
    }

    private Producto seleccionarProducto() {
        List<Producto> productos = productoService.listarActivos().stream()
                .filter(Producto::isDisponible)
                .toList();
        if (productos.isEmpty()) throw new IllegalStateException("No hay productos disponibles.");
        productos.forEach(System.out::println);
        return productoService.buscarPorId(EntradaUtil.leerLong(scanner, "ID de producto: "));
    }

    private Estado seleccionarEstado() {
        System.out.println("1 = PENDIENTE  2 = CONFIRMADO  3 = TERMINADO  4 = CANCELADO");
        return switch (EntradaUtil.leerEntero(scanner, "Estado: ")) {
            case 1 -> Estado.PENDIENTE;
            case 2 -> Estado.CONFIRMADO;
            case 3 -> Estado.TERMINADO;
            case 4 -> Estado.CANCELADO;
            default -> throw new IllegalArgumentException("Estado fuera de rango.");
        };
    }

    private FormaPago seleccionarFormaPago() {
        System.out.println("1 = TARJETA  2 = TRANSFERENCIA  3 = EFECTIVO");
        return switch (EntradaUtil.leerEntero(scanner, "Forma de pago: ")) {
            case 1 -> FormaPago.TARJETA;
            case 2 -> FormaPago.TRANSFERENCIA;
            case 3 -> FormaPago.EFECTIVO;
            default -> throw new IllegalArgumentException("Forma de pago fuera de rango.");
        };
    }
}
