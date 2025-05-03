package ru.locationwatch.backend.dto;

import ru.locationwatch.backend.models.Coordinate;

import java.util.List;

public class ZoneDTO {

    private String typeName;

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
