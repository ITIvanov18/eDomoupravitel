package nbu.edomoupravitel.repository;

import nbu.edomoupravitel.entity.Apartment;
import nbu.edomoupravitel.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    // SELECT * FROM apartments WHERE building_id = ?
    List<Apartment> findByBuilding(Building building);
}
