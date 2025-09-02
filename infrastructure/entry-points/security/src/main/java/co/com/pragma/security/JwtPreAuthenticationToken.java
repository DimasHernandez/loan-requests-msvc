package co.com.pragma.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtPreAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;

    public JwtPreAuthenticationToken(String token) {
        super(null);
        this.token = token;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
