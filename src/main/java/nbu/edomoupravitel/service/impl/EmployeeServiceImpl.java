package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.EmployeeDto;
import nbu.edomoupravitel.entity.Building;
import nbu.edomoupravitel.entity.Company;
import nbu.edomoupravitel.entity.Employee;
import nbu.edomoupravitel.exception.LogicOperationException;
import nbu.edomoupravitel.exception.ResourceNotFoundException;
import nbu.edomoupravitel.repository.CompanyRepository;
import nbu.edomoupravitel.repository.EmployeeRepository;
import nbu.edomoupravitel.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// автоматично създава конструктор за final полетата (dependency injection)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    @Override
    public void createEmployee(EmployeeDto employeeDto) {
        // намира фирмата по ID от падащото меню
        Company company = companyRepository.findById(employeeDto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + employeeDto.getCompanyId()));

        // създава служителя и му assign-ва фирмата
        Employee employee = EmployeeDto.toEntity(employeeDto);
        employee.setCompany(company);

        Employee savedEmployee = employeeRepository.save(employee);
        EmployeeDto.fromEntity(savedEmployee);
    }

    @Override
    public void updateEmployee(Long id, EmployeeDto employeeDto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setPhoneNumber(employeeDto.getPhoneNumber());

        // обновяване на компанията, ако е сменена
        if (employeeDto.getCompanyId() != null && !employeeDto.getCompanyId().equals(employee.getCompany().getId())) {
            Company newCompany = companyRepository.findById(employeeDto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + employeeDto.getCompanyId()));
            employee.setCompany(newCompany);
        }

        Employee savedEmployee = employeeRepository.save(employee);
        EmployeeDto.fromEntity(savedEmployee);
    }

    @Override
    public EmployeeDto getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return EmployeeDto.fromEntity(employee);
    }

    @Override
    public List<EmployeeDto> getAllEmployees(String sortBy) {
        List<EmployeeDto> employees = employeeRepository.findAll().stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());


        if (sortBy == null || sortBy.isEmpty()) {
            return employees;
        }

        switch (sortBy) {
            case "name_asc":
                employees.sort(Comparator.comparing(EmployeeDto::getFirstName)
                        .thenComparing(EmployeeDto::getLastName));
                break;
            case "name_desc":
                employees.sort(Comparator.comparing(EmployeeDto::getFirstName)
                        .thenComparing(EmployeeDto::getLastName).reversed());
                break;
            case "buildings_desc": // Most workloads at top
                employees.sort(Comparator.comparingInt(EmployeeDto::getBuildingCount).reversed());
                break;
            case "buildings_asc": // Least workloads at top
                employees.sort(Comparator.comparingInt(EmployeeDto::getBuildingCount));
                break;
        }
        return employees;
    }


    @Override
    @Transactional
    public void redistributeBuildings(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        List<Building> buildingsToRedistribute = new ArrayList<>(employee.getBuildings());

        if (!buildingsToRedistribute.isEmpty()) {
            Company company = employee.getCompany();
            if (company == null) {
                return;
            }

            List<Employee> otherEmployees = employeeRepository.findByCompany(company).stream()
                    .filter(e -> !e.getId().equals(id))
                    .toList();

            if (otherEmployees.isEmpty()) {
                throw new LogicOperationException(
                        "Failed to remove employee. There are no other employees in the company to take over their buildings.");
            }

            for (Building building : buildingsToRedistribute) {
                Employee targetEmployee = otherEmployees.stream()
                        .min(Comparator.comparingInt(e -> e.getBuildings().size()))
                        .orElse(otherEmployees.getFirst());

                building.setEmployee(targetEmployee);
                targetEmployee.getBuildings().add(building);
            }
        }
    }

    @Override
    @Transactional // гарантира, че прехвърлянето на сгради и триенето стават заедно
    public void deleteEmployee(Long id) {
        Employee employeeToDelete = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        redistributeBuildings(id);
        employeeRepository.delete(employeeToDelete);
    }
}
