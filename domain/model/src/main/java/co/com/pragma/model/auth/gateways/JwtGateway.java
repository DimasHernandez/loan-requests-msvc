package co.com.pragma.model.auth.gateways;

public interface JwtGateway {

    Boolean validateToken(String token);

    String extractUserId(String token);

    String extractUserEmail(String token);

    String extractUserDocumentNumber(String token);

    String extractRole(String token);
}
