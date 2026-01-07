package nbu.edomoupravitel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbu.edomoupravitel.entity.Resident;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResidentDto {
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Min(value = 0, message = "Age must be positive")
    private int age;

    private boolean usesElevator;

    private boolean isOwner;

    private Long apartmentId;

    private String apartmentNumber;

    public static Resident toEntity(ResidentDto dto) {
        return Resident.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .age(dto.getAge())
                .usesElevator(dto.isUsesElevator())
                .build();
    }

    public static ResidentDto fromEntity(Resident entity) {
        boolean isOwner = false;
        if (entity.getApartment() != null && entity.getApartment().getOwner() != null) {
            String ownerFirst = entity.getApartment().getOwner().getFirstName();
            String ownerLast = entity.getApartment().getOwner().getLastName();
            if (ownerFirst != null && ownerLast != null
                    && ownerFirst.equals(entity.getFirstName())
                    && ownerLast.equals(entity.getLastName())) {
                isOwner = true;
            }
        }

        return ResidentDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .age(entity.getAge())
                .usesElevator(entity.isUsesElevator())
                .apartmentId(entity.getApartment() != null ? entity.getApartment().getId() : null)
                .apartmentNumber(
                        entity.getApartment() != null ? String.valueOf(entity.getApartment().getApartmentNumber())
                                : "N/A")
                .isOwner(isOwner)
                .build();
    }
}
