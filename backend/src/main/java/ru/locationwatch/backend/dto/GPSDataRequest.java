package ru.locationwatch.backend.dto;

public class GPSDataRequest {

    private int clientId;

    private double latitude;

    private double longitude;

    private double speed;

    public GPSDataRequest() {}

    public GPSDataRequest(int clientId, double latitude, double longitude, double speed) {
        this.clientId = clientId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
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

    @Override
    public String toString() {
        return "GPSDataRequest{" +
                "clientId='" + clientId + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", speed='" + speed + '\'' +
                '}';
    }

}
