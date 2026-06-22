package integrador.prog2.service;

import integrador.prog2.dao.DireccionDAO;
import integrador.prog2.entities.Direccion;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.exception.PersistenciaException;

import java.sql.SQLException;
import java.util.List;

public class DireccionService implements GenericService<Direccion> {
    private final DireccionDAO dao = new DireccionDAO();

    @Override
    public List<Direccion> listarActivos() {
        try {
            return dao.listarActivos();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar direcciones.", e);
        }
    }

    public Direccion crear(String calle, String numero, String ciudad, String codigoPostal) {
        validarTexto(calle, "La calle");
        validarTexto(numero, "El número");
        validarTexto(ciudad, "La ciudad");
        validarTexto(codigoPostal, "El código postal");

        Direccion direccion = new Direccion(calle, numero, ciudad, codigoPostal);
        try {
            dao.guardar(direccion);
            return direccion;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al crear la dirección.", e);
        }
    }

    @Override
    public Direccion buscarPorId(Long id) {
        try {
            Direccion direccion = dao.buscarPorId(id);
            if (direccion == null) {
                throw new EntidadNoEncontradaException("No existe una dirección activa con ID " + id);
            }
            return direccion;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar la dirección.", e);
        }
    }

    public void editar(Long id, String calle, String numero, String ciudad, String codigoPostal) {
        Direccion direccion = buscarPorId(id);
        if (calle != null && !calle.isBlank()) direccion.setCalle(calle);
        if (numero != null && !numero.isBlank()) direccion.setNumero(numero);
        if (ciudad != null && !ciudad.isBlank()) direccion.setCiudad(ciudad);
        if (codigoPostal != null && !codigoPostal.isBlank()) direccion.setCodigoPostal(codigoPostal);

        try {
            dao.actualizar(direccion);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar la dirección.", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        buscarPorId(id);
        try {
            if (dao.estaAsignada(id, null)) {
                throw new IllegalStateException("La dirección está asociada a un usuario y no puede eliminarse.");
            }
            dao.eliminar(id);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar la dirección.", e);
        }
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
    }
}
