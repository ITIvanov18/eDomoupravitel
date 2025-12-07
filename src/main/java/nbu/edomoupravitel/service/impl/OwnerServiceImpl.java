package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.OwnerDto;
import nbu.edomoupravitel.entity.Owner;
import nbu.edomoupravitel.exception.ResourceNotFoundException;
import nbu.edomoupravitel.repository.OwnerRepository;
import nbu.edomoupravitel.service.OwnerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// автоматично създава конструктор за final полетата (dependency injection)
public class OwnerServiceImpl implements OwnerService {

    private final OwnerRepository ownerRepository;

    // преобразува DTO -> Entity, за да го запише в базата
    @Override
    public OwnerDto createOwner(OwnerDto ownerDto) {
        Owner owner = OwnerDto.toEntity(ownerDto);
        Owner savedOwner = ownerRepository.save(owner);
        return OwnerDto.fromEntity(savedOwner);
        // после връща Entity -> DTO към контролера
    }

    @Override
    public OwnerDto updateOwner(Long id, OwnerDto ownerDto) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + id));

        owner.setFirstName(ownerDto.getFirstName());
        owner.setLastName(ownerDto.getLastName());

        // save() върху съществуващо ID прави UPDATE, а не INSERT
        Owner savedOwner = ownerRepository.save(owner);
        return OwnerDto.fromEntity(savedOwner);
    }

    @Override
    public void deleteOwner(Long id) {
        if (!ownerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Owner not found with id: " + id);
        }
        ownerRepository.deleteById(id);
    }

    @Override
    public OwnerDto getOwner(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + id));
        return OwnerDto.fromEntity(owner);
    }

    @Override
    // stream API за преобразуване на списък от Entities към DTOs
    public List<OwnerDto> getAllOwners() {
        return ownerRepository.findAll().stream()
                .map(OwnerDto::fromEntity)
                .collect(Collectors.toList());
    }
}
