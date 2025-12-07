package nbu.edomoupravitel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nbu.edomoupravitel.entity.Owner;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDto {
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    public static Owner toEntity(OwnerDto dto) {
        return Owner.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .build();
    }

    public static OwnerDto fromEntity(Owner entity) {
        return OwnerDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .build();
    }
}
