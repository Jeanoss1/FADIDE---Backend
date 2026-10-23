package pe.com.fadide.sisco.common.exception;

// El GlobalExceptionHandler la convierte en una respuesta 404
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
