package nbu.edomoupravitel.controller;
import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Date;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CompanyRepository companyRepository;
    private final BuildingRepository buildingRepository;
    private final EmployeeRepository employeeRepository;
    private final ApartmentRepository apartmentRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/")
    public String index(org.springframework.ui.Model model) {
        model.addAttribute("companyCount", companyRepository.count());
        model.addAttribute("buildingCount", buildingRepository.count());
        model.addAttribute("employeeCount", employeeRepository.count());
        model.addAttribute("apartmentCount", apartmentRepository.count());
        model.addAttribute("paymentCount", paymentRepository.count());

        model.addAttribute("serverTime", new Date());
        return "index";
    }
}
