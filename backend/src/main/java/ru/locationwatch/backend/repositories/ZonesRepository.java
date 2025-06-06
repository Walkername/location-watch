package ru.locationwatch.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.locationwatch.backend.models.Zone;

@Repository
public interface ZonesRepository extends JpaRepository<Zone, Integer> {
}
