package integrador.prog2.dao;

import java.sql.SQLException;
import java.util.List;

public interface IBaseDAO<T> {
    void guardar(T entidad) throws SQLException;
    T buscarPorId(Long id) throws SQLException;
    List<T> listarActivos() throws SQLException;
    void actualizar(T entidad) throws SQLException;
    void eliminar(Long id) throws SQLException;
}
