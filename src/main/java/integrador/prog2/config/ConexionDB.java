package integrador.prog2.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionDB {
    private static final String ARCHIVO = "db.properties"; // conexion a la db
    private static final Properties PROPIEDADES = cargarPropiedades();

    private ConexionDB() {
    }

    private static Properties cargarPropiedades() {
        Properties propiedades = new Properties();
        Path archivoExterno = Path.of(ARCHIVO);

        try (InputStream entrada = Files.exists(archivoExterno)
                ? Files.newInputStream(archivoExterno)
                : ConexionDB.class.getClassLoader().getResourceAsStream(ARCHIVO)) {

            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontro el db.properties"
                );
            }

            propiedades.load(entrada);
            validarPropiedad(propiedades, "db.url");
            validarPropiedad(propiedades, "db.user");
            validarPropiedad(propiedades, "db.password");
            return propiedades;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO, e);
        }
    }

    private static void validarPropiedad(Properties propiedades, String clave) {
        if (!propiedades.containsKey(clave)) {
            throw new IllegalStateException("Falta la propiedad " + clave + " en " + ARCHIVO);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPIEDADES.getProperty("db.url"),
                PROPIEDADES.getProperty("db.user"),
                PROPIEDADES.getProperty("db.password"));
    }
}
