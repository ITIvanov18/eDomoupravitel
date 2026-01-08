package nbu.edomoupravitel.repository;

import nbu.edomoupravitel.entity.MonthlyFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MonthlyFeeRepository extends JpaRepository<MonthlyFee, Long> {

        // Проверка дали вече сме начислили такси за този месец (за да не дублираме
        // бутона)
        boolean existsByMonthAndYear(int month, int year);

        // --- СПРАВКИ ЗА ДЪЛЖИМИ СУМИ (UNPAID) ---

        // 1. Дължимо по Компания
        @Query("SELECT SUM(mf.amount) FROM MonthlyFee mf " +
                        "WHERE mf.isPaid = false " +
                        "AND mf.apartment.building.employee.company.id = :companyId")
        BigDecimal sumUnpaidByCompany(Long companyId);

        // 2. Дължимо по Служител
        @Query("SELECT SUM(mf.amount) FROM MonthlyFee mf " +
                        "WHERE mf.isPaid = false " +
                        "AND mf.apartment.building.employee.id = :employeeId")
        BigDecimal sumUnpaidByEmployee(Long employeeId);

        // 3. Дължимо по Сграда
        @Query("SELECT SUM(mf.amount) FROM MonthlyFee mf " +
                        "WHERE mf.isPaid = false " +
                        "AND mf.apartment.building.id = :buildingId")
        BigDecimal sumUnpaidByBuilding(Long buildingId);

        // Списък с неплатени такси за конкретен апартамент (за да можем да ги платим)
        List<MonthlyFee> findByApartmentIdAndIsPaidFalse(Long apartmentId);
}