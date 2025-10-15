package evswap.swp391to4.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminConsoleController {

    @GetMapping("/console")
    public String renderConsole() {
        return "admin-console";
    }
}
