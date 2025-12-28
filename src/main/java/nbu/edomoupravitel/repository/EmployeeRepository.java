package nbu.edomoupravitel.repository;

import nbu.edomoupravitel.entity.Company;
import nbu.edomoupravitel.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // SELECT * FROM employees WHERE company_id = ?
    List<Employee> findByCompany(Company company);

    List<Employee> findByCompanyIsNull();
}
