package nbu.edomoupravitel.repository;

import nbu.edomoupravitel.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    @Query("SELECT new nbu.edomoupravitel.dto.CompanyDto(c.id, c.name, COUNT(b)) "
            +
            "FROM Company c " +
            "LEFT JOIN c.employees e " +
            "LEFT JOIN e.buildings b " +
            "GROUP BY c.id, c.name")
    java.util.List<nbu.edomoupravitel.dto.CompanyDto> findAllWithBuildingCount();
}
