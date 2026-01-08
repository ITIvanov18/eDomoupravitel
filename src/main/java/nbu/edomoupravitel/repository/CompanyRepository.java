package nbu.edomoupravitel.repository;

import nbu.edomoupravitel.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import nbu.edomoupravitel.dto.CompanyDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * извлича всички компании заедно с пресметнат брой сгради и финансови настройки
 * constructor expression-ът (new CompanyDto...) връща директно оптимизиран DTO обект,
 * вместо да pull-ва тежките Entity обекти и да ги преобразува в Java обекти
 */

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    @Query("SELECT new nbu.edomoupravitel.dto.CompanyDto(" +
            "c.id, c.name, COUNT(b), c.taxPerSqM, c.elevatorTax, c.petTax) " +
            "FROM Company c " +
            "LEFT JOIN c.employees e " +
            "LEFT JOIN e.buildings b " +
            "GROUP BY c.id, c.name, c.taxPerSqM, c.elevatorTax, c.petTax")
    List<CompanyDto> findAllWithBuildingCount();
}
