package nbu.edomoupravitel.dto;

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

    // помощен метод за превръщане от DTO в Entity (за запис в базата)
    public static Company toEntity(CompanyDto dto) {
        return Company.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }

    // помощен метод за превръщане от Entity в DTO (за визуализация)
    public static CompanyDto fromEntity(Company entity) {
        return CompanyDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
