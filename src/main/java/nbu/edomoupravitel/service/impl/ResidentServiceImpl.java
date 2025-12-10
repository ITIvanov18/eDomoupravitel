package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.ResidentDto;
import nbu.edomoupravitel.entity.Apartment;
import nbu.edomoupravitel.entity.Owner;
import nbu.edomoupravitel.entity.Resident;
import nbu.edomoupravitel.exception.LogicOperationException;
import nbu.edomoupravitel.exception.ResourceNotFoundException;
import nbu.edomoupravitel.repository.ApartmentRepository;
import nbu.edomoupravitel.repository.OwnerRepository;
import nbu.edomoupravitel.repository.ResidentRepository;
import nbu.edomoupravitel.service.ResidentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// автоматично създава конструктор за final полетата (dependency injection)
public class ResidentServiceImpl implements ResidentService {

    private final ResidentRepository residentRepository;
    private final ApartmentRepository apartmentRepository;
    private final OwnerRepository ownerRepository;

    @Override
    @Transactional
    public ResidentDto createResident(ResidentDto residentDto) {
        Apartment apartment = apartmentRepository.findById(residentDto.getApartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Apartment not found with id: " + residentDto.getApartmentId()));

        Resident resident = ResidentDto.toEntity(residentDto);
        resident.setApartment(apartment);
        Resident savedResident = residentRepository.save(resident);

        if (residentDto.isOwner()) {
            Owner owner = new Owner();
            owner.setFirstName(residentDto.getFirstName());
            owner.setLastName(residentDto.getLastName());
            owner = ownerRepository.save(owner);

            apartment.setOwner(owner);
            apartmentRepository.save(apartment);
        }

        return ResidentDto.fromEntity(savedResident);
    }

    @Override
    @Transactional
    public ResidentDto updateResident(Long id, ResidentDto residentDto) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + id));

        resident.setFirstName(residentDto.getFirstName());
        resident.setLastName(residentDto.getLastName());
        resident.setAge(residentDto.getAge());
        resident.setUsesElevator(residentDto.isUsesElevator());

        Resident savedResident = residentRepository.save(resident);

        if (residentDto.isOwner()) {
            Apartment apartment = resident.getApartment();
            if (apartment != null) {
                Owner owner = new Owner();
                owner.setFirstName(residentDto.getFirstName());
                owner.setLastName(residentDto.getLastName());
                owner = ownerRepository.save(owner);

                apartment.setOwner(owner);
                apartmentRepository.save(apartment);
            }
        }

        return ResidentDto.fromEntity(savedResident);
    }

    @Override
    public void deleteResident(Long id) {
        if (!residentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resident not found with id: " + id);
        }
        residentRepository.deleteById(id);
    }

    @Override
    public ResidentDto getResident(Long id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + id));
        return ResidentDto.fromEntity(resident);
    }

    @Override
    public List<ResidentDto> getResidentsByApartment(Long apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + apartmentId));

        return residentRepository.findByApartment(apartment).stream()
                .map(ResidentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    // логика позволяваща бързо превключване между обикновен resident и owner
    public void toggleOwner(Long residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id: " + residentId));

        Apartment apartment = resident.getApartment();
        if (apartment == null) {
            throw new LogicOperationException("Resident is not assigned to an apartment");
        }

        Owner currentOwner = apartment.getOwner();

        // проверка дали ТОЗИ жител вече е собственик (по име)
        boolean isCurrentlyOwner = false;
        if (currentOwner != null
                && currentOwner.getFirstName().equals(resident.getFirstName())
                && currentOwner.getLastName().equals(resident.getLastName())) {
            isCurrentlyOwner = true;
        }

        if (isCurrentlyOwner) {
            apartment.setOwner(null);
            apartmentRepository.save(apartment);
            ownerRepository.delete(currentOwner); // чистим базата от orphans
        } else {
            // ако има друг собственик преди това, той бива сменен
            if (currentOwner != null) {
                apartment.setOwner(null);
                ownerRepository.delete(currentOwner);
            }

            Owner newOwner = new Owner();
            newOwner.setFirstName(resident.getFirstName());
            newOwner.setLastName(resident.getLastName());
            newOwner = ownerRepository.save(newOwner);

            apartment.setOwner(newOwner);
            apartmentRepository.save(apartment);
        }
    }
}
