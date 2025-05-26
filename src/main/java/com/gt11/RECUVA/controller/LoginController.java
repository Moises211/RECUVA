package com.gt11.RECUVA.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class LoginController {

    /*@GetMapping("/login")
    public RedirectView redirectToAuth0Login() {
        // Redirige a la URL de inicio del flujo OAuth2 provisto por Spring Security
        // "auth0" es el registration ID que definiste en application.properties
        return new RedirectView("/oauth2/authorization/auth0");
    }*/

    // Si tienes una página HTML para mostrar el botón de login, puedes hacer esto
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Asumiendo que tienes un archivo login.html en src/main/resources/templates
    }

}