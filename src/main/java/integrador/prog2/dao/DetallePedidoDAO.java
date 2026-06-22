package integrador.prog2.dao;

import integrador.prog2.config.ConexionDB;
import integrador.prog2.entities.DetallePedido;
import integrador.prog2.entities.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DetallePedidoDAO {
    private final ProductoDAO productoDAO = new ProductoDAO();

    public void guardar(DetallePedido detalle, Long pedidoId, Connection conexion) throws SQLException {
        String sql = "INSERT INTO detalle_pedido (cantidad, subtotal, pedido_id, producto_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setInt(1, detalle.getCantidad());
            sentencia.setDouble(2, detalle.getSubtotal());
            sentencia.setLong(3, pedidoId);
            sentencia.setLong(4, detalle.getProducto().getId());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) detalle.setId(claves.getLong(1));
            }
        }
    }

    public List<DetallePedido> listarPorPedido(Long pedidoId) throws SQLException {
        String sql = "SELECT * FROM detalle_pedido WHERE pedido_id=? AND eliminado=FALSE ORDER BY id";
        List<DetallePedido> detalles = new ArrayList<>();
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, pedidoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    Producto producto = productoDAO.buscarPorId(resultado.getLong("producto_id"), true, null);
                    DetallePedido detalle = new DetallePedido(
                            resultado.getLong("id"),
                            resultado.getInt("cantidad"),
                            resultado.getDouble("subtotal"),
                            producto);
                    detalle.setEliminado(resultado.getBoolean("eliminado"));
                    Timestamp fecha = resultado.getTimestamp("created_at");
                    if (fecha != null) detalle.setCreatedAt(fecha.toLocalDateTime());
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    public void eliminarPorPedido(Long pedidoId, Connection conexion) throws SQLException {
        String sql = "UPDATE detalle_pedido SET eliminado=TRUE WHERE pedido_id=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, pedidoId);
            sentencia.executeUpdate();
        }
    }
}
