//package co.com.pragma.consumer.config;
//
//import co.com.pragma.jwtadapter.config.JwtProperties;
//import com.nimbusds.jose.jwk.JWKSet;
//import com.nimbusds.jose.jwk.OctetSequenceKey;
//import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
//import com.nimbusds.jose.proc.SecurityContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtEncoder;
//import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
//import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
//
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//
//@Configuration
//public class JwtConfig {
//
//    private static final String ALGORITHM_SHA256 = "HmacSHA256";
//    private final JwtProperties jwtProperties;
//
//    public JwtConfig(JwtProperties jwtProperties) {
//        this.jwtProperties = jwtProperties;
//    }
//
//    @Bean
//    public JwtEncoder jwtEncoder() {
//        byte[] secret = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
//        SecretKeySpec secretKey = new SecretKeySpec(secret, ALGORITHM_SHA256);
//
//        // Importante: definir kid y alg
//        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretKey)
//                .algorithm(com.nimbusds.jose.JWSAlgorithm.HS256) // algoritmo explícito
//                .keyID("my-key-id") // clave identificadora
//                .build();
//
//        ImmutableJWKSet<SecurityContext> jwkSet =
//                new ImmutableJWKSet<>(new JWKSet(jwk));
//
//        return new NimbusJwtEncoder(jwkSet);
//    }
//
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        byte[] secret = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
//        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, ALGORITHM_SHA256)).build();
//    }
//}
