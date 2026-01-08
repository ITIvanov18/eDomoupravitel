package nbu.edomoupravitel.scheduler;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.service.TreasuryService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class FeeScheduler {

    private final TreasuryService treasuryService;

    // 0 0 0 1 * ? означава "в 00:00 часа на 1-во число всеки месец"
    @Scheduled(cron = "0 0 0 1 * ?")
    public void autoGenerateFees() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        System.out.println("🤖 AUTOMATION: Generating fees for " + month + "/" + year);

        try {
            treasuryService.generateMonthlyFees(month, year);
        } catch (Exception e) {
            // вече са генерирани или друга грешка
            System.out.println("⚠️ Auto-generation skipped: " + e.getMessage());
        }
    }
}
