package ru.locationwatch.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.locationwatch.backend.models.Coordinate;
import ru.locationwatch.backend.models.Zone;
import ru.locationwatch.backend.repositories.ZonesRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ZonesService {

    private final ZonesRepository zonesRepository;

    @Autowired
    public ZonesService(ZonesRepository zonesRepository) {
        this.zonesRepository = zonesRepository;
    }

    public List<Zone> findAll() {
        return zonesRepository.findAll();
    }

    @Transactional
    public void save(Zone zone) {
        zonesRepository.save(zone);
    }

    @Transactional
    public void delete(int id) {
        zonesRepository.deleteById(id);
    }
}
