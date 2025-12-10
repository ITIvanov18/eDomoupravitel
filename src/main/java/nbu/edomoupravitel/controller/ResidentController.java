package nbu.edomoupravitel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.ResidentDto;
import nbu.edomoupravitel.service.ResidentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @PostMapping("/create")
    public String createResident(@Valid @ModelAttribute("resident") ResidentDto residentDto) {
        residentService.createResident(residentDto);
        return "redirect:/apartments";
    }

    @DeleteMapping("/{id}")
    public String deleteResident(@PathVariable Long id) {
        residentService.deleteResident(id);
        return "redirect:/apartments";
    }

    @PostMapping("/{id}/toggle-owner")
    public String toggleOwner(@PathVariable Long id) {
        residentService.toggleOwner(id);
        return "redirect:/apartments";
    }
}
