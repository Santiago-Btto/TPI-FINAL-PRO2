package integrador.prog2.service;

import integrador.prog2.dao.CategoriaDAO;
import integrador.prog2.entities.Categoria;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.exception.NombreDuplicadoException;
import integrador.prog2.exception.PersistenciaException;

import java.sql.SQLException;
import java.util.List;

public class CategoriaService implements GenericService<Categoria> {
    private final CategoriaDAO dao = new CategoriaDAO();

    @Override
    public List<Categoria> listarActivos() {
        try {
            return dao.listarActivos();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar categorías.", e);
        }
    }

    public Categoria crear(String nombre, String descripcion) {
        validar(nombre, descripcion);
        try {
            if (dao.existeNombre(nombre, null)) {
                throw new NombreDuplicadoException("Ya existe una categoría llamada " + nombre);
            }
            Categoria categoria = new Categoria(nombre, descripcion);
            dao.guardar(categoria);
            return categoria;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al crear la categoría.", e);
        }
    }

    @Override
    public Categoria buscarPorId(Long id) {
        try {
            Categoria categoria = dao.buscarPorId(id);
            if (categoria == null) {
                throw new EntidadNoEncontradaException("No existe una categoría activa con ID " + id);
            }
            return categoria;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar la categoría.", e);
        }
    }

    public Categoria buscarPorNombre(String nombre) {
        try {
            Categoria categoria = dao.buscarPorNombre(nombre);
            if (categoria == null) {
                throw new EntidadNoEncontradaException("No existe una categoría llamada " + nombre);
            }
            return categoria;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar la categoría por nombre.", e);
        }
    }

    public void editar(Long id, String nombre, String descripcion) {
        Categoria categoria = buscarPorId(id);
        try {
            if (nombre != null && !nombre.isBlank()) {
                if (dao.existeNombre(nombre, id)) {
                    throw new NombreDuplicadoException("Ya existe una categoría llamada " + nombre);
                }
                categoria.setNombre(nombre);
            }
            if (descripcion != null && !descripcion.isBlank()) categoria.setDescripcion(descripcion);
            dao.actualizar(categoria);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al editar la categoría.", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        buscarPorId(id);
        try {
            if (dao.tieneProductosActivos(id)) {
                throw new IllegalStateException("No se puede eliminar: la categoría tiene productos activos.");
            }
            dao.eliminar(id);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar la categoría.", e);
        }
    }

    private void validar(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (descripcion == null || descripcion.isBlank()) throw new IllegalArgumentException("La descripción es obligatoria.");
    }
}
