package nbu.edomoupravitel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbu.edomoupravitel.entity.Building;
import nbu.edomoupravitel.entity.Employee;


import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    private Long companyId;
    private String companyName;
    private int buildingCount;
    private List<String> buildingAddresses;

    // преобразуване DTO -> Entity (без връзката с Company, тя се прави в сървиса)
    public static Employee toEntity(EmployeeDto dto) {
        return Employee.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                // компанията се задава от service-а
                .build();
    }

    // преобразуване Entity -> DTO (зарежда се името на фирмата за UI)
    public static EmployeeDto fromEntity(Employee entity) {
        List<String> addresses = entity.getBuildings() != null
                ? entity.getBuildings().stream()
                .map(Building::getAddress)
                .collect(Collectors.toList())
                : new ArrayList<>();


        return EmployeeDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phoneNumber(entity.getPhoneNumber())
                .companyId(entity.getCompany() != null ? entity.getCompany().getId() : null)
                .companyName(entity.getCompany() != null ? entity.getCompany().getName() : null)
                .buildingCount(addresses.size())
                .buildingAddresses(addresses)
                .build();
    }
}
