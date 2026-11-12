package pe.com.fadide.sisco.security;

// Token ausente, mal formado, con firma alterada o expirado
public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String message) {
        super(message);
    }
}
