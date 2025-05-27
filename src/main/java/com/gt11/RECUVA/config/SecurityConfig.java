package com.gt11.RECUVA.config;

// src/main/java/com/gt11/RECUVA/config/SecurityConfiguration.java


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Inyecta tu servicio personalizado
    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(  "/callback", "/error", "/login", "/logo.jpg").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN") // Asegúrate de que esta regla exista para tu Thymeleaf o controlador
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login") // Si tienes una página de login personalizada
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService) // ¡Aquí se usa tu servicio personalizado!
                )
                .defaultSuccessUrl("/dashboard", true) // Redirige al dashboard después del login exitoso
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .permitAll()
            );
        return http.build();
    }
}

/*import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map; // Asegúrate de importar Map si no lo tienes

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita la seguridad a nivel de método (para @PreAuthorize)
public class SecurityConfig {

   private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/", "/callback", "/error", "/login", "/logo.jpg").permitAll() // Agregamos /logo.jpg si es un recurso público
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/data/write").hasAuthority("SCOPE_write:data")
                        .requestMatchers("/resources/add").hasRole("ADMIN")
                        .requestMatchers("/resources/view").authenticated()
                        .anyRequest().authenticated()
                )
                // Configuración para el login de usuario (frontend)
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/dashboard")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.oidcUserService()) // <-- ¡AQUÍ ESTÁ LA CLAVE! Usamos el servicio OIDC personalizado
                        )
                )
                // Configuración para el Resource Server (backend API)
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                // Configuración de logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // Este Bean es para el Resource Server, cuando se valida un Access Token.
    // No afecta el "hasRole" en el frontend (Thymeleaf), que se basa en el OidcUser de la sesión.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // **CORRECCIÓN AQUÍ:**
        // El claim de roles en Auth0 usualmente usa el issuer (tu dominio Auth0) como namespace.
        // No debes concatenar el 'audience' con el URL del claim.
        // Debe ser el 'issuer' (ej. https://dev-ygztsimsgp6ywe30.us.auth0.com/) + el nombre del claim (ej. "roles")
        grantedAuthoritiesConverter.setAuthoritiesClaimName(issuer + "roles"); // Asumiendo que el claim es como "https://your-domain/roles"

        // Si tus roles en Auth0 ya vienen con "ROLE_" (ej. "ROLE_ADMIN"), puedes quitar el prefijo por defecto de Spring:
        // grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    // **¡NUEVO/CORREGIDO BEAN IMPORANTE PARA EL LOGIN DE FRONTEND!**
    // Este servicio carga el usuario OIDC y extrae los roles del ID Token.
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService(); // Servicio OIDC por defecto de Spring Security

        return (userRequest) -> {
            OidcUser oidcUser = delegate.loadUser(userRequest); // Carga el usuario OIDC (con claims y scopes estándar)

            List<String> roles = new ArrayList<>();
            // **CORRECCIÓN AQUÍ:**
            // Construye el nombre del claim de roles usando el `issuer`
            String rolesClaimName = issuer + "roles"; // Esto resultaría en algo como "https://dev-ygztsimsgp6ywe30.us.auth0.com/roles"

            // Intenta extraer los roles del ID Token (claims).
            // La Regla/Acción de Auth0 DEBE poner los roles en este claim.
            if (oidcUser.getClaims().containsKey(rolesClaimName)) {
                roles = oidcUser.getClaimAsStringList(rolesClaimName);
            } else if (oidcUser.getClaims().containsKey("roles")) {
                // Fallback: si por alguna razón los roles se envían sin namespace (solo "roles")
                roles = oidcUser.getClaimAsStringList("roles");
            }
            // Puedes añadir más fallbacks si sabes de otros claims donde podrían estar los roles

            Collection<GrantedAuthority> mappedAuthorities = new ArrayList<>();
            // Añade las autoridades (scopes) predeterminadas que Auth0 envía (ej. SCOPE_openid, SCOPE_profile)
            mappedAuthorities.addAll(oidcUser.getAuthorities());

            // Mapea los roles extraídos a las autoridades de Spring Security con el prefijo "ROLE_"
            if (roles != null) {
                for (String role : roles) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                }
            }

            // Retorna un nuevo DefaultOidcUser con todas las autoridades combinadas
            // El último argumento "name" indica qué claim usar como nombre principal del usuario
            return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "name");
        };
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
}*/