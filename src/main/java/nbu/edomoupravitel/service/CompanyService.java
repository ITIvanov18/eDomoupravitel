package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.CompanyDto;

import java.util.List;

// интерфейсът дефинира какво прави програмата, без да казва как
public interface CompanyService {
    CompanyDto createCompany(CompanyDto companyDto);

    CompanyDto updateCompany(Long id, CompanyDto companyDto);

    void deleteCompany(Long id);

    CompanyDto getCompany(Long id);

    List<CompanyDto> getAllCompanies();
}
