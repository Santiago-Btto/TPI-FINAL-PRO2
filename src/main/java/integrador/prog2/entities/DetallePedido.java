package integrador.prog2.entities;

public class DetallePedido extends Base {
    private int cantidad;
    private Double subtotal;
    private Producto producto;

    public DetallePedido(int cantidad, Producto producto) {
        this(null, cantidad, producto == null ? 0.0 : cantidad * producto.getPrecio(), producto);
    }

    public DetallePedido(Long id, int cantidad, Double subtotal, Producto producto) {
        super(id);
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.producto = producto;
    }

    public void recalcularSubtotal() {
        this.subtotal = producto == null ? 0.0 : cantidad * producto.getPrecio();
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        recalcularSubtotal();
    }

    @Override
    public String toString() {
        return String.format("  → %-20s | Cantidad: %d | Subtotal: $%.2f",
                producto == null ? "Producto no disponible" : producto.getNombre(), cantidad, subtotal);
    }
}
