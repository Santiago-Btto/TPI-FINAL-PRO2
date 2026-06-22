# Food Store - Sistema de Gestión de Pedidos

Trabajo Práctico Integrador de Programación 2. Aplicación de consola desarrollada con
Java 21, Maven, JDBC y MySQL.

## Funcionalidades

- CRUD completo de categorías, productos, direcciones, usuarios y pedidos.
- Bajas lógicas mediante `eliminado = true`.
- Menús de consola con validación de entradas.
- Patrón DAO y separación por capas.
- `PreparedStatement`, `ResultSet` y `try-with-resources`.
- Pedido con 1..N detalles y total calculado mediante `Calculable`.
- Transacción única para Pedido + Detalles + descuento de stock.
- `commit` al finalizar y `rollback` ante cualquier error.
- Relación 1 a 1 unidireccional Usuario -> Dirección, asegurada también en MySQL
  mediante `UNIQUE (direccion_id)`.

## Requisitos

- JDK 21.
- Maven 3.9 o superior.
- MySQL 8 o superior.
- IntelliJ IDEA.

## Preparar la base de datos

1. Abrir MySQL Workbench.
2. Ejecutar completo `database/schema.sql`.
3. El script recrea `foodstore_db` e inserta datos de prueba.

## Configurar la conexión

1. Copiar `src/main/resources/db.properties.example`.
2. Pegar la copia en la raíz del proyecto.
3. Renombrarla a `db.properties`.
4. Completar usuario y contraseña:

```properties
db.url=jdbc:mysql://localhost:3306/foodstore_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=TU_CONTRASENA
```

`db.properties` ignorado para no publicar las credenciales.

## Ejecutar con Maven

```bash
mvn clean compile
mvn exec:java
```

También puede ejecutarse `integrador.prog2.Main` desde IntelliJ.

## Estructura

```text
src/main/java/integrador/prog2/
├── Main.java
├── config/
├── dao/
├── entities/
├── enums/
├── exception/
├── service/
└── ui/
```

## Prueba recomendada

1. Listar datos iniciales.
2. Crear una dirección.
3. Crear un usuario y asociarlo a esa dirección.
4. Intentar asociar esa misma dirección a otro usuario: debe rechazarse.
5. Crear categoría y producto.
6. Crear un pedido con detalles.
7. Consultar el pedido y verificar el descuento de stock.
8. Ejecutar `Pedidos > Demostrar rollback por stock insuficiente`.
9. Verificar en MySQL que el pedido fallido no fue guardado.
10. Probar editar y baja lógica de las entidades.

## Video demostrativo

-

## Documentación PDF

Dentro del ZIP
