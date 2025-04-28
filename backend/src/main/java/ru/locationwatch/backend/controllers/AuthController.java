package ru.locationwatch.backend.controllers;

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
import ru.locationwatch.backend.models.Person;
import ru.locationwatch.backend.services.AuthService;
import ru.locationwatch.backend.services.TokenService;
import ru.locationwatch.backend.util.PersonErrorResponse;
import ru.locationwatch.backend.util.PersonValidator;
import ru.locationwatch.backend.util.RegistrationException;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ModelMapper modelMapper;
    private final TokenService tokenService;
    private final PersonValidator personValidator;

    @Autowired
    public AuthController(
            AuthService authService,
            ModelMapper modelMapper,
            TokenService tokenService,
            PersonValidator personValidator) {
        this.authService = authService;
        this.modelMapper = modelMapper;
        this.tokenService = tokenService;
        this.personValidator = personValidator;
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
        //personValidator.validate(person, bindingResult);
        validatePerson(bindingResult);

        authService.check(person);

        String accessToken = tokenService.generateAccessToken(person);
        String refreshToken = tokenService.generateRefreshToken(person);
        return new JWTResponse(accessToken, refreshToken);
    }

    // TODO: endpoint to get new access token

    // TODO: endpoint to get new refresh token

    public void validatePerson(BindingResult bindingResult) {
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
    private ResponseEntity<PersonErrorResponse> handleException(RegistrationException ex) {
        PersonErrorResponse response = new PersonErrorResponse(
                ex.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private Person convertToPerson(AuthDTO authDTO) {
        return modelMapper.map(authDTO, Person.class);
    }

}
