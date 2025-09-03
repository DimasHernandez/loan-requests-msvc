package co.com.pragma.model.auth.gateways;

public interface JwtGateway {

    Boolean validateToken(String token);

    String extractUserEmail(String token);

    String extractRole(String token);
}
