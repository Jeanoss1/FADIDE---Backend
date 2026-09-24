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

    public String extractCorreo(String token) {
        JWTClaimsSet claims = verifyAndExtractClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public boolean isTokenValid(String token, String correo) {
        JWTClaimsSet claims = verifyAndExtractClaims(token);
        if (claims == null) {
            return false;
        }
        Date expiration = claims.getExpirationTime();
        return correo.equals(claims.getSubject()) && expiration != null && expiration.after(new Date());
    }

    private JWTClaimsSet verifyAndExtractClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(secret.getBytes(StandardCharsets.UTF_8)))) {
                return null;
            }
            return signedJWT.getJWTClaimsSet();
        } catch (JOSEException | ParseException ex) {
            return null;
        }
    }
}
