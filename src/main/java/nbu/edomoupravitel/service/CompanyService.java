package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.CompanyDto;

import java.util.List;

// интерфейсът дефинира какво прави програмата, без да казва как
public interface CompanyService {
    CompanyDto createCompany(CompanyDto companyDto);

    CompanyDto updateCompany(Long id, CompanyDto companyDto);

    void deleteCompany(Long id);

    CompanyDto getCompany(Long id);

    List<CompanyDto> getAllCompanies(String sort);
    List<CompanyDto> getAllCompanies();

    void assignEmployee(Long companyId, Long employeeId);

    void removeEmployee(Long companyId, Long employeeId);

    void createAndAssignEmployee(Long companyId, nbu.edomoupravitel.dto.EmployeeDto employeeDto);

    List<nbu.edomoupravitel.dto.EmployeeDto> getCompanyEmployees(Long companyId);

    List<nbu.edomoupravitel.dto.EmployeeDto> getAvailableEmployees();
}
