package integrador.prog2.dao;

import integrador.prog2.config.ConexionDB;
import integrador.prog2.entities.Pedido;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO implements IBaseDAO<Pedido> {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final DetallePedidoDAO detallePedidoDAO = new DetallePedidoDAO();

    @Override
    public void guardar(Pedido pedido) throws SQLException {
        try (Connection conexion = ConexionDB.getConnection()) {
            guardarCabecera(pedido, conexion);
        }
    }

    public void guardarCabecera(Pedido pedido, Connection conexion) throws SQLException {
        String sql = "INSERT INTO pedido (fecha, estado, total, forma_pago, usuario_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setDate(1, Date.valueOf(pedido.getFecha()));
            sentencia.setString(2, pedido.getEstado().name());
            sentencia.setDouble(3, pedido.getTotal());
            sentencia.setString(4, pedido.getFormaPago().name());
            sentencia.setLong(5, pedido.getUsuario().getId());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) pedido.setId(claves.getLong(1));
            }
        }
    }

    public void actualizarTotal(Pedido pedido, Connection conexion) throws SQLException {
        String sql = "UPDATE pedido SET total=? WHERE id=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setDouble(1, pedido.getTotal());
            sentencia.setLong(2, pedido.getId());
            sentencia.executeUpdate();
        }
    }

    @Override
    public Pedido buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM pedido WHERE id=? AND eliminado=FALSE";
        Pedido pedido;
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                pedido = resultado.next() ? mapear(resultado) : null;
            }
        }
        if (pedido != null) pedido.setDetalles(detallePedidoDAO.listarPorPedido(id));
        return pedido;
    }

    @Override
    public List<Pedido> listarActivos() throws SQLException {
        return listarConFiltro(null);
    }

    public List<Pedido> listarPorUsuario(Long usuarioId) throws SQLException {
        return listarConFiltro(usuarioId);
    }

    private List<Pedido> listarConFiltro(Long usuarioId) throws SQLException {
        String sql = "SELECT * FROM pedido WHERE eliminado=FALSE " +
                     (usuarioId == null ? "" : "AND usuario_id=? ") + "ORDER BY id";
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (usuarioId != null) sentencia.setLong(1, usuarioId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) pedidos.add(mapear(resultado));
            }
        }
        return pedidos;
    }

    @Override
    public void actualizar(Pedido pedido) throws SQLException {
        String sql = "UPDATE pedido SET estado=?, forma_pago=? WHERE id=? AND eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, pedido.getEstado().name());
            sentencia.setString(2, pedido.getFormaPago().name());
            sentencia.setLong(3, pedido.getId());
            sentencia.executeUpdate();
        }
    }

    public void actualizarEstadoYFormaPago(Long id, Estado estado, FormaPago formaPago) throws SQLException {
        String sql = "UPDATE pedido SET estado=?, forma_pago=? WHERE id=? AND eliminado=FALSE";
        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, estado.name());
            sentencia.setString(2, formaPago.name());
            sentencia.setLong(3, id);
            sentencia.executeUpdate();
        }
    }

    @Override
    public void eliminar(Long id) throws SQLException {
        Connection conexion = null;
        try {
            conexion = ConexionDB.getConnection();
            conexion.setAutoCommit(false);
            eliminarPedido(id, conexion);
            detallePedidoDAO.eliminarPorPedido(id, conexion);
            conexion.commit();
        } catch (SQLException e) {
            if (conexion != null) conexion.rollback();
            throw e;
        } finally {
            if (conexion != null) {
                conexion.setAutoCommit(true);
                conexion.close();
            }
        }
    }

    private void eliminarPedido(Long id, Connection conexion) throws SQLException {
        String sql = "UPDATE pedido SET eliminado=TRUE WHERE id=?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            sentencia.executeUpdate();
        }
    }

    private Pedido mapear(ResultSet resultado) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorIdIncluyendoEliminados(resultado.getLong("usuario_id"));
        Pedido pedido = new Pedido(
                resultado.getLong("id"),
                resultado.getDate("fecha").toLocalDate(),
                Estado.valueOf(resultado.getString("estado")),
                resultado.getDouble("total"),
                FormaPago.valueOf(resultado.getString("forma_pago")),
                usuario);
        pedido.setEliminado(resultado.getBoolean("eliminado"));
        Timestamp fecha = resultado.getTimestamp("created_at");
        if (fecha != null) pedido.setCreatedAt(fecha.toLocalDateTime());
        return pedido;
    }
}
