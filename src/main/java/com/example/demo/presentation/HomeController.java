package com.example.demo.presentation;

import com.example.demo.data.repository.TransactionRepository;
import com.example.demo.data.repository.UserRepository;
import com.example.demo.domain.entities.Transaction;
import com.example.demo.domain.entities.User;
import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.entities.Offer;
import com.example.demo.domain.usecases.listing.GetListingUseCase;
import com.example.demo.domain.usecases.user.GetUserUseCase;
import com.example.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.example.demo.data.repository.OfferRepository;
import com.example.demo.data.repository.ListingRepository;
import com.example.demo.data.util.LoggerUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final GetListingUseCase getListingUseCase;
    private final GetUserUseCase getUserUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final OfferRepository offerRepository;
    private final ListingRepository listingRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public HomeController(GetListingUseCase getListingUseCase,
                          GetUserUseCase getUserUseCase,
                          GetNotificationsUseCase getNotificationsUseCase,
                          OfferRepository offerRepository,
                          ListingRepository listingRepository, TransactionRepository transactionRepository) {
        this.getListingUseCase = getListingUseCase;
        this.getUserUseCase = getUserUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.offerRepository = offerRepository;
        this.listingRepository = listingRepository;
        this.transactionRepository = transactionRepository;
    }

    // Helper method to add notification count to model
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

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        addNotificationCount(model, session);
        return "index";
    }

    @GetMapping("/index")
    public String index(Model model, HttpSession session) {
        addNotificationCount(model, session);
        return "index";
    }

    @GetMapping("/shop")
    public String shop() {
        return "redirect:/search";
    }

    @GetMapping("/sell")
    public String sell(Model model, HttpSession session) {
        addNotificationCount(model, session);
        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        // Get current user ID from session
        String userId = (String) session.getAttribute("userId");

        // If user is not logged in, redirect to login page
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Add notification count
            addNotificationCount(model, session);

            // Fetch user from database
            User user = getUserUseCase.findByIdOrThrow(userId);
            model.addAttribute("user", user);

            // Get active listings (simplified)
            List<Listing> activeListings = getListingUseCase.getByUserId(userId)
                    .stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());
            model.addAttribute("listings", activeListings);

            // Calculate stats
            List<Listing> allListings = getListingUseCase.getByUserId(userId);
            long soldItemsCount = allListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            // Log successful page rendering
            LoggerUtility.logEvent("Profile page accessed by user: " + userId);

            return "profile"; // This should match your template name
        } catch (Exception e) {
            LoggerUtility.logError("Error loading profile for user " + userId + ": " + e.getMessage());
            e.printStackTrace(); // Print stacktrace for debugging
            return "redirect:/";
        }
    }

    @GetMapping("/profile/{userId}")
    public String userProfile(@PathVariable String userId, Model model, HttpSession session) {
        try {
            // Add notification count
            addNotificationCount(model, session);

            // Get the user by ID
            User profileUser = getUserUseCase.findByIdOrThrow(userId);

            // Get all listings for this user
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            // Filter active listings (only these should be publicly visible)
            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            // Count sold listings for stats
            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            // Add data to model
            model.addAttribute("user", profileUser);
            model.addAttribute("section", "items");
            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("listings", activeListings);
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            // Log the visit (could be used for analytics)
            String visitorId = (String) session.getAttribute("userId");
            if (visitorId != null) {
                LoggerUtility.logEvent("User " + visitorId + " viewed profile of user " + userId);
            }

            return "profile";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading profile for user " + userId + ": " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/items")
    public String items(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Add notification count
            addNotificationCount(model, session);

            // Get user and their listings
            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            // Filter listings by status
            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            // Populate model with data
            model.addAttribute("user", user);
            model.addAttribute("section", "items");
            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("listings", activeListings);
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            return "profile";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading items for user " + userId + ": " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/sales")
    public String sales(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Add notification count
            addNotificationCount(model, session);

            // Get user and their listings
            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            // Filter listings by status
            List<Listing> soldListings = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            // Populate model with data
            model.addAttribute("user", user);
            model.addAttribute("section", "sales");
            model.addAttribute("soldItemsCount", soldListings.size());
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("listings", soldListings);
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            return "profile";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading sales for user " + userId + ": " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/orders")
    public String orders(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Add notification count
            addNotificationCount(model, session);

            // Get user data
            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            // EXISTING: Get offers
            List<Offer> myOffers = offerRepository.findByBuyerId(userId);
            List<Offer> receivedOffers = offerRepository.findBySellerId(userId);

            // NEW: Get transactions
            List<Transaction> myPurchases = transactionRepository.findByBuyerId(userId);
            List<Transaction> mySales = transactionRepository.findBySellerId(userId);

            // EXISTING: Offers with listings (keep your existing OfferWithListing approach)
            List<OfferWithListing> myOffersWithListings = new ArrayList<>();
            for (Offer offer : myOffers) {
                Optional<Listing> listingOpt = listingRepository.findById(offer.getListingID());
                if (listingOpt.isPresent()) {
                    myOffersWithListings.add(new OfferWithListing(offer, listingOpt.get()));
                }
            }

            List<OfferWithListing> receivedOffersWithListings = new ArrayList<>();
            for (Offer offer : receivedOffers) {
                Optional<Listing> listingOpt = listingRepository.findById(offer.getListingID());
                if (listingOpt.isPresent()) {
                    receivedOffersWithListings.add(new OfferWithListing(offer, listingOpt.get()));
                }
            }

            Map<String, Listing> listingMap = new HashMap<>();

            Set<String> listingIds = new HashSet<>();
            myPurchases.forEach(t -> listingIds.add(t.getListingID()));
            mySales.forEach(t -> listingIds.add(t.getListingID()));

            // Fetch all needed listings once
            for (String listingId : listingIds) {
                Optional<Listing> listingOpt = listingRepository.findById(listingId);
                if (listingOpt.isPresent()) {
                    listingMap.put(listingId, listingOpt.get());
                }
            }

            // Filter listings for stats
            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            // Add everything to model
            model.addAttribute("user", user);
            model.addAttribute("section", "orders");
            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());

            // EXISTING
            model.addAttribute("myOffers", myOffersWithListings);
            model.addAttribute("receivedOffers", receivedOffersWithListings);

            // NEW: Transactions and listings separately
            model.addAttribute("myPurchases", myPurchases);
            model.addAttribute("mySales", mySales);
            model.addAttribute("transactionListings", listingMap);

            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            return "profile";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading orders for user " + userId + ": " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/info")
    public String info(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Add notification count
            addNotificationCount(model, session);

            // Get user info
            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            // Filter listings for stats
            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            model.addAttribute("user", user);
            model.addAttribute("section", "info");
            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            return "profile";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading info for user " + userId + ": " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/help")
    public String help(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Add notification count
            addNotificationCount(model, session);

            // Get user data
            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            // Filter listings for stats
            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            model.addAttribute("user", user);
            model.addAttribute("section", "help");
            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            return "profile";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading help for user " + userId + ": " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/followers")
    public String followers(Model model, HttpSession session) {
        addNotificationCount(model, session);
        return "followers";
    }

    @GetMapping("/following")
    public String following(Model model, HttpSession session) {
        addNotificationCount(model, session);
        return "following";
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        addNotificationCount(model, session);
        return "cart";
    }

    @GetMapping("/settings")
    public String settings(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Add notification count
            addNotificationCount(model, session);

            // Get user data
            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            // Filter listings for stats
            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            model.addAttribute("user", user);
            model.addAttribute("section", "settings");  // This tells profile.html to show settings section
            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            return "profile";  // Return profile template, not settings template
        } catch (Exception e) {
            LoggerUtility.logError("Error loading settings for user " + userId + ": " + e.getMessage());
            return "redirect:/";
        }
    }

    // Helper class to combine offer and listing data
    public static class OfferWithListing {
        private final Offer offer;
        private final Listing listing;

        public OfferWithListing(Offer offer, Listing listing) {
            this.offer = offer;
            this.listing = listing;
        }

        public Offer getOffer() {
            return offer;
        }

        public Listing getListing() {
            return listing;
        }
    }
}