package integrador.prog2.entities;

public class Direccion extends Base {
    private String calle;
    private String numero;
    private String ciudad;
    private String codigoPostal;

    public Direccion(String calle, String numero, String ciudad, String codigoPostal) {
        this(null, calle, numero, ciudad, codigoPostal);
    }

    public Direccion(Long id, String calle, String numero, String ciudad, String codigoPostal) {
        super(id);
        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    @Override
    public String toString() {
        return String.format("[ID: %d] %s %s, %s (%s)",
                getId(), calle, numero, ciudad, codigoPostal);
    }
}
