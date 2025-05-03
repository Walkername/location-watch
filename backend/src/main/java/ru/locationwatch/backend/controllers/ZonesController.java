package ru.locationwatch.backend.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.locationwatch.backend.dto.ZoneDTO;
import ru.locationwatch.backend.models.Zone;
import ru.locationwatch.backend.repositories.ZonesRepository;
import ru.locationwatch.backend.services.ZonesService;

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
            @RequestBody ZoneDTO zoneDTO
    ) {
        Zone zone = convertToZone(zoneDTO);
        zonesService.save(zone);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private Zone convertToZone(ZoneDTO zoneDTO) {
        return modelMapper.map(zoneDTO, Zone.class);
    }

}
