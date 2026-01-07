package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.BuildingDto;
import nbu.edomoupravitel.dto.ResidentDto;
import nbu.edomoupravitel.entity.Building;

import java.util.List;

// интерфейсът дефинира какво прави програмата, без да казва как
public interface BuildingService {
    BuildingDto createBuilding(BuildingDto buildingDto);

    BuildingDto updateBuilding(Long id, BuildingDto buildingDto);

    void deleteBuilding(Long id);

    BuildingDto getBuilding(Long id);

    List<BuildingDto> getAllBuildings();


    Building findBuildingById(Long id);

    List<ResidentDto> getResidentsForBuilding(Long buildingId, String sort);
}
