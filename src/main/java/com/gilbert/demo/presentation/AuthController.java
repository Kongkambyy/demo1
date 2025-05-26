package com.gilbert.demo.presentation;

import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.gilbert.demo.domain.usecases.user.UserLoginUseCase;
import com.gilbert.demo.domain.usecases.user.CreateUserUseCase;
import com.gilbert.demo.data.util.LoggerUtility;
import com.gilbert.demo.exceptions.user.InvalidCredentialsException;
import com.gilbert.demo.exceptions.user.DuplicateUserException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserLoginUseCase userLoginUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;

    @Autowired
    public AuthController(UserLoginUseCase userLoginUseCase, CreateUserUseCase createUserUseCase, GetNotificationsUseCase getNotificationsUseCase) {
        this.userLoginUseCase = userLoginUseCase;
        this.createUserUseCase = createUserUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            User user = userLoginUseCase.execute(email, password);
            session.setAttribute("userId", user.getUserID());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userName", user.getName());
            LoggerUtility.logEvent("User logged in: " + user.getEmail());
            return "redirect:/profile";
        } catch (InvalidCredentialsException e) {
            LoggerUtility.logWarning("Login failed for email: " + email);
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/login";
        } catch (Exception e) {
            LoggerUtility.logError("Login error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred. Please try again later.");
            return "redirect:/login";
        }
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

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String alias,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false) String number,
                           @RequestParam(required = false) String address,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = createUserUseCase.Execute(name, alias, password, email, number, address);
            LoggerUtility.logEvent("New user registered: " + email);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (DuplicateUserException e) {
            LoggerUtility.logWarning("Registration failed - duplicate email: " + email);
            redirectAttributes.addFlashAttribute("error", "Email already in use");
            return "redirect:/register";
        } catch (Exception e) {
            LoggerUtility.logError("Registration error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred. Please try again later.");
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail != null) {
            LoggerUtility.logEvent("User logged out: " + userEmail);
        }
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        addNotificationCount(model, session);
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        addNotificationCount(model, session);
        return "register";
    }
}
