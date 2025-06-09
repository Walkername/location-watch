package ru.locationwatch.backend.controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.locationwatch.backend.dto.AuthDTO;
import ru.locationwatch.backend.dto.JWTResponse;
import ru.locationwatch.backend.dto.RefreshTokenRequest;
import ru.locationwatch.backend.models.Person;
import ru.locationwatch.backend.models.RefreshToken;
import ru.locationwatch.backend.services.AuthService;
import ru.locationwatch.backend.services.PeopleService;
import ru.locationwatch.backend.services.TokenService;
import ru.locationwatch.backend.util.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ModelMapper modelMapper;
    private final TokenService tokenService;
    private final PersonValidator personValidator;
    private final PeopleService peopleService;

    @Autowired
    public AuthController(
            AuthService authService,
            ModelMapper modelMapper,
            TokenService tokenService,
            PersonValidator personValidator, PeopleService peopleService) {
        this.authService = authService;
        this.modelMapper = modelMapper;
        this.tokenService = tokenService;
        this.personValidator = personValidator;
        this.peopleService = peopleService;
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(
            @RequestBody @Valid AuthDTO authDTO,
            BindingResult bindingResult
    ) {
        Person person = convertToPerson(authDTO);
        personValidator.validate(person, bindingResult);
        validatePerson(bindingResult);

        authService.register(person);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/login")
    public JWTResponse login(
            @RequestBody @Valid AuthDTO authDTO,
            BindingResult bindingResult
    ) {
        Person person = convertToPerson(authDTO);
        validatePerson(bindingResult);

        // Checking if such person exists in DB
        authService.check(person);

        // Getting this person in order to get his ID
        Optional<Person> newPerson = peopleService.findByUsername(person.getUsername());
        if (newPerson.isEmpty()) {
            throw new LoginException("Invalid username/password");
        }
        Person currentPerson = newPerson.get();

        // Generating a pair of tokens
        String accessToken = tokenService.generateAccessToken(currentPerson);
        String refreshToken = tokenService.generateRefreshToken(currentPerson);

        // Update current refresh token on new refresh token
        authService.updateRefreshToken(currentPerson.getId(), refreshToken);

        return new JWTResponse(accessToken, refreshToken);
    }

    @PostMapping("/refresh")
    public JWTResponse refreshTokens(
            @RequestBody @Valid RefreshTokenRequest refreshTokenRequest
    ) {
        int userId;

        try {
            // Checking if refresh token is valid
            DecodedJWT jwt = tokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
            userId = jwt.getClaim("id").asInt();

            // Getting current user's refresh token in order to compare
            RefreshToken refreshToken = authService.findRefreshToken(userId);
            if (refreshToken == null || !refreshToken.getRefreshToken().equals(refreshTokenRequest.getRefreshToken())) {
                throw new RefreshException("Invalid refresh token");
            }
        } catch (JWTVerificationException e) {
            // If jwt refresh token is invalid, then return nothing
            System.out.println("Here2");
            throw new RefreshException("Invalid refresh token");
        }

        // Getting person by id in order to generate tokens
        Person person = peopleService.findOne(userId);

        // Generating a pair of tokens
        String accessToken = tokenService.generateAccessToken(person);
        String refreshToken = tokenService.generateRefreshToken(person);

        // Update current refresh token on new refresh token
        authService.updateRefreshToken(userId, refreshToken);

        return new JWTResponse(accessToken, refreshToken);
    }

    private void validatePerson(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMsg.append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage())
                        .append(";");
            }

            throw new RegistrationException(errorMsg.toString());
        }
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(RefreshException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(RegistrationException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(LoginException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private Person convertToPerson(AuthDTO authDTO) {
        return modelMapper.map(authDTO, Person.class);
    }

}
