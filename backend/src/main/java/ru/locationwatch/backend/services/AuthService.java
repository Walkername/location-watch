package ru.locationwatch.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.locationwatch.backend.models.Person;
import ru.locationwatch.backend.models.RefreshToken;
import ru.locationwatch.backend.repositories.PeopleRepository;
import ru.locationwatch.backend.repositories.RefreshTokensRepository;
import ru.locationwatch.backend.util.LoginException;
import ru.locationwatch.backend.util.RegistrationException;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final PeopleRepository peopleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokensRepository refreshTokensRepository;

    @Autowired
    public AuthService(
            PeopleRepository peopleRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokensRepository refreshTokensRepository) {
        this.peopleRepository = peopleRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokensRepository = refreshTokensRepository;
    }

    @Transactional
    public void register(Person person) {
        if (peopleRepository.existsById(person.getId())) {
            throw new RegistrationException("User already exists");
        }

        person.setPassword(passwordEncoder.encode(person.getPassword()));
        person.setRole("ROLE_USER");

        peopleRepository.save(person);
    }

    public void check(Person user) {
        Optional<Person> personOptional = peopleRepository.findByUsername(user.getUsername());
        if (personOptional.isEmpty()) {
            throw new LoginException("User was not found");
        }

        Person personGet = personOptional.get();

        if (!passwordEncoder.matches(user.getPassword(), personGet.getPassword())) {
            throw new LoginException("Wrong password");
        }
    }

    @Transactional
    public void saveRefreshToken(int personId, String refreshToken) {
        RefreshToken newRefreshToken = new RefreshToken(personId, refreshToken);
        refreshTokensRepository.save(newRefreshToken);
    }

    public RefreshToken findRefreshToken(int personId) {
        Optional<RefreshToken> refreshTokenOptional = refreshTokensRepository.findByPersonId(personId);
        return refreshTokenOptional.orElse(null);
    }

    @Transactional
    public void deleteRefreshToken(int personId) {
        refreshTokensRepository.deleteByPersonId(personId);
    }

    @Transactional
    public void updateRefreshToken(int personId, String newRefreshToken) {
        RefreshToken refreshToken = findRefreshToken(personId);
        refreshToken.setRefreshToken(newRefreshToken);
    }

}
