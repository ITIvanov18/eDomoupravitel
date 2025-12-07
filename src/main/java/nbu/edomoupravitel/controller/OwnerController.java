package nbu.edomoupravitel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.OwnerDto;
import nbu.edomoupravitel.service.OwnerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping
    public String listOwners(Model model) {
        model.addAttribute("owners", ownerService.getAllOwners());
        return "owners/list";
    }

    @GetMapping("/create")
    public String createOwnerForm(Model model) {
        model.addAttribute("owner", new OwnerDto());
        return "owners/form";
    }

    @PostMapping("/create")
    public String createOwner(@Valid @ModelAttribute("owner") OwnerDto ownerDto, BindingResult result) {
        if (result.hasErrors()) {
            return "owners/form";
        }
        ownerService.createOwner(ownerDto);
        return "redirect:/owners";
    }

    @GetMapping("/edit/{id}")
    public String editOwnerForm(@PathVariable Long id, Model model) {
        model.addAttribute("owner", ownerService.getOwner(id));
        return "owners/form";
    }

    @PutMapping("/edit/{id}")
    public String updateOwner(@PathVariable Long id,
                              @Valid @ModelAttribute("owner") OwnerDto ownerDto,
                              BindingResult result) {
        if (result.hasErrors()) {
            return "owners/form";
        }
        ownerService.updateOwner(id, ownerDto);
        return "redirect:/owners";
    }

    @DeleteMapping("/{id}")
    public String deleteOwner(@PathVariable Long id) {
        // няма try-catch, GlobalExceptionHandler поема ResourceNotFoundException
        ownerService.deleteOwner(id);
        return "redirect:/owners";
    }
}
