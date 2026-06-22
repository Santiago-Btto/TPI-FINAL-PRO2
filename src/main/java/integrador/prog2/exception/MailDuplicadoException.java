package integrador.prog2.exception;

public class MailDuplicadoException extends RuntimeException {
    public MailDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
