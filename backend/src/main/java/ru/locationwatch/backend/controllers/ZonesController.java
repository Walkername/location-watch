package ru.locationwatch.backend.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.locationwatch.backend.dto.ZoneDTO;
import ru.locationwatch.backend.models.Zone;
import ru.locationwatch.backend.services.ZonesService;
import ru.locationwatch.backend.util.ErrorResponse;
import ru.locationwatch.backend.util.ZoneNotCreatedException;

import java.util.List;

@RestController
@RequestMapping("/zones")
public class ZonesController {

    private final ZonesService zonesService;
    private final ModelMapper modelMapper;

    @Autowired
    public ZonesController(
            ZonesService zonesService,
            ModelMapper modelMapper
    ) {
        this.zonesService = zonesService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/add")
    public ResponseEntity<HttpStatus> addZone(
            @RequestBody @Valid ZoneDTO zoneDTO,
            BindingResult bindingResult
    ) {
        Zone zone = convertToZone(zoneDTO);
        validateZone(bindingResult);

        zonesService.save(zone);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping()
    public List<Zone> getZones() {
        // Currently ZoneDTO is request from frontend, but it's also suitable to send it to frontend
        // But it's better to create another dto for response to frontend
        //return zonesService.findAll().stream().map(this::convertToZoneDTO).collect(Collectors.toList());
        // In order to delete zones, I need to know its id
        // So just returning zone with all its information
        return zonesService.findAll();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteZone(
            @PathVariable("id") int id
    ) {
        zonesService.delete(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    public void validateZone(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMsg.append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage())
                        .append(";");
            }

            throw new ZoneNotCreatedException(errorMsg.toString());
        }
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(ZoneNotCreatedException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private Zone convertToZone(ZoneDTO zoneDTO) {
        return modelMapper.map(zoneDTO, Zone.class);
    }

    private ZoneDTO convertToZoneDTO(Zone zone) {
        return modelMapper.map(zone, ZoneDTO.class);
    }
}
