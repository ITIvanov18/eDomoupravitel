package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.ApartmentDto;

import java.util.List;

public interface ApartmentService {
    ApartmentDto createApartment(ApartmentDto apartmentDto);

    ApartmentDto updateApartment(Long id, ApartmentDto apartmentDto);

    void deleteApartment(Long id);

    ApartmentDto getApartment(Long id);

    List<ApartmentDto> getApartmentsByBuilding(Long buildingId);

    List<ApartmentDto> getAllApartments();

    // изчислява месечната такса за апартамент базирано на правилата:
    // (площ * цена/кв.м) + (живущи над 7г, ползващи асансьор * такса) + (pet такса)
    double calculateMonthlyFee(Long apartmentId);
}
