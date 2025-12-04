package nbu.edomoupravitel.service;

import nbu.edomoupravitel.dto.EmployeeDto;
import java.util.List;

// интерфейсът дефинира какво прави програмата, без да казва как
public interface EmployeeService {
    EmployeeDto createEmployee(EmployeeDto employeeDto);

    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);

    void deleteEmployee(Long id);

    EmployeeDto getEmployee(Long id);

    List<EmployeeDto> getAllEmployees();
}
