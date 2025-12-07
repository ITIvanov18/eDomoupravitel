package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.OwnerDto;

import java.util.List;

public interface OwnerService {
    OwnerDto createOwner(OwnerDto ownerDto);

    OwnerDto updateOwner(Long id, OwnerDto ownerDto);

    void deleteOwner(Long id);

    OwnerDto getOwner(Long id);

    List<OwnerDto> getAllOwners();
}
