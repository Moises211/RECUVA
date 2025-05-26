package com.gt11.RECUVA.controller; // Ajusta el paquete según tu estructura

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Obtener la información de autenticación del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            // Obtener el nombre del usuario (normalmente del 'name' o 'preferred_username' del principal)
            String userName = oauthToken.getPrincipal().getAttribute("name");
            if (userName == null) {
                // Si 'name' no está disponible, intenta con 'email' o 'preferred_username'
                userName = oauthToken.getPrincipal().getAttribute("email");
            }
            if (userName == null) {
                userName = oauthToken.getPrincipal().getName(); // Último recurso, el nombre principal
            }

            model.addAttribute("userName", userName);
            model.addAttribute("userEmail", oauthToken.getPrincipal().getAttribute("email"));
            // Puedes añadir más atributos si los necesitas del principal, como avatar, etc.
            // model.addAttribute("userPicture", oauthToken.getPrincipal().getAttribute("picture"));

        } else {
            // Esto es para usuarios que no inician sesión con OAuth2, si tienes otros métodos.
            model.addAttribute("userName", authentication.getName());
            model.addAttribute("userEmail", "N/A (No OAuth2)");
        }

        return "dashboard"; // Esto buscará el archivo dashboard.html en src/main/resources/templates
    }

    @GetMapping("/") // Para que la raíz '/' sea accesible y redirija al dashboard si está autenticado
    public String root() {
        return "redirect:/dashboard"; // Redirige a /dashboard si se accede a la raíz
    }
}
