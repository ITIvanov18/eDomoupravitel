package nbu.edomoupravitel.controller;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.service.ApartmentService;
import nbu.edomoupravitel.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ApartmentService apartmentService;

    @GetMapping
    public String listPayments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "payments/list";
    }

    @GetMapping("/create")
    public String createPaymentForm(@RequestParam(required = false) Long apartmentId, Model model) {
        if (apartmentId != null) {
            // взима double от ApartmentService и го обръща в BigDecimal за визуализация
            double fee = apartmentService.calculateMonthlyFee(apartmentId);
            model.addAttribute("suggestedAmount", BigDecimal.valueOf(fee));
            model.addAttribute("apartmentId", apartmentId);
        }
        return "payments/form";
    }

    @PostMapping("/create")
    public String createPayment(@RequestParam Long apartmentId, @RequestParam BigDecimal amount) {
        paymentService.payFee(apartmentId, amount);
        return "redirect:/payments";
    }

    @GetMapping("/export")
    public String exportPayments(RedirectAttributes redirectAttributes) {
        try {
            // Exports to project root or specified path
            String fileName = "paid_fees.csv";
            paymentService.exportPaidFeesToFile(fileName);
            redirectAttributes.addFlashAttribute("message", "Successfully exported fees to " + fileName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Export failed: " + e.getMessage());
        }
        return "redirect:/payments";
    }
}
