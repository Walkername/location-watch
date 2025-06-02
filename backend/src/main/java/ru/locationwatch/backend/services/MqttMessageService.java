package ru.locationwatch.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.locationwatch.backend.dto.GPSDataRequest;
import ru.locationwatch.backend.dto.ViolationMessage;
import ru.locationwatch.backend.models.Coordinate;
import ru.locationwatch.backend.models.Zone;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class MqttMessageService {

    private final ZonesService zonesService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MqttMessageService(ZonesService zonesService, SimpMessagingTemplate messagingTemplate) {
        this.zonesService = zonesService;
        this.messagingTemplate = messagingTemplate;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        ViolationMessage violationMessage = null;
        // Convert message to existing object
        GPSDataRequest gpsData = new GPSDataRequest();
        try {
            String json = message.getPayload().toString();
            gpsData = new ObjectMapper().readValue(json, GPSDataRequest.class);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        System.out.println(gpsData);

        // Creating object of coordinate
        Coordinate point = new Coordinate(
                gpsData.getLatitude(),
                gpsData.getLongitude()
        );

        // test point
        Coordinate test = new Coordinate(
                59.953229,
                30.314315
        );

        // Getting all zones to check if user is inside of at least in one of them
        // This method is ineffective
        // Send request to DB each gps data
        // TODO: maybe add cache or other ways to reduce the load on the DB
        List<Zone> zones = zonesService.findAll();

        // Iteration on all zones
        for (Zone zone : zones) {
            List<Coordinate> area = zone.getArea();

            // Location is in restricted zone:
            boolean isPointInside = isPointInZone(test, area);
            System.out.println("The user in restricted #" + zone.getTitle() + " zone: " + isPointInside);
            if (isPointInside) {
                // Creating violation message to send to admin frontend
                violationMessage = new ViolationMessage(
                        0,
                        zone.getTitle(),
                        Instant.now()
                );

                // Creating notification message to send to mobile client
                // TODO:
            }
        }

        // Send to admin frontend
        if (violationMessage != null) {
            System.out.println("Here");
            messagingTemplate.convertAndSend("/topic/violations", violationMessage);
        }
    }

    private boolean isPointInZone(Coordinate point, List<Coordinate> zone) {
        double x = point.getY();
        double y = point.getX();
        boolean inside = false;

        for (int i = 0, j = zone.size() - 1; i < zone.size(); j = i++) {
            double xi = zone.get(i).getY();
            double yi = zone.get(i).getX();
            double xj = zone.get(j).getY();
            double yj = zone.get(j).getX();

            // Check if point is a vertex
            if (x == xi && y == yi) return true;

            // Check if the edge crosses the ray from (x, y) to the right
            boolean intersect = ((yi > y) != (yj > y))
                    && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }
}
