package com.example.demo.domain.usecases.Notifications;

import com.example.demo.data.repository.NotificationRepository;
import com.example.demo.domain.entities.Notification;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GetNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public GetNotificationsUseCase(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getAllForUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> getUnreadForUser(String userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }

    public int countUnread(String userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }

    public Notification getById(String notificationId, String userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);

        if (notificationOpt.isEmpty()) {
            LoggerUtility.logWarning("Notification not found: " + notificationId);
            throw new RuntimeException("Notification not found: " + notificationId);
        }

        Notification notification = notificationOpt.get();

        if (!notification.getUserID().equals(userId)) {
            throw new RuntimeException("Access denied to this notification");
        }

        return notification;
    }
}