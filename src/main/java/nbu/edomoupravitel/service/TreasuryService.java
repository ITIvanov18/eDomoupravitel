package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.TreasuryReportDto;

public interface TreasuryService {
    // начислява таксите за всички апартаменти за даден месец
    void generateMonthlyFees(int month, int year);

    // генерира справка за таблото
    TreasuryReportDto getTreasuryReport();
}
