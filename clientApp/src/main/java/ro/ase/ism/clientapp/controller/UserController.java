package ro.ase.ism.clientapp.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ro.ase.ism.clientapp.entity.Bank;
import ro.ase.ism.clientapp.entity.User;
import ro.ase.ism.clientapp.service.BankService;
import ro.ase.ism.clientapp.service.FabricService;
import ro.ase.ism.clientapp.service.UserService;

import java.lang.reflect.Array;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Log4j2
public class UserController {


    private final UserService userService;
    private final BankService bankService;
    private final FabricService fabricService;

    public UserController(UserService userService, BankService bankService, FabricService fabricService) {
        this.userService = userService;
        this.bankService = bankService;
        this.fabricService = fabricService;
    }


    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());

        if (!model.containsAttribute("cardConnected")) {
            model.addAttribute("cardConnected", false);

            if(model.containsAttribute("port")) log.info("port");
        }

        boolean kycStatus = user.getBank()!=null && fabricService.checkEnrollment(user.getBank(), user.getEmail());

        if(kycStatus)
        {
            model.addAttribute("kycStatus", true);
            model.addAttribute("userBank", user.getBank());
            model.addAttribute("smartContracts", Arrays.asList("Contract1","Contract2","Contract3"));
        }
        else{
            List<Bank> banks = bankService.findAll();
            model.addAttribute("banks", banks);
            model.addAttribute("kycStatus", false);
        }
        return "home";
    }

    @PostMapping("/enroll")
    public String enroll(@RequestParam String bank, @RequestParam String name, @RequestParam String address, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        fabricService.enrollKYC(bank, user.getId().toString(), name, user.getEmail(), address);
        return "redirect:/";
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
        userService.registerUser(user);
        return "redirect:/login";
    }

}
