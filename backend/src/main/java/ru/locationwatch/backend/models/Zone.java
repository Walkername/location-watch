package ru.locationwatch.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnTransformer;
import ru.locationwatch.backend.util.PolygonConverter;

import java.util.List;

@Entity
@Table(name = "zone")
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "Type name should no be empty")
    @Size(max = 100, message = "Type should be less than 100")
    @Column(name = "type_name")
    private String typeName;

    @Convert(converter = PolygonConverter.class)
    @ColumnTransformer(write = "?::polygon")
    private List<Coordinate> area;

    public Zone() {}

    public Zone(String typeName, List<Coordinate> area) {
        this.typeName = typeName;
        this.area = area;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        return "Zone{" +
                "id=" + id +
                ", typeName='" + typeName + '\'' +
                ", area=" + area +
                '}';
    }
}
