package nbu.edomoupravitel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.EmployeeDto;
import nbu.edomoupravitel.service.CompanyService;
import nbu.edomoupravitel.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final CompanyService companyService;

    @GetMapping
    public String listEmployees(@RequestParam(required = false, defaultValue = "name_asc") String sort, Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees(sort));
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("currentSort", sort);
        return "employees/list";
    }

    @GetMapping("/create")
    public String createEmployeeForm(Model model) {
        model.addAttribute("employee", new EmployeeDto());
        model.addAttribute("companies", companyService.getAllCompanies());
        return "employees/form";
    }

    @PostMapping("/create")
    public String createEmployee(@Valid @ModelAttribute("employee") EmployeeDto employeeDto, BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("companies", companyService.getAllCompanies());
            return "employees/form";
        }
        employeeService.createEmployee(employeeDto);
        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String editEmployeeForm(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.getEmployee(id));
        model.addAttribute("companies", companyService.getAllCompanies());
        return "employees/form";
    }

    @PostMapping("/edit/{id}")
    public String updateEmployee(@PathVariable Long id, @Valid @ModelAttribute("employee") EmployeeDto employeeDto,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("companies", companyService.getAllCompanies());
            return "employees/form";
        }
        employeeService.updateEmployee(id, employeeDto);
        return "redirect:/employees";
    }

    // DELETE вместо GET метод (изисква form method="post" + hidden input в HTML)
    @DeleteMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute("successMessage", "Служителят е изтрит успешно.");
        } catch (RuntimeException e) {
            // прихваща грешката (примерно ако е единствен служител със сгради)
            // и я връща като съобщение към потребителя
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employees";
    }
}
