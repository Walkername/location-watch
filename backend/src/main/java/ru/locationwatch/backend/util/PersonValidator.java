package ru.locationwatch.backend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.locationwatch.backend.models.Person;
import ru.locationwatch.backend.services.PeopleService;

@Component
public class PersonValidator implements Validator {

    private final PeopleService peopleService;

    @Autowired
    public PersonValidator(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Person user = (Person) target;

        if (peopleService.findByUsername(user.getUsername()).isPresent()) {
            errors.rejectValue("username", "", "This username is already in use");
        }
    }

}
