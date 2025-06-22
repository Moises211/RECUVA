package com.gt11.RECUVA.Reports; // Es importante que el paquete sea 'controller'

import com.gt11.RECUVA.Reports.Report;
import com.gt11.RECUVA.Reports.ReportRepository;
import com.gt11.RECUVA.Resources.Resource; // Asegúrate de importar tu entidad Resource
import com.gt11.RECUVA.Resources.ResourceRepository; // Y tu ResourceRepository, si vas a asociar el reporte a un recurso
import com.gt11.RECUVA.Users.User;
import com.gt11.RECUVA.Users.UsersRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/") // Un RequestMapping general para el controlador de reportes
public class ReportController {

    private final ReportRepository reportRepository;
    private final UsersRepository userRepository;
    private final ResourceRepository resourceRepository; // Inyectar ResourceRepository si asocias reportes a recursos

    // Constructor para inyección de dependencias
    public ReportController(ReportRepository reportRepository, UsersRepository userRepository,
            ResourceRepository resourceRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository; // Inicializar
    }

    // --- Endpoints para la GESTIÓN de Reportes (Ejemplo: Accesible solo por ADMIN)
    // ---

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')") // Protege esta ruta solo para ADMINS
    public String listReports(Model model) {
        model.addAttribute("reports", reportRepository.findAll()); // Pasa la lista de reportes al modelo
        return "list-report"; // Nombre de tu vista Thymeleaf para listar reportes
    }

    @GetMapping("/new") // Ruta para mostrar formulario de nuevo reporte (página completa)
    @PreAuthorize("hasRole('ADMIN')") // Protege esta ruta solo para ADMINS
    public String formNewReport(Model model) {
        model.addAttribute("reporte", new Report());
        // Aquí podrías añadir otros atributos necesarios para el formulario,
        // como una lista de recursos si el reporte es sobre uno específico
        return "form-report"; // Nombre de tu vista Thymeleaf para el formulario completo
    }

    @PostMapping("/new") // Ruta para guardar un nuevo reporte desde un formulario de página completa
    @PreAuthorize("hasRole('ADMIN')") // Protege esta ruta solo para ADMINS
    public String saveReport(@ModelAttribute Report report, RedirectAttributes redirectAttributes) {
        // En este escenario (formulario de página completa), si el campo 'user' no
        // viene del formulario,
        // necesitamos obtener el usuario de sesión aquí también, si no viene de otra
        // forma.
        // Asumiendo que el formulario de reporte de página completa NO tiene un campo
        // para seleccionar el usuario.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String userEmail = oidcUser.getEmail();
            userRepository.findByEmail(userEmail).ifPresent(report::setUser); // Asigna el usuario a la entidad Report
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: No se pudo identificar al usuario para el reporte.");
            return "redirect:/reports/new";
        }

        // La fecha se establecerá automáticamente por el método @PrePersist en la
        // entidad Report
        reportRepository.save(report);
        redirectAttributes.addFlashAttribute("successMessage", "Reporte creado exitosamente.");
        return "redirect:/reports"; // Redirige a la lista de reportes
    }

    @GetMapping("/edit/{id}") // Ruta para mostrar formulario de edición de reporte
    @PreAuthorize("hasRole('ADMIN')") // Protege esta ruta solo para ADMINS
    public String formEditReport(Model model, @PathVariable Long id) {
        Report reporte = reportRepository.findById(id).orElse(null); // Usar orElse(null) para un manejo seguro
        if (reporte == null) {
            // Manejar caso donde el reporte no existe
            return "redirect:/reports"; // O redirigir a una página de error
        }
        model.addAttribute("reporte", reporte);
        return "form-report"; // Reutiliza el formulario para edición
    }

    @PostMapping("/edit/{id}") // Ruta para guardar cambios de un reporte editado
    @PreAuthorize("hasRole('ADMIN')")
    public String updateReport(@PathVariable Long id, @ModelAttribute Report updatedReport,
            RedirectAttributes redirectAttributes) {
        return reportRepository.findById(id).map(existingReport -> {
            existingReport.setMotivo(updatedReport.getMotivo());
            // No actualizar user ni fecha aquí, a menos que sea una funcionalidad explícita
            // existingReport.setResource(updatedReport.getResource()); // Si el recurso
            // también se puede editar

            reportRepository.save(existingReport);
            redirectAttributes.addFlashAttribute("successMessage", "Reporte actualizado exitosamente.");
            return "redirect:/reports";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Reporte no encontrado para actualizar.");
            return "redirect:/reports";
        });
    }

    @GetMapping("/delete/{id}") // <-- ¡CAMBIA A @GetMapping!
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reportRepository.delete(new Report(id));
        return "redirect:/reports";
    }

    // Si tienes este método para eliminar recursos desde reportes:
    // ANTES: @PostMapping("/resources/delete-from-report/{resourceId}")
    @GetMapping("/resource/delete-from-report/{resourceId}") // <-- ¡CAMBIA A @GetMapping!
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteResourceFromReport(@PathVariable Long resourceId, RedirectAttributes redirectAttributes) {
        resourceRepository.delete(new Resource(resourceId));
        return "redirect:/reports"; // O donde quieras redirigir
    }

    // --- Endpoint para el SUBMIT del MODAL de Reporte desde la vista de recursos
    // ---

    @PostMapping("/submit-from-modal") // Ruta específica para el envío desde el modal
    @PreAuthorize("isAuthenticated()") // Solo usuarios autenticados pueden enviar reportes desde el modal
    public String submitReportFromModal(@RequestParam("motive") String motive, // Nombre del campo en el HTML del modal
            @RequestParam(name = "resourceId", required = false) Long resourceId, // Opcional, si reportas un recurso
                                                                                  // específico
            RedirectAttributes redirectAttributes) {

        // 1. Obtener el usuario autenticado de la sesión de Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof OidcUser)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe iniciar sesión para enviar un reporte.");
            return "redirect:/login"; // Redirige al login si no está autenticado
        }

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String userEmail = oidcUser.getEmail(); // Obtiene el email del usuario de Auth0

        if (userEmail == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No se pudo obtener el email del usuario autenticado.");
            return "redirect:/resource"; // Redirige a la vista de materias con un mensaje
        }

        // 2. Buscar el usuario en tu base de datos local usando el email de Auth0
        Optional<User> userOptional = userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: Su usuario no está registrado en la base de datos local para enviar reportes.");
            return "redirect:/resource"; // Redirige a la vista de materias con un mensaje
        }

        User reportingUser = userOptional.get();

        // 3. Crear y guardar el reporte
        Report report = new Report();
        report.setMotivo(motive); // Establece el motivo capturado del formulario
        report.setUser(reportingUser); // Asigna el usuario que envió el reporte
        // La fecha se establecerá automáticamente por @PrePersist en la entidad Report

        // Si se proporciona un ID de recurso, búscalo y asignalo
        if (resourceId != null) {
            Optional<Resource> resourceOptional = resourceRepository.findById(resourceId);
            resourceOptional.ifPresent(report::setResource); // Asigna el recurso si se encuentra
        }

        reportRepository.save(report);

        redirectAttributes.addFlashAttribute("successMessage", "Reporte enviado con éxito. Gracias por tu ayuda!");
        return "redirect:/resource"; // Redirige de nuevo a la vista de materias
    }
}