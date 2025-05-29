package ru.locationwatch.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.locationwatch.backend.models.FirebaseToken;

@Repository
public interface NotificationsRepository extends JpaRepository<FirebaseToken, Integer> {
}
