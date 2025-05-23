package ru.locationwatch.backend.dto;

import java.time.Instant;

public class ViolationMessage {

    private int clientId;

    private String zoneTitle;

    private Instant timestamp;

    public ViolationMessage(int clientId, String zoneTitle, Instant timestamp) {
        this.clientId = clientId;
        this.zoneTitle = zoneTitle;
        this.timestamp = timestamp;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getZoneTitle() {
        return zoneTitle;
    }

    public void setZoneTitle(String zoneTitle) {
        this.zoneTitle = zoneTitle;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
