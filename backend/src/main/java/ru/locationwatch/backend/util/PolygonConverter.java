package ru.locationwatch.backend.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.locationwatch.backend.models.Coordinate;

import java.util.ArrayList;
import java.util.List;

@Converter
public class PolygonConverter implements AttributeConverter<List<Coordinate>, String> {
    @Override
    public String convertToDatabaseColumn(List<Coordinate> area) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < area.size(); i++) {
            Coordinate coord = area.get(i);
            sb.append("(")
                    .append(coord.getLatitude())
                    .append(",")
                    .append(coord.getLongitude())
                    .append(")");
            if (i < area.size() - 1) sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public List<Coordinate> convertToEntityAttribute(String s) {
        List<Coordinate> area = new ArrayList<>();
        String clean = s.replace("(", "").replace(")", "");
        String[] coords = clean.split(",");
        for (int i = 0; i < coords.length - 1; i += 2) {
            Coordinate coordinate = new Coordinate(Double.parseDouble(coords[i]), Double.parseDouble(coords[i + 1]));
            area.add(coordinate);
        }
        return area;
    }
}
