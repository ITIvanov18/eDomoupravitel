package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.ApartmentDto;
import nbu.edomoupravitel.entity.*;
import nbu.edomoupravitel.exception.ResourceNotFoundException;
import nbu.edomoupravitel.repository.*;
import nbu.edomoupravitel.service.ApartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final OwnerRepository ownerRepository;
    private final PaymentRepository paymentRepository;
    private final ResidentRepository residentRepository;

    private static final double BASE_TAX = 1.00; // такса на кв.м
    private static final double ELEVATOR_TAX = 10.00; // такса за живущ над 7 г., ползващ асансьор
    private static final double PET_TAX = 5.00; // такса за домашен любимец

    @Override
    @Transactional
    public ApartmentDto createApartment(ApartmentDto apartmentDto) {
        // валидация, че сградата съществува
        Building building = buildingRepository.findById(apartmentDto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Building not found with id: " + apartmentDto.getBuildingId()));

        Apartment apartment = ApartmentDto.toEntity(apartmentDto);
        apartment.setBuilding(building);

        // ако е подаден собственик, той бива намерен и assign-нат
        if (apartmentDto.getOwnerId() != null) {
            Owner owner = ownerRepository.findById(apartmentDto.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Owner not found with id: " + apartmentDto.getOwnerId()));
            apartment.setOwner(owner);
        }

        Apartment savedApartment = apartmentRepository.save(apartment);
        // връща се DTO с вече изчислена такса (за визуализация веднага)
        return mapToDtoWithFee(savedApartment);
    }

    @Override
    @Transactional
    public ApartmentDto updateApartment(Long id, ApartmentDto apartmentDto) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + id));

        apartment.setApartmentNumber(apartmentDto.getApartmentNumber());
        apartment.setFloor(apartmentDto.getFloor());
        apartment.setArea(apartmentDto.getArea());
        apartment.setHasPet(apartmentDto.isHasPet());

        if (apartmentDto.getOwnerId() != null) {
            Owner owner = ownerRepository.findById(apartmentDto.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Owner not found with id: " + apartmentDto.getOwnerId()));
            apartment.setOwner(owner);
        } else {
            apartment.setOwner(null);
        }

        Apartment savedApartment = apartmentRepository.save(apartment);
        return mapToDtoWithFee(savedApartment);
    }

    @Override
    @Transactional
    public void deleteApartment(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + id));

        List<Payment> payments = paymentRepository.findByApartment(apartment);

        // unlink на плащанията с апартамента
        // не се трият, за да не губим отчетност
        String historySnapshot = String.format("ARCHIVED: %s, , Floor %d, Ap. %d",
                apartment.getBuilding().getAddress(),
                apartment.getFloor(),
        apartment.getApartmentNumber());

        for (Payment payment : payments) {
            // ЗАПАЗВА ИСТОРИЯТА
            payment.setAuditDetails(historySnapshot);
            // КЪСА ВРЪЗКАТА
            payment.setApartment(null);
            paymentRepository.save(payment);
        }

        // (cascade delete) - ако няма апартамент, няма и жители
        // трием ги, за да не гръмне следващият constraint
        List<Resident> residents = residentRepository.findByApartment(apartment);
        residentRepository.deleteAll(residents);

        // синхронизира базата преди да изтрие "родителя"
        paymentRepository.flush();
        residentRepository.flush();

        apartmentRepository.delete(apartment);
    }

    @Override
    public ApartmentDto getApartment(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + id));
        return mapToDtoWithFee(apartment);
    }

    @Override
    public List<ApartmentDto> getApartmentsByBuilding(Long buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + buildingId));

        return apartmentRepository.findByBuilding(building).stream()
                .map(this::mapToDtoWithFee)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApartmentDto> getAllApartments() {
        return apartmentRepository.findAll().stream()
                .sorted(Comparator.comparing((Apartment a) -> a.getBuilding().getAddress())
                        .thenComparingInt(Apartment::getApartmentNumber))
                .map(this::mapToDtoWithFee)
                .collect(Collectors.toList());
    }

    @Override
    public double calculateMonthlyFee(Long apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + apartmentId));
        return calculateFeeInternal(apartment);
    }

    private ApartmentDto mapToDtoWithFee(Apartment apartment) {
        ApartmentDto dto = ApartmentDto.fromEntity(apartment);
        dto.setCalculatedFee(calculateFeeInternal(apartment));
        return dto;
    }

    private double calculateFeeInternal(Apartment apartment) {
        double areaFee = apartment.getArea() * BASE_TAX;

        long residentsUsingElevator = apartment.getResidents() != null ? apartment.getResidents().stream()
                .filter(r -> r.getAge() > 7 && r.isUsesElevator())
                .count() : 0;
        double elevatorFee = residentsUsingElevator * ELEVATOR_TAX;

        double petFee = apartment.isHasPet() ? PET_TAX : 0.0;

        return areaFee + elevatorFee + petFee;
    }
}
