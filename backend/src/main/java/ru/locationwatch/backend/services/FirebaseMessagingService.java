package ru.locationwatch.backend.services;

import com.google.firebase.messaging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.locationwatch.backend.dto.NotificationMessage;
import ru.locationwatch.backend.models.FirebaseToken;
import ru.locationwatch.backend.repositories.NotificationsRepository;

import java.util.HashMap;
import java.util.Map;

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

    @Transactional
    public void deleteFirebaseToken(int personId, String token) {
        notificationsRepository.deleteByPersonIdAndToken(personId, token);
    }

    public String sendNotification(NotificationMessage request) throws FirebaseMessagingException {
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .setClickAction("OPEN_DETAILS_ACTION")
                        .build()
                )
                .build();

        Map<String, String> data = new HashMap<>();
        data.put("ntf_title", request.getTitle());
        data.put("ntf_body", request.getBody());

        Message message = Message.builder()
                .setToken(request.getToken())
                .setAndroidConfig(androidConfig)
                .putAllData(data)
                .build();

        return firebaseMessaging.send(message);
    }

}
