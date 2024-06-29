package ro.ase.ism.clientapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ro.ase.ism.clientapp.entity.User;
import ro.ase.ism.clientapp.repo.UserRepository;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/home")
    public String home(Model model, Principal principal) {



        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        //boolean cardConnected = javacardService.isCardConnected();
        boolean cardConnected = false;
        //BigDecimal balance = cardConnected ? javacardService.getBalance(user.getRippleAddress()) : BigDecimal.ZERO;
        BigDecimal balance = BigDecimal.ZERO;

        model.addAttribute("username", user.getUsername());
        model.addAttribute("cardConnected", cardConnected);
        model.addAttribute("balance", balance);
        //model.addAttribute("banks", kycService.getAvailableBanks());

        //banks.add("BankA");
        //banks.add("BankB");
        //banks.add("BankC");
        model.addAttribute("banks", Arrays.asList("Bank A", "Bank B", "Bank C"));
        model.addAttribute("kycStatus", false);
        return "home";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login";
    }

}
