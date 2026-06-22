package integrador.prog2.dao;

import integrador.prog2.config.ConexionDB;
import integrador.prog2.entities.Categoria;
import integrador.prog2.entities.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO implements IBaseDAO<Producto> {
    private static final String SELECT_BASE = """
            SELECT p.*, c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion,
                   c.eliminado AS categoria_eliminada, c.created_at AS categoria_created_at
            FROM producto p
            JOIN categoria c ON c.id=p.categoria_id
            """;

    @Override
    public void guardar(Producto producto) throws SQLException {
        String sql = "INSERT INTO producto (nombre, precio, descripcion, stock, imagen, disponible, categoria_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            completarSentencia(sentencia, producto);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) producto.setId(claves.getLong(1));
            }
        }
    }

    @Override
    public Producto buscarPorId(Long id) throws SQLException {
        return buscarPorId(id, false, null);
    }

    public Producto buscarPorId(Long id, boolean incluirEliminado, Connection conexionExterna) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.id=? " + (incluirEliminado ? "" : "AND p.eliminado=FALSE");
        boolean cerrarConexion = conexionExterna == null;
        Connection conexion = cerrarConexion ? ConexionDB.getConnection() : conexionExterna;
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        } finally {
            if (cerrarConexion) conexion.close();
        }
    }

    public Producto buscarPorIdParaActualizar(Long id, Connection conexion) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.id=? AND p.eliminado=FALSE FOR UPDATE";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public List<Producto> listarActivos() throws SQLException {
        return ejecutarListado(SELECT_BASE + " WHERE p.eliminado=FALSE ORDER BY p.id", null);
    }

    public List<Producto> listarPorCategoria(Long categoriaId) throws SQLException {
        return ejecutarListado(SELECT_BASE + " WHERE p.eliminado=FALSE AND p.categoria_id=? ORDER BY p.id", categoriaId);
    }

    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.eliminado=FALSE AND LOWER(p.nombre) LIKE LOWER(?) ORDER BY p.nombre";
        List<Producto> productos = new ArrayList<>();
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, "%" + texto + "%");
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) productos.add(mapear(resultado));
            }
        }
        return productos;
    }

    @Override
    public void actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE producto SET nombre=?, precio=?, descripcion=?, stock=?, imagen=?, disponible=?, " +
                     "categoria_id=? WHERE id=? AND eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            completarSentencia(sentencia, producto);
            sentencia.setLong(8, producto.getId());
            sentencia.executeUpdate();
        }
    }

    public void descontarStock(Long productoId, int cantidad, Connection conexion) throws SQLException {
        String sql = "UPDATE producto SET stock=stock-? WHERE id=? AND eliminado=FALSE AND stock>=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, cantidad);
            sentencia.setLong(2, productoId);
            sentencia.setInt(3, cantidad);
            int filas = sentencia.executeUpdate();
            if (filas == 0) throw new SQLException("No se pudo descontar stock del producto ID " + productoId);
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        String sql = "UPDATE producto SET eliminado=TRUE, disponible=FALSE WHERE id=?";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            sentencia.executeUpdate();
        }
    }

    private void completarSentencia(PreparedStatement sentencia, Producto producto) throws SQLException {
        sentencia.setString(1, producto.getNombre());
        sentencia.setDouble(2, producto.getPrecio());
        sentencia.setString(3, producto.getDescripcion());
        sentencia.setInt(4, producto.getStock());
        sentencia.setString(5, producto.getImagen());
        sentencia.setBoolean(6, producto.isDisponible());
        sentencia.setLong(7, producto.getCategoria().getId());
    }

    private List<Producto> ejecutarListado(String sql, Long categoriaId) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (categoriaId != null) sentencia.setLong(1, categoriaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) productos.add(mapear(resultado));
            }
        }
        return productos;
    }

    private Producto mapear(ResultSet resultado) throws SQLException {
        Categoria categoria = new Categoria(
                resultado.getLong("categoria_id"),
                resultado.getString("categoria_nombre"),
                resultado.getString("categoria_descripcion"));
        categoria.setEliminado(resultado.getBoolean("categoria_eliminada"));
        Timestamp fechaCategoria = resultado.getTimestamp("categoria_created_at");
        if (fechaCategoria != null) categoria.setCreatedAt(fechaCategoria.toLocalDateTime());

        Producto producto = new Producto(
                resultado.getLong("id"),
                resultado.getString("nombre"),
                resultado.getDouble("precio"),
                resultado.getString("descripcion"),
                resultado.getInt("stock"),
                resultado.getString("imagen"),
                resultado.getBoolean("disponible"),
                categoria);
        producto.setEliminado(resultado.getBoolean("eliminado"));
        Timestamp fecha = resultado.getTimestamp("created_at");
        if (fecha != null) producto.setCreatedAt(fecha.toLocalDateTime());
        return producto;
    }
}
