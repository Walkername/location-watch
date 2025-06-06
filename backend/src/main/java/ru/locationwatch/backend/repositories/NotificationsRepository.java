package ru.locationwatch.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.locationwatch.backend.models.FirebaseToken;

import java.util.Optional;

@Repository
public interface NotificationsRepository extends JpaRepository<FirebaseToken, Integer> {

    void deleteByPersonIdAndToken(int personId, String token);

    Optional<FirebaseToken> findByPersonId(int personId);

}
