package ru.locationwatch.backend.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationsController {

    // TODO: Not sure that this controller is needed!

//    @MessageMapping("/sendMessage")
//    @SendTo("/topic/notifications")
//    public String sendMessage(String message) {
//        return message;
//    }

}
