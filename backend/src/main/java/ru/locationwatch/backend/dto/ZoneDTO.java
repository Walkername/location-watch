package ru.locationwatch.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import ru.locationwatch.backend.models.Coordinate;

import java.util.List;

public class ZoneDTO {

    @NotEmpty(message = "Title should not be empty")
    @Size(max = 30, message = "Title should be less than 30")
    private String title;

    @NotEmpty(message = "Type name should not be empty")
    @Size(max = 30, message = "Type should be less than 30")
    private String typeName;

    @Max(value = 20, message = "Speed should be less or equal than 20")
    private int speed;

    // Maybe to find out what annotations
    // can I use to validate collections
    private List<Coordinate> area;

    public ZoneDTO() {}

    public ZoneDTO(String title, String typeName, int speed, List<Coordinate> area) {
        this.title = title;
        this.typeName = typeName;
        this.speed = speed;
        this.area = area;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<Coordinate> getArea() {
        return area;
    }

    public void setArea(List<Coordinate> area) {
        this.area = area;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "ZoneDTO{" +
                "title='" + title + '\'' +
                ", typeName='" + typeName + '\'' +
                ", speed=" + speed +
                ", area=" + area +
                '}';
    }
}
