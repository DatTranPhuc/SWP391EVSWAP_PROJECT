package evswap.swp391to4.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/stations")
@RequiredArgsConstructor
public class StationController {

    @GetMapping
    public String redirectToReservation(@RequestParam(value = "name", required = false) String name,
                                        RedirectAttributes redirectAttributes) {
        if (name != null && !name.isBlank()) {
            redirectAttributes.addAttribute("q", name);
        }
        return "redirect:/reservations/schedule";
    }

    @GetMapping("/{id}")
    public String redirectStationDetails(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("stationId", id);
        return "redirect:/reservations/book";
    }
}
