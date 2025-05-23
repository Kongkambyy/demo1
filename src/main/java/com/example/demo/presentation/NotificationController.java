package com.example.demo.presentation;

import com.example.demo.domain.entities.Notification;
import com.example.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.example.demo.data.repository.NotificationRepository;
import com.example.demo.data.util.LoggerUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationController(GetNotificationsUseCase getNotificationsUseCase,
                                  NotificationRepository notificationRepository) {
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/notifications")
    public String notifications(Model model, HttpSession session) {
        // Check if user is logged in
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Get all notifications for the user
            List<Notification> allNotifications = getNotificationsUseCase.getAllForUser(userId);
            List<Notification> unreadNotifications = getNotificationsUseCase.getUnreadForUser(userId);

            // Sort notifications by created date (most recent first)
            allNotifications.sort((n1, n2) -> n2.getCreatedDate().compareTo(n1.getCreatedDate()));

            model.addAttribute("notifications", allNotifications);
            model.addAttribute("unreadCount", unreadNotifications.size());
            model.addAttribute("totalCount", allNotifications.size());

            LoggerUtility.logEvent("User " + userId + " viewed notifications page");

            return "notifications";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading notifications for user " + userId + ": " + e.getMessage());
            model.addAttribute("error", "An error occurred while loading notifications");
            model.addAttribute("notifications", List.of());
            model.addAttribute("unreadCount", 0);
            model.addAttribute("totalCount", 0);
            return "notifications";
        }
    }

    @PostMapping("/notifications/mark-read")
    public String markAsRead(@RequestParam("notificationId") String notificationId,
                             @RequestParam(value = "returnUrl", required = false, defaultValue = "/notifications") String returnUrl,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Verify the notification belongs to the user
            Notification notification = getNotificationsUseCase.getById(notificationId, userId);

            // Mark as read
            notificationRepository.markAsRead(notificationId);

            LoggerUtility.logEvent("User " + userId + " marked notification " + notificationId + " as read");

            return "redirect:" + returnUrl;
        } catch (Exception e) {
            LoggerUtility.logError("Error marking notification as read for user " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Could not mark notification as read");
            return "redirect:" + returnUrl;
        }
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllAsRead(HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            notificationRepository.markAllAsRead(userId);
            LoggerUtility.logEvent("User " + userId + " marked all notifications as read");
            redirectAttributes.addFlashAttribute("success", "All notifications marked as read");

            return "redirect:/notifications";
        } catch (Exception e) {
            LoggerUtility.logError("Error marking all notifications as read for user " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Could not mark all notifications as read");
            return "redirect:/notifications";
        }
    }

    @PostMapping("/notifications/delete")
    public String deleteNotification(@RequestParam("notificationId") String notificationId,
                                     @RequestParam(value = "returnUrl", required = false, defaultValue = "/notifications") String returnUrl,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Verify the notification belongs to the user before deleting
            Notification notification = getNotificationsUseCase.getById(notificationId, userId);

            notificationRepository.delete(notificationId);
            LoggerUtility.logEvent("User " + userId + " deleted notification " + notificationId);

            return "redirect:" + returnUrl;
        } catch (Exception e) {
            LoggerUtility.logError("Error deleting notification for user " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Could not delete notification");
            return "redirect:" + returnUrl;
        }
    }

    @PostMapping("/notifications/clear-all")
    public String clearAllNotifications(HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            notificationRepository.deleteAllForUser(userId);
            LoggerUtility.logEvent("User " + userId + " cleared all notifications");
            redirectAttributes.addFlashAttribute("success", "All notifications cleared");

            return "redirect:/notifications";
        } catch (Exception e) {
            LoggerUtility.logError("Error clearing all notifications for user " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Could not clear all notifications");
            return "redirect:/notifications";
        }
    }
}