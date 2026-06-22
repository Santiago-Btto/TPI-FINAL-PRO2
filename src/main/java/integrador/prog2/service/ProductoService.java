package integrador.prog2.service;

import integrador.prog2.dao.ProductoDAO;
import integrador.prog2.entities.Categoria;
import integrador.prog2.entities.Producto;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.exception.PersistenciaException;
import integrador.prog2.exception.StockInvalidoException;

import java.sql.SQLException;
import java.util.List;

public class ProductoService implements GenericService<Producto> {
    private final ProductoDAO dao = new ProductoDAO();

    @Override
    public List<Producto> listarActivos() {
        try {
            return dao.listarActivos();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar productos.", e);
        }
    }

    public List<Producto> listarPorCategoria(Categoria categoria) {
        if (categoria == null) throw new IllegalArgumentException("La categoría es obligatoria.");
        try {
            return dao.listarPorCategoria(categoria.getId());
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar productos por categoría.", e);
        }
    }

    public List<Producto> buscarPorNombre(String texto) {
        try {
            return dao.buscarPorNombre(texto);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar productos por nombre.", e);
        }
    }

    public Producto crear(String nombre, Double precio, String descripcion, int stock,
                          String imagen, boolean disponible, Categoria categoria) {
        validar(nombre, precio, stock, categoria);
        Producto producto = new Producto(nombre, precio, descripcion, stock, imagen, disponible, categoria);
        try {
            dao.guardar(producto);
            return producto;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al crear el producto.", e);
        }
    }

    @Override
    public Producto buscarPorId(Long id) {
        try {
            Producto producto = dao.buscarPorId(id);
            if (producto == null) {
                throw new EntidadNoEncontradaException("No existe un producto activo con ID " + id);
            }
            return producto;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el producto.", e);
        }
    }

    public void editar(Long id, String nombre, Double precio, String descripcion, Integer stock,
                       String imagen, Boolean disponible, Categoria categoria) {
        Producto producto = buscarPorId(id);
        if (nombre != null && !nombre.isBlank()) producto.setNombre(nombre);
        if (precio != null) {
            if (precio < 0) throw new StockInvalidoException("El precio no puede ser negativo.");
            producto.setPrecio(precio);
        }
        if (descripcion != null && !descripcion.isBlank()) producto.setDescripcion(descripcion);
        if (stock != null) {
            if (stock < 0) throw new StockInvalidoException("El stock no puede ser negativo.");
            producto.setStock(stock);
        }
        if (imagen != null && !imagen.isBlank()) producto.setImagen(imagen);
        if (disponible != null) producto.setDisponible(disponible);
        if (categoria != null) producto.setCategoria(categoria);

        try {
            dao.actualizar(producto);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al editar el producto.", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        buscarPorId(id);
        try {
            dao.eliminar(id);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el producto.", e);
        }
    }

    private void validar(String nombre, Double precio, int stock, Categoria categoria) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (precio == null || precio < 0) throw new StockInvalidoException("El precio debe ser mayor o igual a 0.");
        if (stock < 0) throw new StockInvalidoException("El stock debe ser mayor o igual a 0.");
        if (categoria == null || categoria.isEliminado()) {
            throw new EntidadNoEncontradaException("La categoría no existe o está eliminada.");
        }
    }
}
