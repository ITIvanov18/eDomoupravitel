package nbu.edomoupravitel.service.impl;

import lombok.RequiredArgsConstructor;
import nbu.edomoupravitel.dto.CompanyDto;
import nbu.edomoupravitel.entity.Company;
import nbu.edomoupravitel.entity.Employee;
import nbu.edomoupravitel.exception.ResourceNotFoundException;
import nbu.edomoupravitel.repository.CompanyRepository;
import nbu.edomoupravitel.repository.EmployeeRepository;
import nbu.edomoupravitel.repository.MonthlyFeeRepository;
import nbu.edomoupravitel.service.CompanyService;
import nbu.edomoupravitel.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
// автоматично създава конструктор за final полетата (dependency injection)
public class CompanyServiceImpl implements CompanyService {

    private final MonthlyFeeRepository monthlyFeeRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    @Override
    public CompanyDto createCompany(CompanyDto companyDto) {
        Company company = CompanyDto.toEntity(companyDto);
        Company savedCompany = companyRepository.save(company);
        return CompanyDto.fromEntity(savedCompany);
    }

    @Override
    public CompanyDto updateCompany(Long id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        company.setName(companyDto.getName());
        company.setTaxPerSqM(companyDto.getTaxPerSqM());
        company.setElevatorTax(companyDto.getElevatorTax());
        company.setPetTax(companyDto.getPetTax());

        Company savedCompany = companyRepository.save(company);
        return CompanyDto.fromEntity(savedCompany);
    }

    @Override
    public CompanyDto getCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return CompanyDto.fromEntity(company);
    }

    public List<CompanyDto> getAllCompanies(String sort) {
        // взима базовия списък
        List<CompanyDto> companies = companyRepository.findAllWithBuildingCount();
        // обикаля и попълва прихода за всяка компания
        for (CompanyDto company : companies) {
            BigDecimal revenue = monthlyFeeRepository.sumPaidByCompany(company.getId());
            company.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);
        }
        // сортира
        if (sort != null) {
            switch (sort) {
                case "name_asc":
                    companies.sort(Comparator.comparing(CompanyDto::getName));
                    break;
                case "name_desc":
                    companies.sort(Comparator.comparing(CompanyDto::getName).reversed());
                    break;
                case "revenue_desc": // най-богатите най-горе
                    companies.sort(Comparator.comparing(CompanyDto::getTotalRevenue).reversed());
                    break;
                case "revenue_asc":
                    companies.sort(Comparator.comparing(CompanyDto::getTotalRevenue));
                    break;
            }
        }
        return companies;
    }

    @Override
    public List<CompanyDto> getAllCompanies() {
        return getAllCompanies(null);
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        // намира всички служители, които работят там
        List<Employee> employees = employeeRepository.findByCompany(company);

        // разкача ги от фирмата (стават "unassigned", но си пазят сградите)
        for (Employee employee : employees) {
            employee.setCompany(null);
            employeeRepository.save(employee);
        }

        // UPDATE в базата веднага
        // така базата знае, че служителите са свободни, ПРЕДИ да изчезне фирмата
        employeeRepository.flush();
        companyRepository.delete(company);
    }

    @Override
    @Transactional
    public void assignEmployee(Long companyId, Long employeeId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employee.getCompany() != null) {
            throw new IllegalArgumentException("Employee is already assigned to a company");
        }

        employee.setCompany(company);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void removeEmployee(Long companyId, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employee.getCompany() == null || !employee.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Employee is not assigned to this company");
        }

        // redistribute buildings before unassigning
        employeeService.redistributeBuildings(employeeId);

        employee.setCompany(null);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void createAndAssignEmployee(Long companyId, nbu.edomoupravitel.dto.EmployeeDto employeeDto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Employee employee = nbu.edomoupravitel.dto.EmployeeDto.toEntity(employeeDto);
        employee.setCompany(company);
        employeeRepository.save(employee);
    }

    @Override
    public List<nbu.edomoupravitel.dto.EmployeeDto> getCompanyEmployees(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return employeeRepository.findByCompany(company).stream()
                .map(nbu.edomoupravitel.dto.EmployeeDto::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<nbu.edomoupravitel.dto.EmployeeDto> getAvailableEmployees() {
        return employeeRepository.findByCompanyIsNull().stream()
                .map(nbu.edomoupravitel.dto.EmployeeDto::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

}
