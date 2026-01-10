package nbu.edomoupravitel.repository;

import nbu.edomoupravitel.entity.Apartment;
import nbu.edomoupravitel.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByApartment(Apartment apartment);

    // сумира всички плащания за дадена фирма
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.apartment.building.employee.company.id = :companyId")
    java.math.BigDecimal sumTotalPaymentsByCompany(Long companyId);
}
