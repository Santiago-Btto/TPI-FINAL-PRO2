package integrador.prog2.entities;

import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.exception.StockInvalidoException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Pedido extends Base implements Calculable {
    private LocalDate fecha;
    private Estado estado;
    private Double total;
    private FormaPago formaPago;
    private Usuario usuario;
    private List<DetallePedido> detalles;

    public Pedido(Usuario usuario, FormaPago formaPago) {
        super();
        this.usuario = usuario;
        this.formaPago = formaPago;
        this.fecha = LocalDate.now();
        this.estado = Estado.PENDIENTE;
        this.total = 0.0;
        this.detalles = new ArrayList<>();
    }

    public Pedido(Long id, LocalDate fecha, Estado estado, Double total,
                  FormaPago formaPago, Usuario usuario) {
        super(id);
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
        this.formaPago = formaPago;
        this.usuario = usuario;
        this.detalles = new ArrayList<>();
    }

    public void addDetallePedido(int cantidad, Double precio, Producto producto) {
        if (producto == null) {
            throw new EntidadNoEncontradaException("El producto es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new StockInvalidoException("La cantidad debe ser mayor a 0.");
        }

        DetallePedido existente = detalles.stream()
                .filter(d -> !d.isEliminado() && d.getProducto().equals(producto))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
        } else {
            DetallePedido detalle = new DetallePedido(cantidad, producto);
            detalle.setSubtotal(cantidad * precio);
            detalles.add(detalle);
        }
        calcularTotal();
    }

    public DetallePedido findeDetallePedidoByProducto(Producto producto) {
        return detalles.stream()
                .filter(d -> !d.isEliminado() && d.getProducto().equals(producto))
                .findFirst()
                .orElseThrow(() -> new EntidadNoEncontradaException(
                        "No existe un detalle para el producto: " + producto.getNombre()));
    }

    public void deleteDetallePedidoByProducto(Producto producto) {
        DetallePedido detalle = findeDetallePedidoByProducto(producto);
        detalle.setEliminado(true);
        calcularTotal();
    }

    @Override
    public void calcularTotal() {
        total = detalles.stream()
                .filter(d -> !d.isEliminado())
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles == null ? new ArrayList<>() : detalles;
        calcularTotal();
    }

    @Override
    public String toString() {
        String nombreUsuario = usuario == null ? "Sin usuario" : usuario.getNombre() + " " + usuario.getApellido();
        return String.format("[ID: %d] Usuario: %-20s | Estado: %-11s | Pago: %-14s | Total: $%.2f | %s",
                getId(), nombreUsuario, estado, formaPago, total, fecha);
    }
}
