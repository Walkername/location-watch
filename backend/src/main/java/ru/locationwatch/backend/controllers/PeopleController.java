package ru.locationwatch.backend.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.locationwatch.backend.dto.PersonResponse;
import ru.locationwatch.backend.models.Person;
import ru.locationwatch.backend.services.PeopleService;
import ru.locationwatch.backend.services.TokenService;
import ru.locationwatch.backend.util.PersonValidator;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class PeopleController {

    private final PeopleService peopleService;
    private final PersonValidator personValidator;
    private final ModelMapper modelMapper;
    private final TokenService tokenService;

    @Autowired
    public PeopleController(
            PeopleService peopleService,
            PersonValidator personValidator,
            ModelMapper modelMapper,
            TokenService tokenService
    ) {
        this.peopleService = peopleService;
        this.personValidator = personValidator;
        this.modelMapper = modelMapper;
        this.tokenService = tokenService;
    }

    @GetMapping("/{id}")
    public PersonResponse getUser(
            @PathVariable("id") int id
    ) {
        return convertToPersonResponse(peopleService.findOne(id));
    }

    private PersonResponse convertToPersonResponse(Person person) {
        return modelMapper.map(person, PersonResponse.class);
    }

}
