package pe.com.fadide.sisco.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

/**
 * Genera y valida los tokens JWT (firmados con HMAC-SHA256).
 * Estructura del token: HEADER.PAYLOAD.FIRMA
 *  - header: algoritmo (HS256)
 *  - payload (claims): sub = correo, rol, iat = emitido, exp = expiración
 *  - firma: HMAC del header + payload con la clave secreta del servidor
 */
@Component
public class JwtService {

    @Value("${sisco.jwt.secret}")
    private String secret;

    @Value("${sisco.jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(String correo, String rol) {
        try {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + expirationMs);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(correo)
                    .claim("rol", rol)
                    .issueTime(now)
                    .expirationTime(expiry)
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
            return signedJWT.serialize();
        } catch (JOSEException ex) {
            throw new IllegalStateException("Error al generar el token JWT", ex);
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * Valida formato, firma y expiración del token y devuelve el correo (subject).
     * Si algo falla lanza TokenInvalidoException con el motivo, que el
     * JwtAuthenticationEntryPoint devuelve al cliente en la respuesta 401.
     */
    public String validarToken(String token) {
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException ex) {
            throw new TokenInvalidoException("El token no tiene un formato JWT válido");
        }

        try {
            if (!signedJWT.verify(new MACVerifier(secret.getBytes(StandardCharsets.UTF_8)))) {
                throw new TokenInvalidoException("La firma del token no es válida");
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new TokenInvalidoException("El token expiró, inicia sesión nuevamente");
            }
            if (claims.getSubject() == null) {
                throw new TokenInvalidoException("El token no identifica a ningún usuario");
            }
            return claims.getSubject();
        } catch (JOSEException | ParseException ex) {
            throw new TokenInvalidoException("No se pudo verificar el token");
        }
    }

    public String extractCorreo(String token) {
        try {
            return validarToken(token);
        } catch (TokenInvalidoException ex) {
            return null;
        }
    }

    public boolean isTokenValid(String token, String correo) {
        String subject = extractCorreo(token);
        return subject != null && subject.equals(correo);
    }
}
