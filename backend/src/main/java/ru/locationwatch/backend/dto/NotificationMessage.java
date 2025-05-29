package ru.locationwatch.backend.dto;

/**
 * This DTO is needed to send it to mobile client
 */

public class NotificationMessage {

    private String title;

    private String body;

    private String token;

    public NotificationMessage() {}

    public NotificationMessage(String title, String body, String token) {
        this.title = title;
        this.body = body;
        this.token = token;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
