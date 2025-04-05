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
