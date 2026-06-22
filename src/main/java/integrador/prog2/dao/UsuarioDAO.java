package integrador.prog2.dao;

import integrador.prog2.config.ConexionDB;
import integrador.prog2.entities.Direccion;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements IBaseDAO<Usuario> {
    private static final String SELECT_BASE = """
            SELECT u.*, d.calle, d.numero, d.ciudad, d.codigo_postal,
                   d.eliminado AS direccion_eliminada, d.created_at AS direccion_created_at
            FROM usuario u
            JOIN direccion d ON d.id=u.direccion_id
            """;

    @Override
    public void guardar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, apellido, mail, celular, contrasena, rol, direccion_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            completarSentencia(sentencia, usuario);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) usuario.setId(claves.getLong(1));
            }
        }
    }

    @Override
    public Usuario buscarPorId(Long id) throws SQLException {
        return buscarPorId(id, false);
    }

    public Usuario buscarPorIdIncluyendoEliminados(Long id) throws SQLException {
        return buscarPorId(id, true);
    }

    private Usuario buscarPorId(Long id, boolean incluirEliminado) throws SQLException {
        String sql = SELECT_BASE + " WHERE u.id=? " + (incluirEliminado ? "" : "AND u.eliminado=FALSE");
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public List<Usuario> listarActivos() throws SQLException {
        String sql = SELECT_BASE + " WHERE u.eliminado=FALSE ORDER BY u.id";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) usuarios.add(mapear(resultado));
        }
        return usuarios;
    }

    public Usuario buscarPorMail(String mail) throws SQLException {
        String sql = SELECT_BASE + " WHERE LOWER(u.mail)=LOWER(?) AND u.eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, mail);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    public boolean existeMail(String mail, Long idAExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE LOWER(mail)=LOWER(?) " +
                     (idAExcluir == null ? "" : "AND id<>?");
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, mail);
            if (idAExcluir != null) sentencia.setLong(2, idAExcluir);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        }
    }

    public boolean direccionAsignada(Long direccionId, Long usuarioAExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE direccion_id=? " +
                     (usuarioAExcluir == null ? "" : "AND id<>?");
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, direccionId);
            if (usuarioAExcluir != null) sentencia.setLong(2, usuarioAExcluir);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nombre=?, apellido=?, mail=?, celular=?, contrasena=?, rol=?, " +
                     "direccion_id=? WHERE id=? AND eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            completarSentencia(sentencia, usuario);
            sentencia.setLong(8, usuario.getId());
            sentencia.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "UPDATE usuario SET eliminado=TRUE WHERE id=?";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            sentencia.executeUpdate();
        }
    }

    private void completarSentencia(PreparedStatement sentencia, Usuario usuario) throws SQLException {
        sentencia.setString(1, usuario.getNombre());
        sentencia.setString(2, usuario.getApellido());
        sentencia.setString(3, usuario.getMail());
        sentencia.setString(4, usuario.getCelular());
        sentencia.setString(5, usuario.getContrasena());
        sentencia.setString(6, usuario.getRol().name());
        sentencia.setLong(7, usuario.getDireccion().getId());
    }

    private Usuario mapear(ResultSet resultado) throws SQLException {
        Direccion direccion = new Direccion(
                resultado.getLong("direccion_id"),
                resultado.getString("calle"),
                resultado.getString("numero"),
                resultado.getString("ciudad"),
                resultado.getString("codigo_postal"));
        direccion.setEliminado(resultado.getBoolean("direccion_eliminada"));
        Timestamp fechaDireccion = resultado.getTimestamp("direccion_created_at");
        if (fechaDireccion != null) direccion.setCreatedAt(fechaDireccion.toLocalDateTime());

        Usuario usuario = new Usuario(
                resultado.getLong("id"),
                resultado.getString("nombre"),
                resultado.getString("apellido"),
                resultado.getString("mail"),
                resultado.getString("celular"),
                resultado.getString("contrasena"),
                Rol.valueOf(resultado.getString("rol")),
                direccion);
        usuario.setEliminado(resultado.getBoolean("eliminado"));
        Timestamp fecha = resultado.getTimestamp("created_at");
        if (fecha != null) usuario.setCreatedAt(fecha.toLocalDateTime());
        return usuario;
    }
}
