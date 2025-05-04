package ru.locationwatch.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import ru.locationwatch.backend.models.Coordinate;

import java.util.List;

public class ZoneDTO {

    @NotEmpty(message = "Type name should no be empty")
    @Size(max = 100, message = "Type should be less than 100")
    private String typeName;

    // Maybe to find out what annotations
    // can I use to validate collections
    private List<Coordinate> area;

    public ZoneDTO() {}

    public ZoneDTO(String typeName, List<Coordinate> area) {
        this.typeName = typeName;
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

    @Override
    public String toString() {
        return "ZoneDTO{" +
                "typeName='" + typeName + '\'' +
                ", area=" + area +
                '}';
    }
}
