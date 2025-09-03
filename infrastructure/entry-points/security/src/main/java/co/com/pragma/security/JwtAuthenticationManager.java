package co.com.pragma.security;

import co.com.pragma.model.auth.gateways.JwtGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtGateway jwtGateway;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        if (!jwtGateway.validateToken(token)) {
            return Mono.empty();
        }

        String email = jwtGateway.extractUserEmail(token);
        String role = jwtGateway.extractRole(token);

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        AbstractAuthenticationToken auth = new JwtAuthenticationToken(email, token, authorities);
        return Mono.just(auth);
    }
}
