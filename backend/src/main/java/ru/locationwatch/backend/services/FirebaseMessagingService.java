package ru.locationwatch.backend.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.locationwatch.backend.dto.NotificationMessage;
import ru.locationwatch.backend.models.FirebaseToken;
import ru.locationwatch.backend.repositories.NotificationsRepository;

@Service
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationsRepository notificationsRepository;

    @Autowired
    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging, NotificationsRepository notificationsRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.notificationsRepository = notificationsRepository;
    }

    public void saveFirebaseToken(FirebaseToken firebaseToken) {
        notificationsRepository.save(firebaseToken);
    }

    public String sendNotification(NotificationMessage request) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();

        Message message = Message.builder()
                .setToken(request.getToken())
                .setNotification(notification)
                .build();

        return firebaseMessaging.send(message);
    }

}
