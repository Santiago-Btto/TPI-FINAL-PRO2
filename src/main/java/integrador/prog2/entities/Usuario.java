package integrador.prog2.entities;

import integrador.prog2.enums.Rol;

import java.util.ArrayList;
import java.util.List;

public class Usuario extends Base {
    private String nombre;
    private String apellido;
    private String mail;
    private String celular;
    private String contrasena;
    private Rol rol;
    private Direccion direccion;
    private final List<Pedido> pedidos;

    public Usuario(String nombre, String apellido, String mail, String celular,
                   String contrasena, Rol rol, Direccion direccion) {
        this(null, nombre, apellido, mail, celular, contrasena, rol, direccion);
    }

    public Usuario(Long id, String nombre, String apellido, String mail, String celular,
                   String contrasena, Rol rol, Direccion direccion) {
        super(id);
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.celular = celular;
        this.contrasena = contrasena;
        this.rol = rol;
        this.direccion = direccion;
        this.pedidos = new ArrayList<>();
    }

    public void agregarPedido(Pedido pedido) {
        if (pedido != null && !pedidos.contains(pedido)) {
            pedidos.add(pedido);
            pedido.setUsuario(this);
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    @Override
    public String toString() {
        return String.format("[ID: %d] %s %s | Mail: %s | Rol: %s | Dirección: %s",
                getId(), nombre, apellido, mail, rol,
                direccion == null ? "Sin dirección" : direccion.getCalle() + " " + direccion.getNumero());
    }
}
