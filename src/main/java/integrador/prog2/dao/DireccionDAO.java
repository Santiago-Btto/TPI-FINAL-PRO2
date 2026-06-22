package integrador.prog2.dao;

import integrador.prog2.config.ConexionDB;
import integrador.prog2.entities.Direccion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DireccionDAO implements IBaseDAO<Direccion> {

    @Override
    public void guardar(Direccion direccion) throws SQLException {
        try (Connection conexion = ConexionDB.getConnection()) {
            guardar(direccion, conexion);
        }
    }

    public void guardar(Direccion direccion, Connection conexion) throws SQLException {
        String sql = "INSERT INTO direccion (calle, numero, ciudad, codigo_postal) VALUES (?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, direccion.getCalle());
            sentencia.setString(2, direccion.getNumero());
            sentencia.setString(3, direccion.getCiudad());
            sentencia.setString(4, direccion.getCodigoPostal());
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) direccion.setId(claves.getLong(1));
            }
        }
    }

    @Override
    public Direccion buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM direccion WHERE id = ? AND eliminado = FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    public Direccion buscarPorIdIncluyendoEliminadas(Long id) throws SQLException {
        String sql = "SELECT * FROM direccion WHERE id = ?";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public List<Direccion> listarActivos() throws SQLException {
        String sql = "SELECT * FROM direccion WHERE eliminado = FALSE ORDER BY id";
        List<Direccion> direcciones = new ArrayList<>();
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) direcciones.add(mapear(resultado));
        }
        return direcciones;
    }

    @Override
    public void actualizar(Direccion direccion) throws SQLException {
        String sql = "UPDATE direccion SET calle=?, numero=?, ciudad=?, codigo_postal=? " +
                     "WHERE id=? AND eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, direccion.getCalle());
            sentencia.setString(2, direccion.getNumero());
            sentencia.setString(3, direccion.getCiudad());
            sentencia.setString(4, direccion.getCodigoPostal());
            sentencia.setLong(5, direccion.getId());
            sentencia.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "UPDATE direccion SET eliminado=TRUE WHERE id=?";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            sentencia.executeUpdate();
        }
    }

    public boolean estaAsignada(Long direccionId, Long usuarioAExcluir) throws SQLException {
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

    private Direccion mapear(ResultSet resultado) throws SQLException {
        Direccion direccion = new Direccion(
                resultado.getLong("id"),
                resultado.getString("calle"),
                resultado.getString("numero"),
                resultado.getString("ciudad"),
                resultado.getString("codigo_postal"));
        direccion.setEliminado(resultado.getBoolean("eliminado"));
        Timestamp fecha = resultado.getTimestamp("created_at");
        if (fecha != null) direccion.setCreatedAt(fecha.toLocalDateTime());
        return direccion;
    }
}
