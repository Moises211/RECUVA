package com.gt11.RECUVA.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Importar para @PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;





@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita la seguridad a nivel de método (para @PreAuthorize)*/
public class SecurityConfig {

    @Value("${auth0.api.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/", "/callback", "/error", "/login").permitAll() // Rutas públicas
                        .requestMatchers("/api/public/**").permitAll() // Endpoints API públicos
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Acceso basado en rol
                        .requestMatchers("/api/data/write").hasAuthority("SCOPE_write:data") // Acceso basado en permiso
                        .anyRequest().authenticated() // Todas las demás solicitudes requieren autenticación
                )
                // Configuración para el login de usuario (frontend)
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/login") // Puedes crear una página de login personalizada si lo deseas
                        .defaultSuccessUrl("/dashboard", true) // Redirige después del login exitoso
                        .failureUrl("/dashboard"))
                // Configuración para el Resource Server (backend API)
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                // Configuración de logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // Redirige después de cerrar sesión
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                // Deshabilita CSRF para APIs si tu frontend es una SPA o si manejas CSRF de
                // otra manera
                // Si usas Thymeleaf y formularios, podrías necesitar habilitar CSRF.
                // Para simplificar al inicio, lo deshabilitamos para APIs.
                .csrf(csrf -> csrf.disable()); // Considera habilitarlo y manejarlo adecuadamente en producción

        return http.build();
    }

    // Configura el convertidor para extraer roles y permisos del JWT
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // El prefijo "SCOPE_" es el predeterminado para los permisos (scopes) de
        // OAuth2.
        // Si tus roles de Auth0 no tienen el prefijo "ROLE_", puedes quitarlo o
        // agregarlo aquí.
        // Por ejemplo, si un rol en Auth0 es "admin", y quieres que Spring lo vea como
        // "ROLE_ADMIN":
        // grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        // Establece el claim JWT que contiene los roles.
        // Debe coincidir con el nombre del claim que definiste en tu Auth0 Action.
        grantedAuthoritiesConverter.setAuthoritiesClaimName(audience + "/roles"); // Ejemplo:
                                                                                  // https://api.recuva.com/roles

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    // Opcional: Validador de JWT para asegurar que el token es para tu API
    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }
}
