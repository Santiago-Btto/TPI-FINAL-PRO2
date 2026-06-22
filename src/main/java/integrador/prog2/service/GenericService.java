package integrador.prog2.service;

import java.util.List;

public interface GenericService<T> {
    List<T> listarActivos();
    T buscarPorId(Long id);
    void eliminar(Long id);
}
