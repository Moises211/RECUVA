package com.gt11.RECUVA.Ratings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gt11.RECUVA.Users.UserController;
import com.gt11.RECUVA.Users.UsersRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;





@Controller
@RequestMapping("/")
public class RatingsController {

    @Autowired
    private RatingsRepository ratingsRepository;
    @Autowired
    private UserController userController;
    //private UsersRepository usersRepository;

    @GetMapping("/ratings")
    public String listRatings(Model model) {
        model.addAttribute("ratings", ratingsRepository.findAll());
        return "list-ratings";
    }
    
    @GetMapping("/ratingsNew")
    public String formNewRatings(Model model) {
        //model.addAttribute("users", usersRepository.findAll());
        model.addAttribute("ratings", new Ratings());
        model.addAttribute("users", userController.usersList());
        
        return "form-ratings";
    }
    
    @PostMapping("/ratingsNew")
    public String saveRatings(@ModelAttribute Ratings ratings) {
        ratingsRepository.save(ratings);
        
        return "redirect:/ratings";
    }

    @GetMapping("/editRatings/{id}")
    public String getMethodName(Model model, @PathVariable Long id) {
        Ratings ratings = ratingsRepository.findById(id).get();
        model.addAttribute("ratings", ratings);
        model.addAttribute("users", userController.usersList());
        //model.addAttribute("users", usersRepository.findAll());
        return "form-ratings";
    }
    
    @GetMapping("/deleteRatings/{id}")
    public String deleteRatings(@PathVariable Long id) {
        ratingsRepository.delete(new Ratings(id));
        return "redirect:/ratings";
    }
    

}
