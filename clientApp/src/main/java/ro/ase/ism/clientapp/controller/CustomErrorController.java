package ro.ase.ism.clientapp.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // Retrieve the error status code
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // Check if the status code is 404
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == 404) {
                // Redirect to home if the status code is 404
                return "redirect:/home";
            }
        }

        // Otherwise, display a custom error page or redirect to home
        return "redirect:/home";
    }

}