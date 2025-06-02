package ru.locationwatch.backend.dto;

import ru.locationwatch.backend.models.Zone;

import java.time.Instant;
import java.util.List;

public class ViolationMessage {

    private int clientId;

    private List<Zone> crossedZones;

    private Instant timestamp;

    public ViolationMessage(int clientId, List<Zone> crossedZones, Instant timestamp) {
        this.clientId = clientId;
        this.crossedZones = crossedZones;
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
}
