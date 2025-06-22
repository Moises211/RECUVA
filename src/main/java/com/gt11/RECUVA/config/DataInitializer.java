package com.gt11.RECUVA.config;

// Puedes añadir esto como un @Bean CommandLineRunner en tu RecuvaApplication.java
// O ejecutar directamente en tu DB.

import com.gt11.RECUVA.Users.User; // Importa tu entidad User existente
import com.gt11.RECUVA.Users.UsersRepository; // Importa tu UserRepository existente
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer { // Un nombre de clase diferente para claridad

    @Bean
    public CommandLineRunner initLocalUsersForRoles(UsersRepository userRepository) {
        return args -> {
            // Usuario con rol ADMIN (role = true)
            String adminEmail = "recubatutor@gmail.com"; // Email que usas para Auth0
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User adminUser = new User();
                adminUser.setEmail(adminEmail);
                adminUser.setName("Admin User"); // Puedes poner un nombre si lo necesitas
                adminUser.setPass("N/A"); // Contraseña no usada para login OIDC, pero JPA puede requerirla non-null
                adminUser.setRole(true); // true para ADMIN
                userRepository.save(adminUser);
                System.out.println("Usuario '" + adminEmail + "' con rol ADMIN (DB local) creado.");
            }

            // Usuario con rol USER (role = false)
            String regularUserEmail = "otro@example.com"; // Otro email de Auth0 de prueba
            if (userRepository.findByEmail(regularUserEmail).isEmpty()) {
                User regularUser = new User();
                regularUser.setEmail(regularUserEmail);
                regularUser.setName("Regular User");
                regularUser.setPass("N/A");
                regularUser.setRole(false); // false para USER
                userRepository.save(regularUser);
                System.out.println("Usuario '" + regularUserEmail + "' con rol USER (DB local) creado.");
            }
            // Asegúrate de que estos emails también existan y estén verificados en Auth0.
        };
    }
}