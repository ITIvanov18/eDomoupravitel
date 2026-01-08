package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.TreasuryReportDto;

public interface TreasuryService {
    // Начислява таксите за всички апартаменти за даден месец
    void generateMonthlyFees(int month, int year);

    // Генерира справка за таблото
    TreasuryReportDto getTreasuryReport();
}
