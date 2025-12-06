package nbu.edomoupravitel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbu.edomoupravitel.entity.Building;

@Data
@Builder
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
    private Long companyId; // used for creation context

    public static Building toEntity(BuildingDto dto) {
        return Building.builder()
                .id(dto.getId())
                .address(dto.getAddress())
                .floors(dto.getFloors())
                .numberOfApartments(dto.getNumberOfApartments())
                .area(dto.getArea())
                .commonPartsArea(dto.getCommonPartsArea())
                // Employee set via service logic
                .build();
    }

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
                .build();
    }
}
