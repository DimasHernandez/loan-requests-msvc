package co.com.pragma.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

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
                                .pathMatchers("/api/v1/loans/**").hasRole("APPLICANT")
                                .pathMatchers("/webjars/swagger-ui/index.html").permitAll()
                                .pathMatchers("/swagger-ui.html").permitAll()
                                .pathMatchers("/swagger-ui/**").permitAll()
                                .pathMatchers("/webjars/**").permitAll()
                                .pathMatchers("/v3/api-docs").permitAll()
                                .pathMatchers("/v3/api-docs/**").permitAll()
                                .anyExchange().authenticated()
                )
                .build();
    }
}
