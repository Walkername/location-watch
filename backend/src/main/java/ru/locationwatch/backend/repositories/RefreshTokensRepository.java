package ru.locationwatch.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.locationwatch.backend.models.RefreshToken;

import java.util.Optional;

@Repository
public interface RefreshTokensRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByPersonId(int personId);

    void deleteByPersonId(int personId);

}
