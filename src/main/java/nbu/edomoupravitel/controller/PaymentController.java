package nbu.edomoupravitel.controller;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.PaymentDto;
import nbu.edomoupravitel.service.ApartmentService;
import nbu.edomoupravitel.service.PaymentService;
import nbu.edomoupravitel.service.TreasuryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ApartmentService apartmentService;
    private final TreasuryService treasuryService;

    @GetMapping
    public String listPayments(Model model) {
        // списък с плащания (история)
        model.addAttribute("payments", paymentService.getAllPayments());

        // списък с всички апартаменти за падащото меню "Record Payment"
        model.addAttribute("apartments", apartmentService.getAllApartments());

        // данни за хазната
        model.addAttribute("report", treasuryService.getTreasuryReport());
        model.addAttribute("currentMonth", LocalDate.now().getMonthValue());
        model.addAttribute("currentYear", LocalDate.now().getYear());

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
        PaymentDto paymentDto = PaymentDto.builder()
                .apartmentId(apartmentId)
                .amount(amount)
                .build();
        paymentService.createPayment(paymentDto);
        return "redirect:/payments";
    }

    @PostMapping("/generate-fees")
    public String generateFees(@RequestParam int month, @RequestParam int year, RedirectAttributes redirectAttributes) {
        try {
            treasuryService.generateMonthlyFees(month, year);
            redirectAttributes.addFlashAttribute("message", "Успешно начислени такси за " + month + "/" + year);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при начисляване: " + e.getMessage());
        }
        return "redirect:/payments";
    }

    @GetMapping("/export")
    public String exportPayments(RedirectAttributes redirectAttributes) {
        try {
            // експортва файла в папката на проекта
            String fileName = "paid_fees.csv";
            paymentService.exportPaidFeesToFile(fileName);
            redirectAttributes.addFlashAttribute("message", "Successfully exported fees to " + fileName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Export failed: " + e.getMessage());
        }
        return "redirect:/payments";
    }
}
