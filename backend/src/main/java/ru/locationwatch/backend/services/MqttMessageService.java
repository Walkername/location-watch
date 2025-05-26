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
import java.util.ArrayList;
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
        List<ViolationMessage> violations = new ArrayList<>();

        GPSDataRequest gpsData = new GPSDataRequest();
        try {
            String json = message.getPayload().toString();
            gpsData = new ObjectMapper().readValue(json, GPSDataRequest.class);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        System.out.println(gpsData);

        Coordinate point = new Coordinate(
                gpsData.getLatitude(),
                gpsData.getLongitude()
        );

        // This method is ineffective
        // Send request to DB each gps data
        // TODO: maybe add cache or other ways to reduce the load on the DB
        List<Zone> zones = zonesService.findAll();

        int intersections = 0;
        for (Zone zone : zones) {
            List<Coordinate> area = zone.getArea();
            int n = zone.getArea().size();

            for (int i = 0; i < n; i++) {
                Coordinate current = area.get(i);
                Coordinate next = area.get((i + 1) % n);

                if (rayCrossesSegment(point, current, next)) {
                    intersections++;
                }
            }

            if (intersections % 2 == 1) {
                ViolationMessage violationMessage = new ViolationMessage(
                        0,
                        zone.getTitle(),
                        Instant.now()
                );
                violations.add(violationMessage);
            }
        }

        // Location is in restricted zone:
        // intersections % 2 == 1 => odd -> true; even -> false.

        // Send notification that user is in restricted zone
        // TODO: send only to specific user
//        if (intersections % 2 == 1) {
//            messagingTemplate.convertAndSend("/topic/violations", "Notification from checking");
//        }

        // it's only example to send
        ViolationMessage violationMessage = new ViolationMessage(
                0,
                "walkername",
                Instant.now()
        );
        violations.add(violationMessage);
        messagingTemplate.convertAndSend("/topic/violations", violations);
    }

    private boolean rayCrossesSegment(Coordinate point, Coordinate current, Coordinate next) {
        if (current.getY() < point.getY() == next.getY() > point.getY()) {
            return false;
        }

        double intersectionX = (next.getX() - current.getX()) * (point.getY() - current.getY())
                / (next.getY() - current.getY()) + current.getX();

        return intersectionX > point.getX();
    }
}
