package com.gt11.RECUVA.Resources;

import com.gt11.RECUVA.Subjects.Subject; // Importar Subject
import com.gt11.RECUVA.Subjects.SubjectsRepository; // Importar SubjectRepository
import com.gt11.RECUVA.Users.User; // Importar User
import com.gt11.RECUVA.Users.UsersRepository; // Importar UserRepository


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // Para proteger rutas
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Para obtener el usuario autenticado
import org.springframework.security.oauth2.core.oidc.user.OidcUser; // Tipo de usuario de Auth0
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // Para capturar los parámetros de visibilidad
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Para streams

@Controller
@RequestMapping("/")
public class ResourceController {

    private final ResourceRepository resourceRepository;
    private final UsersRepository userRepository; // Inyectar UserRepository
    private final SubjectsRepository subjectRepository; // Inyectar SubjectRepository

    // Constructor para inyección de dependencias
    public ResourceController(ResourceRepository resourceRepository, UsersRepository userRepository, SubjectsRepository subjectRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    @GetMapping("/resource")
    public String listResource(Model model, @AuthenticationPrincipal OidcUser oidcUser, @RequestParam(value = "cycle", required = false) String searchCycle) {
        List<Resource> resources;
        boolean isAdmin = false;
        Long currentUserId = null;

        if (oidcUser != null) {
            // Asumiendo que el email de OidcUser es el mismo que en tu User entidad para encontrar el ID
            Optional<User> currentUserOptional = userRepository.findByEmail(oidcUser.getEmail());
            if (currentUserOptional.isPresent()) {
                User currentUser = currentUserOptional.get();
                currentUserId = currentUser.getId();
                // Asumiendo que User.getRole() devuelve Boolean (true para ADMIN)
                isAdmin = currentUser.getRole() != null && currentUser.getRole(); 
            }
        }

        if (isAdmin) {
            // Si es ADMIN, puede ver todos los recursos
            resources = resourceRepository.findAll();
        } else if (currentUserId != null) {
            // Si es un usuario normal (no admin), ve los públicos y los suyos privados
            List<Resource> publicResources = resourceRepository.findByVisibilidad(VisibilityStatus.PUBLIC);
            List<Resource> privateUserResources = resourceRepository.findByVisibilidadAndUser_Id(VisibilityStatus.PRIVATE, currentUserId);
            
            // Combinar y asegurar que no hay duplicados si fuera el caso
            resources = publicResources.stream()
                                       .collect(Collectors.toList());
            privateUserResources.forEach(res -> {
                if (!resources.contains(res)) { // Evitar duplicados
                    resources.add(res);
                }
            });

        } else {
            // Si no está autenticado, solo ve recursos públicos (en este caso, la vista /resource requiere autenticación por SecurityConfig)
            // Sin embargo, si quisieras que esta página fuera accesible para no autenticados, aquí solo mostrarías los públicos.
            // Dada tu SecurityConfig, esta rama solo se alcanzaría si permites /resource para no autenticados.
            resources = resourceRepository.findByVisibilidad(VisibilityStatus.PUBLIC);
        }

        // Aplicar el filtro por ciclo si se proporciona un término de búsqueda

         List<Resource> finalResourcesToShow = resources;
        if (searchCycle != null && !searchCycle.trim().isEmpty()) {
            final String lowerCaseSearchCycle = searchCycle.trim().toLowerCase();
            System.out.println("DEBUG: Aplicando filtro por ciclo: '" + lowerCaseSearchCycle + "'");

            finalResourcesToShow = resources.stream()
                                 .filter(recurso -> {
                                     boolean matches = false;
                                     if (recurso.getSubject() != null && recurso.getSubject().getCiclo() != null) {
                                         String resourceCycle = recurso.getSubject().getCiclo().toLowerCase();
                                         matches = resourceCycle.contains(lowerCaseSearchCycle);
                                         System.out.println("DEBUG: Recurso ID: " + recurso.getId() + 
                                                            ", Ciclo: '" + recurso.getSubject().getCiclo() + 
                                                            "', Coincide: " + matches);
                                     } else {
                                         System.out.println("DEBUG: Recurso ID: " + recurso.getId() + 
                                                            ", No tiene Subject o Ciclo.");
                                     }
                                     return matches;
                                 })
                                 .collect(Collectors.toList());
        } else {
             System.out.println("DEBUG: No hay término de búsqueda por ciclo. Mostrando todos los recursos visibles.");
        }

        model.addAttribute("resources", resources);
        // También pasa si el usuario es admin a la vista para la visibilidad de botones
        model.addAttribute("isAdmin", isAdmin); 
        model.addAttribute("currentUserId", currentUserId); // Pasa el ID del usuario actual
        model.addAttribute("currentSearchCycle", searchCycle); 
        return "list-resource"; // Devuelve la plantilla de la lista de recursos
    }

    @GetMapping("/resourceNew")
    @PreAuthorize("isAuthenticated()") // Solo usuarios autenticados pueden acceder al formulario de nuevo recurso
    public String formNewResource(Model model) {
        model.addAttribute("recurso", new Resource());
        model.addAttribute("subjects", subjectRepository.findAll()); // Para poblar el dropdown de materias
        // También pasamos los valores posibles para el enum de visibilidad
        model.addAttribute("visibilityOptions", VisibilityStatus.values()); 
        return "form-resource"; // Retorna la plantilla del formulario para agregar recursos.
    }

    @PostMapping("/resourceNew")
    @PreAuthorize("isAuthenticated()") // Solo usuarios autenticados pueden guardar un nuevo recurso
    public String saveResource(@ModelAttribute("recurso") Resource resource,
                               @RequestParam("visibilidad") String visibilidadString, // Captura la visibilidad como String
                               @AuthenticationPrincipal OidcUser oidcUser,
                               RedirectAttributes redirectAttributes) {
        try {
            // Asigna el usuario que subió el recurso
            if (oidcUser != null) {
                Optional<User> currentUserOptional = userRepository.findByEmail(oidcUser.getEmail());
                if (currentUserOptional.isPresent()) {
                    resource.setUser(currentUserOptional.get());
                } else {
                    // Manejar caso donde el usuario no se encuentra en la BD (debería existir si está logueado)
                    redirectAttributes.addFlashAttribute("errorMessage", "No se pudo identificar al usuario. Recurso no guardado.");
                    return "redirect:/resourceNew";
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuario no autenticado. Recurso no guardado.");
                return "redirect:/resourceNew";
            }

            resource.setFechaCreacion(LocalDate.now()); // Establece la fecha de creación automáticamente
            resource.setVisibilidad(VisibilityStatus.valueOf(visibilidadString.toUpperCase())); // Convierte String a Enum

            resourceRepository.save(resource);
            redirectAttributes.addFlashAttribute("successMessage", "Recurso agregado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el recurso: " + e.getMessage());
        }
        return "redirect:/resource";
    }

    @GetMapping("/editResource/{id}")
    @PreAuthorize("hasRole('ADMIN') or @resourceController.isOwner(#id, authentication)") // Solo ADMIN o propietario puede editar
    public String formEditResource(Model model, @PathVariable Long id, @AuthenticationPrincipal OidcUser oidcUser) {
        Resource recurso = resourceRepository.findById(id).orElse(null); // Usar orElse(null) o throw exception
        if (recurso == null) {
            // Manejar recurso no encontrado
            return "redirect:/resource"; // O a una página de error 404
        }
        model.addAttribute("recurso", recurso);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("visibilityOptions", VisibilityStatus.values());
        return "form-resource"; // Retorna la plantilla del formulario para editar recursos.
    }

    @PostMapping("/editResource/{id}")
    @PreAuthorize("hasRole('ADMIN') or @resourceController.isOwner(#id, authentication)") // Proteger la actualización
    public String updateResource(@PathVariable Long id, @ModelAttribute("recurso") Resource updatedResource,
                                @RequestParam("visibilidad") String visibilidadString,
                                @AuthenticationPrincipal OidcUser oidcUser,
                                RedirectAttributes redirectAttributes) {
        try {
            Resource existingResource = resourceRepository.findById(id).orElseThrow(() -> new RuntimeException("Recurso no encontrado para actualizar"));

            // Actualiza solo los campos modificables del recurso existente
            existingResource.setTitulo(updatedResource.getTitulo());
            existingResource.setDescripcion(updatedResource.getDescripcion());
            existingResource.setTipoDoc(updatedResource.getTipoDoc());
            existingResource.setUrl(updatedResource.getUrl());
            existingResource.setSubject(updatedResource.getSubject()); // Asegúrate de que el Subject ID se mapea correctamente

            // Actualiza la visibilidad
            existingResource.setVisibilidad(VisibilityStatus.valueOf(visibilidadString.toUpperCase()));

            // El usuario que lo subió y la fecha de creación no deben cambiar en una edición, solo en la creación
            // existingResource.setUser(...)
            // existingResource.setFechaCreacion(...)

            resourceRepository.save(existingResource);
            redirectAttributes.addFlashAttribute("successMessage", "Recurso actualizado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el recurso: " + e.getMessage());
        }
        return "redirect:/resource";
    }


    // Método para la lógica de autorización personalizada para "isOwner"
    // Esto es para que Spring Security lo use en @PreAuthorize
    public boolean isOwner(Long resourceId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser)) {
            return false;
        }
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        
        Optional<User> currentUserOptional = userRepository.findByEmail(oidcUser.getEmail());
        if (!currentUserOptional.isPresent()) {
            return false;
        }
        Long currentUserId = currentUserOptional.get().getId();

        Optional<Resource> resourceOptional = resourceRepository.findById(resourceId);
        return resourceOptional.map(resource -> resource.getUser() != null && resource.getUser().getId().equals(currentUserId))
                               .orElse(false);
    }


