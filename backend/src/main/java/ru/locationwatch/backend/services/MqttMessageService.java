package ru.locationwatch.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.locationwatch.backend.models.Coordinate;
import ru.locationwatch.backend.models.Zone;

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
        System.out.println(message.getPayload());
        String payload = message.getPayload().toString();
        String[] split = payload.split(",");
        double latitude = Double.parseDouble(split[0].split(":")[1]);
        double longitude = Double.parseDouble(split[1].split(":")[1]);
        //double speed = Double.parseDouble(split[2].split(":")[1]);
        Coordinate point = new Coordinate(latitude, longitude);

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
        }

        System.out.println("Location is in restricted zone: " + (intersections % 2 == 1));
        // Send notification that user is in restricted zone
        // TODO: send only to specific user
        if (intersections % 2 == 1) {
            messagingTemplate.convertAndSend("/topic/notifications", "Notification from checking");
        }
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
