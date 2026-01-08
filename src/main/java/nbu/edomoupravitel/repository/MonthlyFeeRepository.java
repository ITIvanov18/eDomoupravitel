package nbu.edomoupravitel.repository;

import nbu.edomoupravitel.entity.MonthlyFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MonthlyFeeRepository extends JpaRepository<MonthlyFee, Long> {

        // сумиране на ПЛАТЕНИ такси по компания
        @Query("SELECT SUM(mf.amount) FROM MonthlyFee mf " +
            "WHERE mf.isPaid = true " +
            "AND mf.apartment.building.employee.company.id = :companyId")
        BigDecimal sumPaidByCompany(Long companyId);

        // проверка дали вече са начислени такси за този месец
        boolean existsByMonthAndYear(int month, int year);

        // СПРАВКИ ЗА ДЪЛЖИМИ СУМИ
        // дължимо по компания
        @Query("SELECT SUM(mf.amount) FROM MonthlyFee mf " +
                        "WHERE mf.isPaid = false " +
                        "AND mf.apartment.building.employee.company.id = :companyId")
        BigDecimal sumUnpaidByCompany(Long companyId);

        // дължимо по служител
        @Query("SELECT SUM(mf.amount) FROM MonthlyFee mf " +
                        "WHERE mf.isPaid = false " +
                        "AND mf.apartment.building.employee.id = :employeeId")
        BigDecimal sumUnpaidByEmployee(Long employeeId);

        // дължимо по сграда
        @Query("SELECT SUM(mf.amount) FROM MonthlyFee mf " +
                        "WHERE mf.isPaid = false " +
                        "AND mf.apartment.building.id = :buildingId")
        BigDecimal sumUnpaidByBuilding(Long buildingId);

        // списък с неплатени такси за конкретен апартамент (за да можем да ги платим)
        List<MonthlyFee> findByApartmentIdAndIsPaidFalse(Long apartmentId);
}