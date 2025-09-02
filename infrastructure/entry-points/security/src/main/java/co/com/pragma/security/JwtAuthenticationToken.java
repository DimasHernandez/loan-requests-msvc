package co.com.pragma.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String userId;
    private final String email;
    private final String documentNumber;
    private final String token;

    public JwtAuthenticationToken(String userId, String email, String documentNumber, String token,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userId = userId;
        this.email = email;
        this.documentNumber = documentNumber;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return email;
    }

    public Object getUserId() {
        return userId;
    }

    public Object getDocumentNumber() {
        return documentNumber;
    }
}
