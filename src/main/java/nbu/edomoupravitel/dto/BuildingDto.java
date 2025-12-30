package nbu.edomoupravitel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbu.edomoupravitel.entity.Building;

@Data // Lombok: aвтоматично генерира getters, setters, toString, equals, hashCode
@Builder // Lombok: позволява създаване на обекти чрез pattern (BuildingDto.builder())
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDto {
    private Long id;

    @NotBlank(message = "Address is required")
    private String address;

    @Min(value = 1, message = "Floors must be at least 1")
    private int floors;

    @Min(value = 1, message = "Number of apartments must be at least 1")
    private int numberOfApartments;

    @Min(value = 0, message = "Area must be positive")
    private double area;

    @Min(value = 0, message = "Common parts area must be positive")
    private double commonPartsArea;

    private Long employeeId;
    private String employeeName;
    private String employeePhoneNumber;
    private String employeeCompanyName;
    private Long companyId;

    // преобразуване DTO -> Entity
    // използва се, когато получаваме данни от формата за create/edit
    public static Building toEntity(BuildingDto dto) {
        return Building.builder()
                .id(dto.getId())
                .address(dto.getAddress())
                .floors(dto.getFloors())
                .numberOfApartments(dto.getNumberOfApartments())
                .area(dto.getArea())
                .commonPartsArea(dto.getCommonPartsArea())
                // employee се задава чрез service логиката
                .build();
    }

    // преобразуване Entity -> DTO
    // ползва се при зареждане на списъка със сгради, за да се покаже на екрана
    public static BuildingDto fromEntity(Building entity) {
        return BuildingDto.builder()
                .id(entity.getId())
                .address(entity.getAddress())
                .floors(entity.getFloors())
                .numberOfApartments(entity.getNumberOfApartments())
                .area(entity.getArea())
                .commonPartsArea(entity.getCommonPartsArea())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null
                        ? entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName()
                        : null)
                .employeePhoneNumber(entity.getEmployee() != null ? entity.getEmployee().getPhoneNumber() : null)

                // deep traversal: Building -> Employee -> Company -> Name
                // проверка дали има Служител И дали този служител има Компания.
                // ако веригата е прекъсната някъде, връща null, за да не гръмне приложението
                .employeeCompanyName(entity.getEmployee() != null && entity.getEmployee().getCompany() != null
                        ? entity.getEmployee().getCompany().getName()
                        : null)
                .build();
    }
}
