package ru.locationwatch.backend.controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.locationwatch.backend.dto.FirebaseTokenDTO;
import ru.locationwatch.backend.dto.NotificationMessage;
import ru.locationwatch.backend.models.FirebaseToken;
import ru.locationwatch.backend.services.FirebaseMessagingService;
import ru.locationwatch.backend.services.TokenService;

// Controller to save or delete firebase tokens
// Also can be used to send notifications by certain endpoint "/send-notification"

@RestController
@RequestMapping("/notifications")
public class NotificationsController {

    private final FirebaseMessagingService firebaseMessagingService;
    private final TokenService tokenService;

    @Autowired
    public NotificationsController(FirebaseMessagingService firebaseMessagingService, TokenService tokenService) {
        this.firebaseMessagingService = firebaseMessagingService;
        this.tokenService = tokenService;
    }

    @PostMapping("/send-token")
    public ResponseEntity<String> saveFirebaseToken(
            @RequestBody FirebaseTokenDTO firebaseTokenDTO,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            String jwtToken = authorization.substring(7);
            DecodedJWT jwt = tokenService.validateAccessToken(jwtToken);
            int personId = jwt.getClaim("id").asInt();

            FirebaseToken firebaseToken = new FirebaseToken();
            firebaseToken.setToken(firebaseTokenDTO.getToken());
            firebaseToken.setPersonId(personId);
            firebaseMessagingService.saveFirebaseToken(firebaseToken);
        } catch (JWTVerificationException e) {
            return new ResponseEntity<>("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(HttpStatus.OK.toString());
    }

    // I don't need to specify user id in path
    // cuz I can get user id from jwt token
    // So I don't need to check if path variable user id is equals jwt user id
    @PostMapping("/delete-token")
    public ResponseEntity<String> deleteFirebaseToken(
            @RequestBody FirebaseTokenDTO firebaseTokenDTO,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            // Getting user id from jwt token
            String jwtToken = authorization.substring(7);
            DecodedJWT jwt = tokenService.validateAccessToken(jwtToken);
            int jwtUserId = jwt.getClaim("id").asInt();

            // Delete only token that has jwtUserId
            // Otherwise either there are no tokens with this jwtUserId
            // neither jwtUserId with such token
            firebaseMessagingService.deleteFirebaseToken(jwtUserId, firebaseTokenDTO.getToken());

            return ResponseEntity.ok(HttpStatus.OK.toString());
        } catch (JWTVerificationException e) {
            return new ResponseEntity<>("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/send-notification")
    public String sendNotification(
            @RequestBody NotificationMessage notification
    ) throws FirebaseMessagingException {
        return firebaseMessagingService.sendNotification(notification);
    }

}
