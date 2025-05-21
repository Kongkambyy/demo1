package com.example.demo.presentation;

import com.example.demo.domain.entities.User;
import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.usecases.listing.GetListingUseCase;
import com.example.demo.domain.usecases.user.GetUserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    // These would be autowired in a full implementation
    private GetListingUseCase getListingUseCase;
    private GetUserUseCase getUserUseCase;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    // Search functionality now handled by SearchController

    @GetMapping("/shop")
    public String shop() {
        return "shop";
    }

    @GetMapping("/sell")
    public String sell() {
        return "sell";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        // In a full implementation, we would get the current user from the session
        // For now, we'll use a placeholder
        // User user = getUserUseCase.getCurrentUser();

        // Add data to the model
        model.addAttribute("section", "items");

        // These would be real data from repositories in a full implementation
        // model.addAttribute("user", user);
        // model.addAttribute("soldItemsCount", getListingUseCase.countSoldListingsByUser(user.getUserID()));
        // model.addAttribute("activeListingsCount", getListingUseCase.countActiveListingsByUser(user.getUserID()));
        // model.addAttribute("followersCount", socialService.getFollowersCount(user.getUserID()));
        // model.addAttribute("followingCount", socialService.getFollowingCount(user.getUserID()));

        return "profile";
    }

    @GetMapping("/profile/{userId}")
    public String userProfile(@PathVariable String userId, Model model) {
        // In a full implementation, we would fetch the user by ID
        // Optional<User> userOpt = getUserUseCase.findById(userId);
        // if (userOpt.isEmpty()) {
        //    return "redirect:/";
        // }
        // User profileUser = userOpt.get();

        // model.addAttribute("user", profileUser);
        model.addAttribute("section", "items");

        return "profile";
    }

    @GetMapping("/items")
    public String items(Model model) {
        model.addAttribute("section", "items");
        return "profile";
    }

    @GetMapping("/sales")
    public String sales(Model model) {
        model.addAttribute("section", "sales");
        return "profile";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("section", "orders");
        return "profile";
    }

    @GetMapping("/info")
    public String info(Model model) {
        model.addAttribute("section", "info");
        return "profile";
    }

    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("section", "help");
        return "profile";
    }

    @GetMapping("/followers")
    public String followers() {
        return "followers";
    }

    @GetMapping("/following")
    public String following() {
        return "following";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    @GetMapping("/favorites")
    public String favorites() {
        return "favorites";
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "notifications";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }
}