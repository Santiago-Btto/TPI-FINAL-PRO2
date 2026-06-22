package integrador.prog2.dao;

import integrador.prog2.config.ConexionDB;
import integrador.prog2.entities.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO implements IBaseDAO<Categoria> {

    @Override
    public void guardar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, categoria.getNombre());
            sentencia.setString(2, categoria.getDescripcion());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) categoria.setId(claves.getLong(1));
            }
        }
    }

    @Override
    public Categoria buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM categoria WHERE id=? AND eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    public Categoria buscarPorIdIncluyendoEliminadas(Long id) throws SQLException {
        String sql = "SELECT * FROM categoria WHERE id=?";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public List<Categoria> listarActivos() throws SQLException {
        String sql = "SELECT * FROM categoria WHERE eliminado=FALSE ORDER BY id";
        List<Categoria> categorias = new ArrayList<>();
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) categorias.add(mapear(resultado));
        }
        return categorias;
    }

    public Categoria buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM categoria WHERE LOWER(nombre)=LOWER(?) AND eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    public boolean existeNombre(String nombre, Long idAExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categoria WHERE LOWER(nombre)=LOWER(?) " +
                     (idAExcluir == null ? "" : "AND id<>?");
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            if (idAExcluir != null) sentencia.setLong(2, idAExcluir);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        }
    }

    public boolean tieneProductosActivos(Long categoriaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE categoria_id=? AND eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        }
    }

    @Override
    public void actualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE categoria SET nombre=?, descripcion=? WHERE id=? AND eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, categoria.getNombre());
            sentencia.setString(2, categoria.getDescripcion());
            sentencia.setLong(3, categoria.getId());
            sentencia.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "UPDATE categoria SET eliminado=TRUE WHERE id=?";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            sentencia.executeUpdate();
        }
    }

    private Categoria mapear(ResultSet resultado) throws SQLException {
        Categoria categoria = new Categoria(
                resultado.getLong("id"),
                resultado.getString("nombre"),
                resultado.getString("descripcion"));
        categoria.setEliminado(resultado.getBoolean("eliminado"));
        Timestamp fecha = resultado.getTimestamp("created_at");
        if (fecha != null) categoria.setCreatedAt(fecha.toLocalDateTime());
        return categoria;
    }
}
