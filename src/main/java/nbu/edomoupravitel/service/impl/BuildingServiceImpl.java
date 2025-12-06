package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.BuildingDto;
import nbu.edomoupravitel.entity.Building;
import nbu.edomoupravitel.entity.Company;
import nbu.edomoupravitel.entity.Employee;
import nbu.edomoupravitel.exception.LogicOperationException;
import nbu.edomoupravitel.exception.ResourceNotFoundException;
import nbu.edomoupravitel.repository.BuildingRepository;
import nbu.edomoupravitel.repository.CompanyRepository;
import nbu.edomoupravitel.repository.EmployeeRepository;
import nbu.edomoupravitel.service.BuildingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// автоматично създава конструктор за final полетата (dependency injection)
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public BuildingDto createBuilding(BuildingDto buildingDto) {
        // намира компанията, към която ще бъде сградата
        Company company = companyRepository.findById(buildingDto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + buildingDto.getCompanyId()));

        Building building = BuildingDto.toEntity(buildingDto);
        // сградата няма директен reference към компанията, а само чрез Employee или логика
        // БИЗНЕС ЛОГИКА: автоматично разпределение на служител
        List<Employee> employees = employeeRepository.findByCompany(company);

        if (employees.isEmpty()) {
            // ако фирмата няма служители, не може да обслужва сградата
            throw new LogicOperationException(
                    "Cannot assign building. No employees found in company: " + company.getName());
        }

        // намира служителя с най-малко сгради (load balancing)
        Employee bestEmployee = employees.stream()
                .min(Comparator.comparingInt(e -> e.getBuildings().size()))
                .orElse(employees.getFirst());

        // закача сградата за него
        building.setEmployee(bestEmployee);

        Building savedBuilding = buildingRepository.save(building);
        return BuildingDto.fromEntity(savedBuilding);
    }

    @Override
    public BuildingDto updateBuilding(Long id, BuildingDto buildingDto) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));

        building.setAddress(buildingDto.getAddress());
        building.setFloors(buildingDto.getFloors());
        building.setNumberOfApartments(buildingDto.getNumberOfApartments());
        building.setArea(buildingDto.getArea());
        building.setCommonPartsArea(buildingDto.getCommonPartsArea());

        Building savedBuilding = buildingRepository.save(building);
        return BuildingDto.fromEntity(savedBuilding);
    }

    @Override
    public void deleteBuilding(Long id) {
        if (!buildingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Building not found with id: " + id);
        }
        buildingRepository.deleteById(id);
    }

    @Override
    public BuildingDto getBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));
        return BuildingDto.fromEntity(building);
    }

    @Override
    public List<BuildingDto> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(BuildingDto::fromEntity)
                .collect(Collectors.toList());
    }
}
