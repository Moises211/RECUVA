package com.gt11.RECUVA.Users;

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
public class UserController {

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/users") 
    public String listUsers(Model model) {
        model.addAttribute("users", usersRepository.findAll());
        return "list-users"; // aqui coloquen donde tienen la tabla de lista 
    }

    @GetMapping("/usersNew") 
    public String formNewUser(Model model) {
        model.addAttribute("usuario", new User());
        return "form-user"; // aqui coloquen donde tienen el form para agregar usuarios.
    }

    @PostMapping("/usersNew") 
    public String saveUser(@ModelAttribute User user) {
        usersRepository.save(user);

        return "redirect:/users"; 
    }

    @GetMapping("/editUser/{id}")
    public String formEditUser(Model model, @PathVariable Long id) {
        User usuario = usersRepository.findById(id).get();
        model.addAttribute("usuario", usuario);
        return "form-user"; // aqui coloquen donde tienen el form para agregar usuarios.
    }

    @GetMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable Long id) {
        usersRepository.delete(new User(id));
        return "redirect:/users"; 
    }

}
