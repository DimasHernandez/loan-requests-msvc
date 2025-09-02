//package co.com.pragma.consumer.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.oauth2.jwt.JwtClaimsSet;
//import org.springframework.security.oauth2.jwt.JwtEncoder;
//import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class ServiceTokenProvider {
//
//    private final JwtEncoder jwtEncoder;
//
//    public String generateServiceToken() {
//        Instant now = Instant.now();
//
//        JwtClaimsSet claims = JwtClaimsSet.builder()
//                .issuer("auth-service")
//                .issuedAt(now)
//                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
//                .subject("request-msvd")
//                .claim("roles", List.of("ADMIN"))
//                .build();
//        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
//    }
//}
