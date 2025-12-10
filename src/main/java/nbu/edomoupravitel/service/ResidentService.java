package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.ResidentDto;

import java.util.List;

// интерфейсът дефинира какво прави програмата, без да казва как
public interface ResidentService {
    ResidentDto createResident(ResidentDto residentDto);

    ResidentDto updateResident(Long id, ResidentDto residentDto);

    void deleteResident(Long id);

    ResidentDto getResident(Long id);

    List<ResidentDto> getResidentsByApartment(Long apartmentId);

    void toggleOwner(Long residentId);
}
