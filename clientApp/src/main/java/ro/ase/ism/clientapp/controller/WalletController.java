package ro.ase.ism.clientapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WalletController {

    public String connect(@RequestParam String host, @RequestParam String port, Model model)
    {
        return "redirect:/home";
    }

}
