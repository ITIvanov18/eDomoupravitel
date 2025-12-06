package nbu.edomoupravitel.repository;

import nbu.edomoupravitel.entity.Building;
import nbu.edomoupravitel.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByEmployee(Employee employee);
}
