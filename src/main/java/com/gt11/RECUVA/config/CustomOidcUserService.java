// --- Paso 2: Servicio de Usuario OIDC Personalizado (CustomOidcUserService.java) ---
package com.gt11.RECUVA.config;

import com.gt11.RECUVA.Users.User; // ¡Importa tu entidad User existente!
import com.gt11.RECUVA.Users.UsersRepository; // ¡Importa tu UserRepository existente!
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOidcUserService.class);

    private final UsersRepository userRepository; // Usa tu UserRepository existente

    // Inyecta tu UserRepository
    public CustomOidcUserService(UsersRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Primero, carga el usuario OIDC estándar desde Auth0
        OidcUser oidcUser = super.loadUser(userRequest);

        log.info("--- CustomOidcUserService: Procesando usuario desde Auth0 ---");
        log.info("Email del usuario autenticado por Auth0: {}", oidcUser.getEmail());
        log.info("Authorities iniciales de OidcUser (de Auth0): {}", oidcUser.getAuthorities());

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

        // Obtén el email del usuario de Auth0
        String userEmail = oidcUser.getEmail();

        if (userEmail != null) {
            // Busca el usuario en tu base de datos local por email usando tu UserRepository
            Optional<User> localDbUserOptional = userRepository.findByEmail(userEmail);

            if (localDbUserOptional.isPresent()) {
                User localDbUser = localDbUserOptional.get();
                Boolean isAdmin = localDbUser.getRole(); // Obtiene el valor booleano del campo 'role'

                String roleName;
                if (isAdmin != null && isAdmin) { // Si es true (ADMIN)
                    roleName = "ADMIN";
                } else { // Si es false o null (se trata como USER)
                    roleName = "USER";
                }
                log.info("Usuario local encontrado con email '{}'. Se asigna el rol: {}", userEmail, roleName);

                // Convierte el rol local a un rol de Spring Security (ej. "ADMIN" -> "ROLE_ADMIN")
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
            } else {
                log.warn("Usuario con email '{}' autenticado por Auth0 NO encontrado en la base de datos local. No se asignarán roles locales basados en DB.", userEmail);
                // Opcional: Si el usuario no existe en tu DB local, podrías asignarle un rol predeterminado
                // por ejemplo, siempre ROLE_USER si no está en la DB:
                // authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
        } else {
            log.error("El email del usuario no está disponible en el ID Token de Auth0. No se pueden buscar roles locales en la DB.");
        }

        log.info("Authorities finales después de combinar con roles locales: {}", authorities);
        log.info("--- CustomOidcUserService: Finalizado ---");

        // Retorna un nuevo DefaultOidcUser con las autoridades combinadas
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}