    @GetMapping("/deleteResource/{id}") // Este es el endpoint que la vista de recursos usa
    @PreAuthorize("hasRole('ADMIN') or @resourceController.isOwner(#id, authentication)") // Solo ADMIN o propietario
    public String deleteResource(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (resourceRepository.existsById(id)) {
                resourceRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Recurso eliminado exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Error: Recurso no encontrado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el recurso: " + e.getMessage());
        }
        return "redirect:/resource";
    }

    // Nuevo método para eliminar recurso desde la vista de reportes (si lo tenías en ReportController)
    @PostMapping("/resources/delete-from-report/{resourceId}")
    @PreAuthorize("hasRole('ADMIN') or @resourceController.isOwner(#resourceId, authentication)")
    public String deleteResourceFromReports(@PathVariable Long resourceId, RedirectAttributes redirectAttributes) {
        try {
            if (resourceRepository.existsById(resourceId)) {
                resourceRepository.deleteById(resourceId); // Asumiendo cascada para eliminar reportes/ratings
                redirectAttributes.addFlashAttribute("successMessage", "Recurso y sus elementos asociados eliminados exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Error: Recurso no encontrado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el recurso desde reportes: " + e.getMessage());
        }
        return "redirect:/reports"; // Redirige de vuelta a la lista de reportes
    }
}

/*
package com.gt11.RECUVA.Resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/")
public class ResourceController {

    @Autowired
    private ResourceRepository resourceRepository;

    @GetMapping("/resource")
    public String listResource(Model model) {
        model.addAttribute("resources", resourceRepository.findAll());
        return "list-resource"; // aqui coloquen donde tienen la tabla de lista
    }

    @GetMapping("/resourceNew")
    public String formNewResource(Model model) {
        model.addAttribute("recurso", new Resource());        
        return "form-resource"; // aqui coloquen donde tienen el form para agregar recursos.
    }

    @PostMapping("/resourceNew")
    public String saveResource(@ModelAttribute Resource resource) {
        resourceRepository.save(resource);

        return "redirect:/resource";
    }

    @GetMapping("/editResource/{id}")
    public String formEditResource(Model model, @PathVariable Long id) {

        Resource recurso = resourceRepository.findById(id).get();
        model.addAttribute("recurso", recurso);
        return "form-resource";// aqui coloquen donde tienen el form para agregar recursos.
    }

    @GetMapping("/deleteResource/{id}")
    public String deleteResource(@PathVariable Long id) {
        resourceRepository.delete(new Resource(id));
        return "redirect:/resource";
    }    
}
*/