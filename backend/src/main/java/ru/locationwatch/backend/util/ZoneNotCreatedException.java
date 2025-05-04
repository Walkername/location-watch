package ru.locationwatch.backend.util;

public class ZoneNotCreatedException extends RuntimeException {
    public ZoneNotCreatedException(String message) {
        super(message);
    }
}
