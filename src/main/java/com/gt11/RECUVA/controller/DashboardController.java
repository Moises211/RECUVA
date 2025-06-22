package com.gt11.RECUVA.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OidcUser principal, Model model) {
        if (principal != null) {
            // Obtener el nombre del usuario (puedes ajustar 'name' por 'nickname', 'preferred_username', etc. según tus claims de Auth0)
            String userName = principal.getClaim("name");
            if (userName == null || userName.isEmpty()) {
                userName = principal.getClaim("nickname"); // Fallback al email si el nombre no está disponible
            }
            if (userName == null || userName.isEmpty()) {
                 userName = principal.getEmail(); // Otro fallback si el nombre no está disponible
            }
            if (userName == null || userName.isEmpty()) {
                userName = principal.getSubject(); // Último recurso, el ID del usuario
            }
            model.addAttribute("userName", userName);

            // También puedes añadir los roles al modelo si quisieras mostrarlos explícitamente, aunque Thymeleaf los usa directamente
            // Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
            // String roles = authorities.stream()
            //        .map(GrantedAuthority::getAuthority)
            //        .collect(Collectors.joining(", "));
            // model.addAttribute("userRoles", roles);

        }
        return "dashboard"; // Retorna el nombre de tu archivo HTML (dashboard.html)
    }

    // Opcional: Controladores para las nuevas rutas, solo para mostrar un mensaje por ahora.
    // Esto se podría convertir en controladores reales que manejen la lógica de ver/añadir recursos.
    @GetMapping("/resources/view")
    public String viewResources(Model model) {
        model.addAttribute("message", "Esta es la página para ver todos los recursos.");
        return "generic_message"; // Necesitarás crear generic_message.html
    }

    @GetMapping("/resources/add")
    public String addResource(Model model) {
        model.addAttribute("message", "Esta es la página para añadir un nuevo recurso. Solo para ADMINS.");
        return "generic_message"; // Necesitarás crear generic_message.html
    }
}