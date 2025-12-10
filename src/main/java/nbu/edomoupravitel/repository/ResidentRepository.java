package nbu.edomoupravitel.repository;

import nbu.edomoupravitel.entity.Apartment;
import nbu.edomoupravitel.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {
    List<Resident> findByApartment(Apartment apartment);
}
