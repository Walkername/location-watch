package ru.locationwatch.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.locationwatch.backend.dto.GPSDataRequest;
import ru.locationwatch.backend.dto.ViolationMessage;
import ru.locationwatch.backend.models.Coordinate;
import ru.locationwatch.backend.models.Zone;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@EnableScheduling
public class MqttMessageService {

    private final ZonesService zonesService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicReference<RTree<Zone, Geometry>> zonesIndex = new AtomicReference<>(RTree.create());
    private volatile List<Zone> cachedZones = List.of();

    @Autowired
    public MqttMessageService(ZonesService zonesService, SimpMessagingTemplate messagingTemplate) {
        this.zonesService = zonesService;
        this.messagingTemplate = messagingTemplate;
        refreshZoneCaches();
    }

    @Scheduled(fixedRate = 300_000)
    public void refreshZoneCaches() {
        List<Zone> freshZones = zonesService.findAll();
        RTree<Zone, Geometry> freshIndex = RTree.create();
        for (Zone zone : freshZones) {
            List<Coordinate> area = zone.getArea();
            double minLon = Double.MAX_VALUE;
            double maxLon = Double.MIN_VALUE;
            double minLat = Double.MAX_VALUE;
            double maxLat = Double.MIN_VALUE;

            for (Coordinate coordinate : area) {
                double lat = coordinate.getX();
                double lon = coordinate.getY();
                minLon = Math.min(minLon, lon);
                maxLon = Math.max(maxLon, lon);
                minLat = Math.min(minLat, lat);
                maxLat = Math.max(maxLat, lat);
            }

            Rectangle bounds = Geometries.rectangle(minLon, maxLon, minLat, maxLat);
            freshIndex = freshIndex.add(zone, bounds);
        }

        System.out.println("Cached zones have been refreshed");
        cachedZones = freshZones;
        zonesIndex.set(freshIndex);
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        // Convert message to existing object
        GPSDataRequest gpsData = parseGPSData(message);
        if (gpsData == null) {
            return;
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

        checkPointAgainstZones(test, gpsData.getClientId());
    }

    private void checkPointAgainstZones(Coordinate point, int clientId) {
        double lon = point.getY();
        double lat = point.getX();
        Point geoPoint = Geometries.point(lon, lat);

        RTree<Zone, Geometry> currentIndex = zonesIndex.get();

        // Search candidate zones via bounding box
        Iterable<Zone> candidates = currentIndex
                .search(geoPoint)
                .filter(entry -> entry.geometry().intersects(geoPoint))
                .map(Entry::value)
                .toBlocking()
                .toIterable();

        List<Zone> crossedZones = new ArrayList<>();
        for (Zone zone : candidates) {
            if (isPointInZone(point, zone.getArea())) {
                crossedZones.add(zone);
            }
        }

        if (!crossedZones.isEmpty()) {
            ViolationMessage violationMessage = new ViolationMessage(
                    clientId,
                    crossedZones,
                    Instant.now()
            );
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

    private GPSDataRequest parseGPSData(Message<?> message) {
        try {
            String json = message.getPayload().toString();
            return new ObjectMapper().readValue(json, GPSDataRequest.class);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
