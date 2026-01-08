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

    // глобални суми
    private BigDecimal totalUnpaidAmount; // общо дължимо (всички фирми)
    private BigDecimal totalPaidAmount; // общо събрано (всички фирми)

    // списък с редове за таблицата (по фирми)
    private List<CompanyReportRow> companyRows;

    private List<OverdueInfo> overdueApartments;

    @Data
    @AllArgsConstructor
    public static class OverdueInfo {
        private String apartmentInfo;
        private String month;
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