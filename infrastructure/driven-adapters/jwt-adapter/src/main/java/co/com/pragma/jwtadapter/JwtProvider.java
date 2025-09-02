package co.com.pragma.jwtadapter;

import co.com.pragma.jwtadapter.config.JwtProperties;
import co.com.pragma.model.auth.gateways.JwtGateway;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.logging.Logger;

@Component
public class JwtProvider implements JwtGateway {

    private final JwtProperties jwtProperties;

    private static final Logger log = Logger.getLogger(JwtProvider.class.getName());

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getKey(jwtProperties.getSecretKey()))
                    .build()
                    .parseClaimsJws(token);

            Date expiration = claimsJws.getBody().getExpiration();
            return expiration.after(new Date());
        } catch (ExpiredJwtException e) {
            log.info("Token is expired");
        } catch (UnsupportedJwtException e) {
            log.info("Token is not supported");
        } catch (MalformedJwtException e) {
            log.info("Token is malformed");
        } catch (SignatureException e) {
            log.info("Token is not signature");
        } catch (IllegalArgumentException e) {
            log.info("Token is empty or null");
        }
        return false;
    }

    @Override
    public String extractUserId(String token) {
        return extractClaims(token).get("userId", String.class);
    }

    @Override
    public String extractUserEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    @Override
    public String extractUserDocumentNumber(String token) {
        return extractClaims(token).get("documentNumber", String.class);
    }

    @Override
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey(jwtProperties.getSecretKey()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getKey(String secretKey) {
        byte[] secretBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
