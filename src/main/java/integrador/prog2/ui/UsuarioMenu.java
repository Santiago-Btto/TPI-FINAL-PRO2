package integrador.prog2.ui;

import integrador.prog2.entities.Direccion;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Rol;
import integrador.prog2.service.DireccionService;
import integrador.prog2.service.UsuarioService;

import java.util.List;
import java.util.Scanner;

public class UsuarioMenu {
    private final UsuarioService service;
    private final DireccionService direccionService;
    private final Scanner scanner;

    public UsuarioMenu(UsuarioService service, DireccionService direccionService, Scanner scanner) {
        this.service = service;
        this.direccionService = direccionService;
        this.scanner = scanner;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n=== USUARIOS ===");
            System.out.println("1. Listar");
            System.out.println("2. Crear");
            System.out.println("3. Editar");
            System.out.println("4. Eliminar");
            System.out.println("5. Buscar por mail");
            System.out.println("0. Volver");
            opcion = EntradaUtil.leerEntero(scanner, "Seleccione: ");
            try {
                switch (opcion) {
                    case 1 -> listar();
                    case 2 -> crear();
                    case 3 -> editar();
                    case 4 -> eliminar();
                    case 5 -> buscarPorMail();
                    case 0 -> { }
                    default -> System.out.println("Opción fuera de rango.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    public void listar() {
        List<Usuario> usuarios = service.listarActivos();
        if (usuarios.isEmpty()) System.out.println("No hay usuarios cargados.");
        else usuarios.forEach(System.out::println);
    }

    private void crear() {
        Direccion direccion = seleccionarDireccion();
        Usuario usuario = service.crear(
                EntradaUtil.leerObligatorio(scanner, "Nombre: "),
                EntradaUtil.leerObligatorio(scanner, "Apellido: "),
                EntradaUtil.leerObligatorio(scanner, "Mail: "),
                EntradaUtil.leerOpcional(scanner, "Celular: "),
                EntradaUtil.leerObligatorio(scanner, "Contraseña: "),
                seleccionarRol(),
                direccion);
        System.out.println("Usuario creado con ID " + usuario.getId());
    }

    private void editar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID a editar: ");
        String nombre = EntradaUtil.leerOpcional(scanner, "Nombre (Enter mantiene): ");
        String apellido = EntradaUtil.leerOpcional(scanner, "Apellido (Enter mantiene): ");
        String mail = EntradaUtil.leerOpcional(scanner, "Mail (Enter mantiene): ");
        String celular = EntradaUtil.leerOpcional(scanner, "Celular (Enter mantiene): ");
        String contrasena = EntradaUtil.leerOpcional(scanner, "Contraseña (Enter mantiene): ");
        String rolTexto = EntradaUtil.leerOpcional(scanner, "Rol 1=ADMIN, 2=USUARIO (Enter mantiene): ");
        String direccionTexto = EntradaUtil.leerOpcional(scanner, "ID dirección (Enter mantiene): ");

        Rol rol = rolTexto == null ? null : (rolTexto.equals("1") ? Rol.ADMIN : Rol.USUARIO);
        Direccion direccion = direccionTexto == null ? null : direccionService.buscarPorId(Long.parseLong(direccionTexto));
        service.editar(id, nombre, apellido, mail, celular, contrasena, rol, direccion);
        System.out.println("Usuario actualizado.");
    }

    private void eliminar() {
        listar();
        Long id = EntradaUtil.leerLong(scanner, "ID a eliminar: ");
        if (EntradaUtil.confirmar(scanner, "¿Confirma la baja lógica?")) {
            service.eliminar(id);
            System.out.println("Usuario eliminado lógicamente. Sus pedidos históricos permanecen.");
        }
    }

    private void buscarPorMail() {
        System.out.println(service.buscarPorMail(EntradaUtil.leerObligatorio(scanner, "Mail: ")));
    }

    private Direccion seleccionarDireccion() {
        List<Direccion> direcciones = direccionService.listarActivos();
        if (direcciones.isEmpty()) {
            throw new IllegalStateException("Primero debe crear una dirección en el menú Direcciones.");
        }
        direcciones.forEach(System.out::println);
        return direccionService.buscarPorId(EntradaUtil.leerLong(scanner, "ID de dirección: "));
    }

    private Rol seleccionarRol() {
        int opcion = EntradaUtil.leerEntero(scanner, "Rol 1 = ADMIN, 2 = USUARIO: ");
        return opcion == 1 ? Rol.ADMIN : Rol.USUARIO;
    }
}
