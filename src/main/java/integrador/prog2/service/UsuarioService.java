package integrador.prog2.service;

import integrador.prog2.dao.UsuarioDAO;
import integrador.prog2.entities.Direccion;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Rol;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.exception.MailDuplicadoException;
import integrador.prog2.exception.PersistenciaException;

import java.sql.SQLException;
import java.util.List;

public class UsuarioService implements GenericService<Usuario> {
    private final UsuarioDAO dao = new UsuarioDAO();

    @Override
    public List<Usuario> listarActivos() {
        try {
            return dao.listarActivos();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar usuarios.", e); // mate fijate aca
        }
    }

    public Usuario crear(String nombre, String apellido, String mail, String celular,
                         String contrasena, Rol rol, Direccion direccion) {
        validar(nombre, apellido, mail, contrasena, rol, direccion);
        try {
            if (dao.existeMail(mail, null)) {
                throw new MailDuplicadoException("Ya existe un usuario con el mail " + mail);
            }
            if (dao.direccionAsignada(direccion.getId(), null)) {
                throw new IllegalStateException("La dirección ya está asociada a otro usuario.");
            }
            Usuario usuario = new Usuario(nombre, apellido, mail, celular, contrasena, rol, direccion);
            dao.guardar(usuario);
            return usuario;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al crear el usuario.", e);
        }
    }

    @Override
    public Usuario buscarPorId(Long id) {
        try {
            Usuario usuario = dao.buscarPorId(id);
            if (usuario == null) {
                throw new EntidadNoEncontradaException("No existe un usuario activo con el ID: " + id);
            }
            return usuario;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el usuario.", e);
        }
    }

    public Usuario buscarPorMail(String mail) {
        try {
            Usuario usuario = dao.buscarPorMail(mail);
            if (usuario == null) throw new EntidadNoEncontradaException("No existe un usuario con ese mail.");
            return usuario;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el usuario por mail.", e);
        }
    }

    public void editar(Long id, String nombre, String apellido, String mail, String celular,
                       String contrasena, Rol rol, Direccion direccion) {
        Usuario usuario = buscarPorId(id);
        try {
            if (nombre != null && !nombre.isBlank()) usuario.setNombre(nombre);
            if (apellido != null && !apellido.isBlank()) usuario.setApellido(apellido);
            if (mail != null && !mail.isBlank() && !mail.equalsIgnoreCase(usuario.getMail())) {
                if (dao.existeMail(mail, id)) throw new MailDuplicadoException("El mail ya está registrado.");
                usuario.setMail(mail);
            }
            if (celular != null && !celular.isBlank()) usuario.setCelular(celular);
            if (contrasena != null && !contrasena.isBlank()) usuario.setContrasena(contrasena);
            if (rol != null) usuario.setRol(rol);
            if (direccion != null && !direccion.equals(usuario.getDireccion())) {
                if (dao.direccionAsignada(direccion.getId(), id)) {
                    throw new IllegalStateException("La dirección ya está asociada a otro usuario.");
                }
                usuario.setDireccion(direccion);
            }
            dao.actualizar(usuario);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al editar el usuario.", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        buscarPorId(id);
        try {
            dao.eliminar(id);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el usuario.", e);
        }
    }

    private void validar(String nombre, String apellido, String mail, String contrasena,
                         Rol rol, Direccion direccion) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (apellido == null || apellido.isBlank()) throw new IllegalArgumentException("El apellido es obligatorio.");
        if (mail == null || mail.isBlank() || !mail.contains("@")) { // verificacion de sintaxis mail
            throw new IllegalArgumentException("El mail es obligatorio y debe tener formato válido.");
        }
        if (contrasena == null || contrasena.isBlank()) throw new IllegalArgumentException("La contraseña es obligatoria.");
        if (rol == null) throw new IllegalArgumentException("El rol es obligatorio.");
        if (direccion == null || direccion.isEliminado()) {
            throw new EntidadNoEncontradaException("La dirección es obligatoria y debe estar activa.");
        }
    }
}
