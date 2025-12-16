package nbu.edomoupravitel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbu.edomoupravitel.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    private LocalDate paymentDate;

    private Long apartmentId;

    // полета за справка (read-only при визуализация)
    private String companyName;
    private String employeeName;
    private String buildingAddress;

    public static Payment toEntity(PaymentDto dto) {
        return Payment.builder()
                .id(dto.getId())
                .amount(dto.getAmount())
                .paymentDate(dto.getPaymentDate())
                .build();
    }

    public static PaymentDto fromEntity(Payment entity) {
        PaymentDto.PaymentDtoBuilder builder = PaymentDto.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .paymentDate(entity.getPaymentDate())
                .apartmentId(entity.getApartment() != null ? entity.getApartment().getId() : null);

        // навигира през графа от обекти, за да извлече контекстна информация за UI
        // ВАРИАНТ 1: апартаментът съществува (Live Data)
        if (entity.getApartment() != null && entity.getApartment().getBuilding() != null) {
            builder.buildingAddress(entity.getApartment().getBuilding().getAddress());

            if (entity.getApartment().getBuilding().getEmployee() != null) {
                builder.employeeName(entity.getApartment().getBuilding().getEmployee().getFirstName()
                        + " " + entity.getApartment().getBuilding().getEmployee().getLastName());

                if (entity.getApartment().getBuilding().getEmployee().getCompany() != null) {
                    builder.companyName(entity.getApartment().getBuilding().getEmployee().getCompany().getName());
                }
            }
        }
        // ВАРИАНТ 2: апартаментът е изтрит (Historical Snapshot)
        else if (entity.getAuditDetails() != null) {
            builder.buildingAddress(entity.getAuditDetails()); // Тук ще излезе "ARCHIVED: Apt 5..."
            builder.companyName("N/A (Deleted)");
            builder.employeeName("N/A");
        }
        // ВАРИАНТ 3: няма данни (сираче)
        else {
            builder.buildingAddress("Unknown (Data Lost)");
        }

        return builder.build();
    }
}
