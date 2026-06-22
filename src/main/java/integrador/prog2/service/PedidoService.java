package integrador.prog2.service;

import integrador.prog2.config.ConexionDB;
import integrador.prog2.dao.DetallePedidoDAO;
import integrador.prog2.dao.PedidoDAO;
import integrador.prog2.dao.ProductoDAO;
import integrador.prog2.entities.DetallePedido;
import integrador.prog2.entities.Pedido;
import integrador.prog2.entities.Producto;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.exception.PersistenciaException;
import integrador.prog2.exception.StockInvalidoException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PedidoService implements GenericService<Pedido> {
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    public List<Pedido> listarActivos() {
        try {
            return pedidoDAO.listarActivos();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar pedidos.", e);
        }
    }

    public List<Pedido> listarPorUsuario(Usuario usuario) {
        if (usuario == null) throw new IllegalArgumentException("El usuario es obligatorio.");
        try {
            return pedidoDAO.listarPorUsuario(usuario.getId());
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar pedidos por usuario.", e);
        }
    }

    public Pedido iniciarPedido(Usuario usuario, FormaPago formaPago) {
        if (usuario == null || usuario.isEliminado()) {
            throw new EntidadNoEncontradaException("El usuario no existe o está eliminado.");
        }
        if (formaPago == null) throw new IllegalArgumentException("La forma de pago es obligatoria.");
        return new Pedido(usuario, formaPago);
    }

    public void agregarDetalle(Pedido pedido, int cantidad, Producto producto) {
        if (pedido == null) throw new IllegalArgumentException("El pedido es obligatorio.");
        if (producto == null || producto.isEliminado() || !producto.isDisponible()) {
            throw new EntidadNoEncontradaException("El producto no está disponible.");
        }
        pedido.addDetallePedido(cantidad, producto.getPrecio(), producto);
    }

    public void confirmarPedido(Pedido pedido) {
        if (pedido == null || pedido.getUsuario() == null) {
            throw new IllegalArgumentException("El pedido debe tener un usuario.");
        }
        if (pedido.getDetalles().stream().noneMatch(d -> !d.isEliminado())) {
            throw new IllegalArgumentException("El pedido debe tener al menos un detalle.");
        }

        Connection conexion = null;
        try {
            conexion = ConexionDB.getConnection();
            conexion.setAutoCommit(false);

            // Se guarda primero la cabecera con total 0. Si luego falla un detalle,
            // el rollback elimina también esta inserción.
            pedido.setTotal(0.0);
            pedidoDAO.guardarCabecera(pedido, conexion);

            double total = 0.0;
            for (DetallePedido detalle : pedido.getDetalles()) {
                if (detalle.isEliminado()) continue;

                Producto productoActual = productoDAO.buscarPorIdParaActualizar(
                        detalle.getProducto().getId(), conexion);

                if (productoActual == null || !productoActual.isDisponible()) {
                    throw new EntidadNoEncontradaException("Producto inexistente o no disponible.");
                }
                if (detalle.getCantidad() <= 0) {
                    throw new StockInvalidoException("La cantidad debe ser mayor a 0.");
                }
                if (productoActual.getStock() < detalle.getCantidad()) {
                    throw new StockInvalidoException(
                            "Stock insuficiente para " + productoActual.getNombre() +
                            ". Disponible: " + productoActual.getStock());
                }

                detalle.setProducto(productoActual);
                detalle.setSubtotal(productoActual.getPrecio() * detalle.getCantidad());
                detalleDAO.guardar(detalle, pedido.getId(), conexion);
                productoDAO.descontarStock(productoActual.getId(), detalle.getCantidad(), conexion);
                total += detalle.getSubtotal();
            }

            pedido.setTotal(total);
            pedidoDAO.actualizarTotal(pedido, conexion);
            conexion.commit();
        } catch (Exception e) {
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }
            if (e instanceof RuntimeException runtime) throw runtime;
            throw new PersistenciaException("Error al crear el pedido. Se aplicó rollback.", e);
        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException ignored) {
                    // La operación principal ya terminó.
                }
            }
        }
    }

    @Override
    public Pedido buscarPorId(Long id) {
        try {
            Pedido pedido = pedidoDAO.buscarPorId(id);
            if (pedido == null) throw new EntidadNoEncontradaException("No existe un pedido activo con ID " + id);
            return pedido;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el pedido.", e);
        }
    }

    public void actualizarEstadoYFormaPago(Long id, Estado estado, FormaPago formaPago) {
        buscarPorId(id);
        if (estado == null || formaPago == null) throw new IllegalArgumentException("Estado y forma de pago son obligatorios.");
        try {
            pedidoDAO.actualizarEstadoYFormaPago(id, estado, formaPago);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar el pedido.", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        buscarPorId(id);
        try {
            pedidoDAO.eliminar(id);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el pedido.", e);
        }
    }
}
