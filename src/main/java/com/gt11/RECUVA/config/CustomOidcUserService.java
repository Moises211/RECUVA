// src/main/java/com/gt11/RECUVA/config/CustomOidcUserService.java
package com.gt11.RECUVA.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Importa el logger de SLF4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service // Asegúrate de que esta anotación esté presente
public class CustomOidcUserService extends OidcUserService {

    private static final String ROLES_CLAIM = "https://dev-ygztsimsgp6ywe30.us.auth0.com/roles";
    // Declara el logger
    private static final Logger log = LoggerFactory.getLogger(CustomOidcUserService.class);

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Llama al método de la clase padre para obtener el OidcUser base
        OidcUser oidcUser = super.loadUser(userRequest);

        log.debug("--- CustomOidcUserService: Iniciando procesamiento de usuario ---");
        log.debug("Authorities iniciales de OidcUser: {}", oidcUser.getAuthorities());
        log.debug("Claims iniciales de OidcUser (userinfo): {}", oidcUser.getClaims());

        // Obtener todos los claims del ID Token (esto es crucial)
        Map<String, Object> idTokenClaims = oidcUser.getIdToken().getClaims();
        log.debug("Todos los claims del ID Token recibido: {}", idTokenClaims);

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

        // Intenta obtener el claim de roles del ID Token
        Object rolesClaim = idTokenClaims.get(ROLES_CLAIM);

        log.debug("Intentando obtener el claim de roles: {}", ROLES_CLAIM);
        log.debug("Valor crudo del claim de roles del ID Token: {}", rolesClaim);

        if (rolesClaim instanceof List) {
            List<String> roles = (List<String>) rolesClaim;
            log.debug("Claim de roles encontrado y es una Lista: {}", roles);
            roles.stream()
                    .map(role -> "ROLE_" + role.toUpperCase()) // Convertir a formato Spring Security
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
            log.debug("Roles añadidos a las autoridades: {}",
                    roles.stream().map(role -> "ROLE_" + role.toUpperCase()).collect(Collectors.toList()));
        } else if (rolesClaim != null) {
            log.warn("El claim de roles no es una Lista, es de tipo: {}", rolesClaim.getClass().getName());
        } else {
            log.info("El claim de roles '{}' NO fue encontrado en el ID Token.", ROLES_CLAIM);
        }

        log.debug("Authorities finales después del CustomOidcUserService: {}", authorities);
        log.debug("--- CustomOidcUserService: Finalizando procesamiento de usuario ---");

        // Imprimir el ID Token completo (solo para depuración, no en producción)
        String rawIdToken = userRequest.getIdToken().getTokenValue();
        log.info("Raw ID Token received: {}", rawIdToken);

        // Imprimir todos los claims del ID Token para ver qué está llegando
        Map<String, Object> claims = oidcUser.getIdToken().getClaims();
        log.info("All ID Token Claims received:");
        claims.forEach((key, value) -> log.info("  Claim: {} = {}", key, value));

        // Retornar un nuevo DefaultOidcUser con las autoridades actualizadas
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}