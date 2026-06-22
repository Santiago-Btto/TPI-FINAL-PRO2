-- FOOD STORE
DROP DATABASE IF EXISTS foodstore_db;
CREATE DATABASE foodstore_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE foodstore_db;

-- relacion 1-1 usuario-direccion
CREATE TABLE direccion (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    calle          VARCHAR(120) NOT NULL,
    numero         VARCHAR(20) NOT NULL,
    ciudad         VARCHAR(100) NOT NULL,
    codigo_postal  VARCHAR(20) NOT NULL,
    eliminado      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categoria (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL UNIQUE,
    descripcion  VARCHAR(255) NOT NULL,
    eliminado    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE producto (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    precio        DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    descripcion   VARCHAR(255),
    stock         INT NOT NULL CHECK (stock >= 0),
    imagen        VARCHAR(255),
    disponible    BOOLEAN NOT NULL DEFAULT TRUE,
    categoria_id  BIGINT NOT NULL,
    eliminado     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

CREATE TABLE usuario (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    apellido      VARCHAR(100) NOT NULL,
    mail          VARCHAR(150) NOT NULL UNIQUE,
    celular       VARCHAR(30),
    contrasena    VARCHAR(255) NOT NULL,
    rol           ENUM('ADMIN', 'USUARIO') NOT NULL DEFAULT 'USUARIO',
    direccion_id  BIGINT NOT NULL UNIQUE,
    eliminado     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_direccion
        FOREIGN KEY (direccion_id) REFERENCES direccion(id)
);

CREATE TABLE pedido (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha        DATE NOT NULL,
    estado       ENUM('PENDIENTE', 'CONFIRMADO', 'TERMINADO', 'CANCELADO')
                 NOT NULL DEFAULT 'PENDIENTE',
    total        DECIMAL(10,2) NOT NULL DEFAULT 0,
    forma_pago   ENUM('TARJETA', 'TRANSFERENCIA', 'EFECTIVO') NOT NULL,
    usuario_id   BIGINT NOT NULL,
    eliminado    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE detalle_pedido (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    cantidad     INT NOT NULL CHECK (cantidad > 0),
    subtotal     DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    pedido_id    BIGINT NOT NULL,
    producto_id  BIGINT NOT NULL,
    eliminado    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_detalle_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_detalle_producto
        FOREIGN KEY (producto_id) REFERENCES producto(id)
);

-- Datos de prueba
INSERT INTO direccion (calle, numero, ciudad, codigo_postal) VALUES
('San Martín', '1200', 'Mendoza', '5500'),
('Las Heras', '350', 'Godoy Cruz', '5501'),
('Colón', '89', 'Luján de Cuyo', '5507');

INSERT INTO categoria (nombre, descripcion) VALUES
('Bebidas', 'Bebidas frías y calientes'),
('Comidas', 'Platos principales'),
('Postres', 'Dulces y postres');

INSERT INTO producto (nombre, precio, descripcion, stock, imagen, disponible, categoria_id) VALUES
('Gaseosa', 1800.00, 'Botella de 500 ml', 20, 'gaseosa.jpg', TRUE, 1),
('Agua mineral', 1200.00, 'Botella de 500 ml', 30, 'agua.jpg', TRUE, 1),
('Hamburguesa', 6500.00, 'Hamburguesa completa', 10, 'hamburguesa.jpg', TRUE, 2),
('Pizza', 9000.00, 'Pizza grande de muzzarella', 8, 'pizza.jpg', TRUE, 2),
('Flan', 2500.00, 'Flan con dulce de leche', 12, 'flan.jpg', TRUE, 3);

INSERT INTO usuario (nombre, apellido, mail, celular, contrasena, rol, direccion_id) VALUES
('Admin', 'Sistema', 'admin@foodstore.com', '2610000000', 'admin123', 'ADMIN', 1),
('Juan', 'Pérez', 'juan@mail.com', '2611111111', 'juan123', 'USUARIO', 2);

SELECT 'Base foodstore_db creada correctamente' AS resultado;
SELECT * FROM direccion;
SELECT * FROM categoria;
SELECT * FROM producto;
SELECT * FROM usuario;
