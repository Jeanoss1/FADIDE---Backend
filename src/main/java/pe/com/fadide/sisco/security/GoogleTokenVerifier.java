package pe.com.fadide.sisco.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Set;

// Verifica el ID token que Google entrega al frontend (firma, emisor, audiencia y expiración)
@Component
public class GoogleTokenVerifier {

    private static final String GOOGLE_CERTS = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    private final DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

    public GoogleTokenVerifier(@Value("${sisco.google.client-id}") String clientId) throws MalformedURLException {
        JWKSource<SecurityContext> keys = JWKSourceBuilder.create(URI.create(GOOGLE_CERTS).toURL()).build();
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keys));
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                clientId, null, Set.of("iss", "sub", "email", "exp")));
    }

    public GoogleUser verify(String idToken) {
        try {
            JWTClaimsSet claims = processor.process(idToken, null);
            if (!ISSUERS.contains(claims.getIssuer())) {
                throw new IllegalArgumentException("Emisor inválido");
            }
            if (!Boolean.TRUE.equals(claims.getBooleanClaim("email_verified"))) {
                throw new IllegalArgumentException("Correo no verificado");
            }
            String nombre = claims.getStringClaim("name");
            return new GoogleUser(claims.getStringClaim("email").toLowerCase(), nombre != null ? nombre : claims.getStringClaim("email"));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo validar la cuenta de Google");
        }
    }

    public record GoogleUser(String correo, String nombre) {
    }
}
