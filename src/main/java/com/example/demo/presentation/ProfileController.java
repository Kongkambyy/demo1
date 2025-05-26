package com.example.demo.presentation;

import com.example.demo.domain.entities.User;
import com.example.demo.domain.usecases.user.GetUserUseCase;
import com.example.demo.domain.usecases.user.UpdateUserUseCase;
import com.example.demo.domain.usecases.user.DeleteUserUseCase;
import com.example.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.example.demo.exceptions.user.UserNotFoundException;
import com.example.demo.exceptions.user.DuplicateUserException;
import com.example.demo.exceptions.user.InvalidCredentialsException;
import com.example.demo.data.util.LoggerUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;

    @Autowired
    public ProfileController(GetUserUseCase getUserUseCase,
                             UpdateUserUseCase updateUserUseCase,
                             DeleteUserUseCase deleteUserUseCase,
                             GetNotificationsUseCase getNotificationsUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
    }

    private void addNotificationCount(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            try {
                int unreadCount = getNotificationsUseCase.countUnread(userId);
                model.addAttribute("globalUnreadNotificationCount", unreadCount);
            } catch (Exception e) {
                LoggerUtility.logError("Error getting notification count for user " + userId + ": " + e.getMessage());
                model.addAttribute("globalUnreadNotificationCount", 0);
            }
        } else {
            model.addAttribute("globalUnreadNotificationCount", 0);
        }
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            addNotificationCount(model, session);
            User user = getUserUseCase.findByIdOrThrow(userId);
            model.addAttribute("user", user);

            LoggerUtility.logEvent("User " + userId + " accessed profile edit page");
            return "edit-profile";
        } catch (UserNotFoundException e) {
            LoggerUtility.logError("User not found when accessing edit profile: " + userId);
            return "redirect:/login";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading edit profile page for user " + userId + ": " + e.getMessage());
            model.addAttribute("error", "An error occurred while loading your profile");
            return "edit-profile";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("name") String name,
            @RequestParam("alias") String alias,
            @RequestParam("email") String email,
            @RequestParam(value = "number", required = false) String number,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "password", required = false) String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Name is required");
                return "redirect:/profile/edit";
            }

            if (alias == null || alias.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Username is required");
                return "redirect:/profile/edit";
            }

            if (email == null || email.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Email is required");
                return "redirect:/profile/edit";
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                redirectAttributes.addFlashAttribute("error", "Please enter a valid email address");
                return "redirect:/profile/edit";
            }

            User updatedUser = updateUserUseCase.execute(
                    userId,
                    name.trim(),
                    alias.trim(),
                    email.trim(),
                    number != null && !number.trim().isEmpty() ? number.trim() : null,
                    address != null && !address.trim().isEmpty() ? address.trim() : null,
                    password != null && !password.trim().isEmpty() ? password : null
            );

            session.setAttribute("userName", updatedUser.getName());
            session.setAttribute("userEmail", updatedUser.getEmail());

            LoggerUtility.logEvent("User " + userId + " successfully updated profile");
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/profile/edit";

        } catch (DuplicateUserException e) {
            LoggerUtility.logWarning("Profile update failed - duplicate email: " + email + " for user: " + userId);
            redirectAttributes.addFlashAttribute("error", "Email is already in use by another account");
            return "redirect:/profile/edit";
        } catch (UserNotFoundException e) {
            LoggerUtility.logError("Profile update failed - user not found: " + userId);
            return "redirect:/login";
        } catch (Exception e) {
            LoggerUtility.logError("Profile update error for user " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating your profile. Please try again.");
            return "redirect:/profile/edit";
        }
    }

    @GetMapping("/profile/edit/cancel")
    public String cancelEdit() {
        return "redirect:/info";
    }

    @GetMapping("/profile/delete")
    public String confirmDeleteAccount(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            addNotificationCount(model, session);
            User user = getUserUseCase.findByIdOrThrow(userId);
            model.addAttribute("user", user);

            LoggerUtility.logEvent("User " + userId + " accessed account deletion confirmation page");
            return "delete-account";
        } catch (UserNotFoundException e) {
            LoggerUtility.logError("User not found when accessing delete account page: " + userId);
            return "redirect:/login";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading delete account page for user " + userId + ": " + e.getMessage());
            return "redirect:/settings";
        }
    }

    @PostMapping("/profile/delete/confirm")
    public String deleteAccount(
            @RequestParam("password") String password,
            @RequestParam("confirmation") String confirmation,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            if (!"DELETE MY ACCOUNT".equals(confirmation)) {
                redirectAttributes.addFlashAttribute("error", "You must type 'DELETE MY ACCOUNT' exactly to confirm");
                return "redirect:/profile/delete";
            }

            if (password == null || password.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Password is required to delete your account");
                return "redirect:/profile/delete";
            }

            deleteUserUseCase.execute(userId, password);
            session.invalidate();

            LoggerUtility.logEvent("Account successfully deleted for user: " + userId);
            redirectAttributes.addFlashAttribute("success", "Your account has been permanently deleted.");
            return "redirect:/";

        } catch (InvalidCredentialsException e) {
            LoggerUtility.logWarning("Account deletion failed - incorrect password for user: " + userId);
            redirectAttributes.addFlashAttribute("error", "Incorrect password provided");
            return "redirect:/profile/delete";
        } catch (RuntimeException e) {
            LoggerUtility.logError("Account deletion failed for user " + userId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/delete";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again or contact support.");
            return "redirect:/profile/delete";
        }
    }

    @GetMapping("/profile/delete/cancel")
    public String cancelDeleteAccount() {
        return "redirect:/settings";
    }
}