package ru.locationwatch.backend.dto;

import ru.locationwatch.backend.models.Zone;

import java.time.Instant;
import java.util.List;

public class ViolationMessage {

    private int clientId;

    private List<Zone> crossedZones;

    private double latitude;

    private double longitude;

    private double speed;

    private Instant timestamp;

    public ViolationMessage(int clientId, List<Zone> crossedZones, double latitude, double longitude, double speed, Instant timestamp) {
        this.clientId = clientId;
        this.crossedZones = crossedZones;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.timestamp = timestamp;
    }

    public int getClientId() {
        return clientId;
    }

    public List<Zone> getCrossedZones() {
        return crossedZones;
    }

    public void setCrossedZones(List<Zone> crossedZones) {
        this.crossedZones = crossedZones;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
