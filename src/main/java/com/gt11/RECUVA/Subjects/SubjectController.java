package com.gt11.RECUVA.Subjects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class SubjectController {
    @Autowired
    private SubjectsRepository subjectsRepository;

    @GetMapping("/subject") 
    public String listSubject(Model model) {
        model.addAttribute("subject", subjectsRepository.findAll());
        return "list-subject"; // aqui coloquen donde tienen la tabla de lista 
    }

    @GetMapping("/subjectNew") 
    public String formNewSubject(Model model) {
        model.addAttribute("materia", new Subject());
        return "form-subject"; // aqui coloquen donde tienen el form para agregar materias.
    }

    @PostMapping("/subjectNew") 
    public String saveSubject(@ModelAttribute Subject subject) {
        subjectsRepository.save(subject);

        return "redirect:/subject"; 
    }

    @GetMapping("/editSubject/{id}")
    public String formEditSubject(Model model, @PathVariable Long id) {
        Subject materia = subjectsRepository.findById(id).get();
        model.addAttribute("materia", materia);
        return "form-subject"; // aqui coloquen donde tienen el form para agregar materias.
    }

    @GetMapping("/deleteSubject/{id}")
    public String deleteSubject(@PathVariable Long id) {
        subjectsRepository.delete(new Subject(id));
        return "redirect:/subject"; 
    }
}
