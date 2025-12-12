package nbu.edomoupravitel.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbu.edomoupravitel.entity.Apartment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentDto {
        private Long id;

        @Min(value = 1, message = "Apartment number must be positive")
        private int apartmentNumber;

        @Min(value = 1, message = "Floor must be at least 1")
        private int floor;

        @Min(value = 0, message = "Area must be positive")
        private double area;

        private boolean hasPet;

        private Long buildingId;
        private String buildingAddress;

        private Long ownerId;
        private String ownerName;

        private List<ResidentDto> residents;
        private double calculatedFee;


        public static Apartment toEntity(ApartmentDto dto) {
                return Apartment.builder()
                                .id(dto.getId())
                                .apartmentNumber(dto.getApartmentNumber())
                                .floor(dto.getFloor())
                                .area(dto.getArea())
                                .hasPet(dto.isHasPet())
                                .build();
        }

        public static ApartmentDto fromEntity(Apartment entity) {
                List<ResidentDto> residentDtos = entity.getResidents() != null
                                ? entity.getResidents().stream()
                                                .map(ResidentDto::fromEntity)
                                                .collect(Collectors.toList())
                                : new ArrayList<>();

                return ApartmentDto.builder()
                                .id(entity.getId())
                                .apartmentNumber(entity.getApartmentNumber())
                                .floor(entity.getFloor())
                                .area(entity.getArea())
                                .hasPet(entity.isHasPet())
                                .buildingId(entity.getBuilding() != null ? entity.getBuilding().getId() : null)
                                .buildingAddress(
                                                entity.getBuilding() != null ? entity.getBuilding().getAddress() : null)
                                .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                                .ownerName(entity.getOwner() != null
                                                ? entity.getOwner().getFirstName() + " "
                                                                + entity.getOwner().getLastName()
                                                : "No Owner")
                                .residents(residentDtos)
                                .calculatedFee(0.0)
                                .build();
        }
}
