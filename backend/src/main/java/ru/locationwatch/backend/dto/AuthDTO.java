package ru.locationwatch.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class AuthDTO {

    @NotEmpty(message = "Username should not be empty")
    @Size(min = 5, max = 20, message = "Username size should be greater than 1 and less than 20")
    private String username;

    @NotEmpty(message = "Password should not be empty")
    @Size(min = 5, max = 50, message = "Password size should be greater than 1 and less than 50")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
