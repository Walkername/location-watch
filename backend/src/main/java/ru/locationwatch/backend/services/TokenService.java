package ru.locationwatch.backend.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.locationwatch.backend.models.Person;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class TokenService {

    private final PeopleService peopleService;

    @Value("${auth.jwt.secret}")
    private String secret;

    @Autowired
    public TokenService(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    public String generateToken(String username) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        Optional<Person> user = peopleService.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found!");
        }
//        String role = user.get().getRole();

        return JWT.create()
                .withSubject("User details")
                .withClaim("id", user.get().getId())
                .withClaim("username", username)
//                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withIssuer("auth-service")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("auth-service")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt;
    }

}
