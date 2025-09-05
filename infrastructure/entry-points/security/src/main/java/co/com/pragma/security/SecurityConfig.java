package co.com.pragma.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationManager jwtAuthenticationManager;

    private final JwtSecurityContextRepository jwtSecurityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authenticationManager(jwtAuthenticationManager)
                .securityContextRepository(jwtSecurityContextRepository)
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers(HttpMethod.POST, "/api/v1/loans").hasRole("APPLICANT")
                                .pathMatchers(HttpMethod.GET, "/api/v1/loans").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.POST, "/api/v1/users/emails/batch").hasRole("ADMIN")
                                .pathMatchers("/webjars/swagger-ui/index.html").permitAll()
                                .pathMatchers("/swagger-ui.html").permitAll()
                                .pathMatchers("/swagger-ui/**").permitAll()
                                .pathMatchers("/webjars/**").permitAll()
                                .pathMatchers("/v3/api-docs").permitAll()
                                .pathMatchers("/v3/api-docs/**").permitAll()
                                .anyExchange().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authorizedHandler())
                        .accessDeniedHandler(forbiddenHandler()))
                .build();
    }

    @Bean
    public ServerAuthenticationEntryPoint authorizedHandler() {
        return ((exchange, ex) -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.UNAUTHORIZED.value());
            body.put("error", "No autorizado");
            body.put("message", "No tiene credenciales validas");

            byte[] bytes = writeToJson(body);
            return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(bytes))
            );
        });
    }

    @Bean
    public ServerAccessDeniedHandler forbiddenHandler() {
        return ((exchange, ex) -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.FORBIDDEN.value());
            body.put("error", "Forbidden");
            body.put("message", "No tiene credenciales validas");

            byte[] bytes = writeToJson(body);
            return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(bytes))
            );
        });
    }

    private byte[] writeToJson(Map<String, Object> body) {
        try {
            return new ObjectMapper().writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return ("{\"error\":\"Serialization error\"}").getBytes(StandardCharsets.UTF_8);
        }

    }
}
