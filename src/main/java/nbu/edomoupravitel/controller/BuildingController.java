package nbu.edomoupravitel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.BuildingDto;
import nbu.edomoupravitel.service.BuildingService;
import nbu.edomoupravitel.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;
    private final CompanyService companyService;

    @GetMapping
    public String listBuildings(Model model) {
        model.addAttribute("buildings", buildingService.getAllBuildings());
        return "buildings/list";
    }

    @GetMapping("/create")
    public String createBuildingForm(Model model) {
        model.addAttribute("building", new BuildingDto());
        model.addAttribute("companies", companyService.getAllCompanies());
        return "buildings/form";
    }

    @PostMapping("/create")
    public String createBuilding(@Valid @ModelAttribute("building") BuildingDto buildingDto, BindingResult result,
            Model model) {
        // ако има грешка във валидацията
        if (result.hasErrors()) {
            model.addAttribute("companies", companyService.getAllCompanies());
            return "buildings/form";
        }
        // ако няма служители във фирмата, LogicOperationException ще гръмне
        // и GlobalHandler ще покаже error.html
        buildingService.createBuilding(buildingDto);
        return "redirect:/buildings";
    }

    @DeleteMapping("/{id}")
    public String deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return "redirect:/buildings";
    }
}
