package nbu.edomoupravitel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.BuildingDto;
import nbu.edomoupravitel.dto.ResidentDto;
import nbu.edomoupravitel.exception.LogicOperationException;
import nbu.edomoupravitel.service.BuildingService;
import nbu.edomoupravitel.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;
    private final CompanyService companyService;

    @GetMapping
    public String listBuildings(Model model) {
        model.addAttribute("buildings", buildingService.getAllBuildings());
        model.addAttribute("companies", companyService.getAllCompanies());
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

    @GetMapping("/{id}/data")
    @ResponseBody
    public BuildingDto getBuildingData(@PathVariable Long id) {
        return buildingService.getBuilding(id);
    }

    @PutMapping("/edit/{id}")
    public String updateBuilding(@PathVariable Long id, @Valid @ModelAttribute("building") BuildingDto buildingDto,
                                 BindingResult result) {
        if (result.hasErrors()) {
            throw new LogicOperationException(
                    "Validation failed: " + result.getAllErrors().getFirst().getDefaultMessage());
        }
        buildingService.updateBuilding(id, buildingDto);
        return "redirect:/buildings";
    }

    @GetMapping("/{id}/residents")
    @ResponseBody
    public List<ResidentDto> getBuildingResidents(@PathVariable Long id,
                                                  @RequestParam(required = false, defaultValue = "name_asc") String sort) {
        return buildingService.getResidentsForBuilding(id, sort);
    }
}
