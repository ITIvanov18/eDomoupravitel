package nbu.edomoupravitel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbu.edomoupravitel.entity.Company;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;

    @NotBlank(message = "Company name is required")
    private String name;
    private Long buildingCount;


    @NotNull(message = "Tax per SqM is required")
    @Min(value = 0, message = "Tax must be positive")
    private Double taxPerSqM;

    @NotNull(message = "Elevator Tax is required")
    @Min(value = 0, message = "Tax must be positive")
    private Double elevatorTax;

    @NotNull(message = "Pet Tax is required")
    @Min(value = 0, message = "Tax must be positive")
    private Double petTax;

    // Manual constructor for JPQL query CompanyDto(c.id, c.name, COUNT(b))
    public CompanyDto(Long id, String name, Long buildingCount) {
        this.id = id;
        this.name = name;
        this.buildingCount = (buildingCount != null) ? buildingCount : 0L;
        this.taxPerSqM = 0.0;
        this.elevatorTax = 0.0;
        this.petTax = 0.0;
    }

    // помощен метод за превръщане от DTO в Entity (за запис в базата)
    public static Company toEntity(CompanyDto dto) {
        return Company.builder()
                .id(dto.getId())
                .name(dto.getName())
                .taxPerSqM(dto.getTaxPerSqM())
                .elevatorTax(dto.getElevatorTax())
                .petTax(dto.getPetTax())
                .build();
    }

    // помощен метод за превръщане от Entity в DTO (за визуализация)
    public static CompanyDto fromEntity(Company entity) {
        return CompanyDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .taxPerSqM(entity.getTaxPerSqM())
                .elevatorTax(entity.getElevatorTax())
                .petTax(entity.getPetTax())
                .build();
    }
}
