package nbu.edomoupravitel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.ApartmentDto;
import nbu.edomoupravitel.service.ApartmentService;
import nbu.edomoupravitel.service.BuildingService;
import nbu.edomoupravitel.service.OwnerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;
    private final BuildingService buildingService;
    private final OwnerService ownerService;

    @GetMapping
    public String listApartments(@RequestParam(required = false) Long buildingId, Model model) {
        if (buildingId != null) {
            model.addAttribute("apartments", apartmentService.getApartmentsByBuilding(buildingId));
        } else {
            model.addAttribute("apartments", apartmentService.getAllApartments());
        }
        model.addAttribute("buildings", buildingService.getAllBuildings());
        return "apartments/list";
    }

    @GetMapping("/create")
    public String createApartmentForm(Model model) {
        model.addAttribute("apartment", new ApartmentDto());
        model.addAttribute("buildings", buildingService.getAllBuildings());
        model.addAttribute("owners", ownerService.getAllOwners());
        return "apartments/form";
    }

    @PostMapping("/create")
    public String createApartment(@Valid @ModelAttribute("apartment") ApartmentDto apartmentDto, BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("buildings", buildingService.getAllBuildings());
            model.addAttribute("owners", ownerService.getAllOwners());
            return "apartments/form";
        }
        apartmentService.createApartment(apartmentDto);
        return "redirect:/apartments?buildingId=" + apartmentDto.getBuildingId();
    }

    @DeleteMapping("/{id}")
    public String deleteApartment(@PathVariable Long id) {
        apartmentService.deleteApartment(id);
        return "redirect:/apartments";
    }

    @GetMapping("/{id}/fee")
    public String showFee(@PathVariable Long id, Model model) {
        double fee = apartmentService.calculateMonthlyFee(id);
        model.addAttribute("fee", fee);
        model.addAttribute("apartment", apartmentService.getApartment(id));
        return "apartments/fee";
    }
}
