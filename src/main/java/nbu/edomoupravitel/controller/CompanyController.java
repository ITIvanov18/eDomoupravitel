package nbu.edomoupravitel.controller;

import nbu.edomoupravitel.dto.EmployeeDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.CompanyDto;
import nbu.edomoupravitel.exception.LogicOperationException;
import nbu.edomoupravitel.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public String listCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());
        return "companies/list";
    }

    @GetMapping("/create")
    public String createCompanyForm(Model model) {
        model.addAttribute("company", new CompanyDto());
        return "companies/form";
    }

    @PostMapping("/create")
    public String createCompany(@Valid @ModelAttribute("company") CompanyDto companyDto, BindingResult result) {
        if (result.hasErrors()) {
            return "companies/form";
        }
        companyService.createCompany(companyDto);
        return "redirect:/companies";
    }

    @GetMapping("/edit/{id}")
    public String editCompanyForm(@PathVariable Long id, Model model) {
        model.addAttribute("company", companyService.getCompany(id));
        return "companies/form";
    }

    @PutMapping("/edit/{id}")
    public String updateCompany(@PathVariable Long id, @Valid @ModelAttribute("company") CompanyDto companyDto,
                                BindingResult result) {
        if (result.hasErrors()) {
            return "companies/form";
        }
        companyService.updateCompany(id, companyDto);
        return "redirect:/companies";
    }

    @DeleteMapping("/{id}")
    public String deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return "redirect:/companies";
    }

    // --- API Endpoints for Modal ---

    @GetMapping("/{id}/data")
    @ResponseBody
    public java.util.Map<String, Object> getCompanyData(@PathVariable Long id) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("company", companyService.getCompany(id));
        data.put("employees", companyService.getCompanyEmployees(id));
        data.put("availableEmployees", companyService.getAvailableEmployees());
        return data;
    }

    @PostMapping("/{id}/employees/{employeeId}")
    @ResponseBody
    public ResponseEntity<?> assignEmployee(@PathVariable Long id,
                                            @PathVariable Long employeeId) {
        companyService.assignEmployee(id, employeeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/employees/{employeeId}")
    @ResponseBody
    public ResponseEntity<?> removeEmployee(@PathVariable Long id,
        @PathVariable Long employeeId) {
        try {
            companyService.removeEmployee(id, employeeId);
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (LogicOperationException | IllegalArgumentException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/employees/create")
    @ResponseBody
    public ResponseEntity<?> createAndAssignEmployee(@PathVariable Long id,
                                                                              @RequestBody EmployeeDto employeeDto) {
        companyService.createAndAssignEmployee(id, employeeDto);
        return ResponseEntity.ok().build();
    }
}
