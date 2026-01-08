package nbu.edomoupravitel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreasuryReportDto {

    // Глобални суми (Общо за цялата система)
    private BigDecimal totalUnpaidAmount; // Общо дължимо (всички фирми)
    private BigDecimal totalPaidAmount; // Общо събрано (всички фирми)

    // Списък с редове за таблицата (по фирми)
    private List<CompanyReportRow> companyRows;

    private List<OverdueInfo> overdueApartments; // <--- НОВО ПОЛЕ

    @Data
    @AllArgsConstructor
    public static class OverdueInfo {
        private String apartmentInfo; // напр. "Блок 1, Ап. 5"
        private String month; // напр. "Януари 2025"
        private BigDecimal amount;
    }

    @Data
    @AllArgsConstructor
    public static class CompanyReportRow {
        private String companyName;
        private BigDecimal unpaidAmount;
        private BigDecimal paidAmount;
    }
}