package com.gilbert.demo.presentation;

import com.gilbert.demo.data.repository.TransactionRepository;
import com.gilbert.demo.domain.entities.Transaction;
import com.gilbert.demo.domain.entities.User;
import com.gilbert.demo.domain.entities.Listing;
import com.gilbert.demo.domain.entities.Offer;
import com.gilbert.demo.domain.usecases.listing.GetListingUseCase;
import com.gilbert.demo.domain.usecases.user.GetUserUseCase;
import com.gilbert.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.gilbert.demo.data.repository.OfferRepository;
import com.gilbert.demo.data.repository.ListingRepository;
import com.gilbert.demo.data.util.LoggerUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            addNotificationCount(model, session);

            User user = getUserUseCase.findByIdOrThrow(userId);
            model.addAttribute("user", user);

            List<Listing> activeListings = getListingUseCase.getByUserId(userId)
                    .stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());
            model.addAttribute("listings", activeListings);

            List<Listing> allListings = getListingUseCase.getByUserId(userId);
            long soldItemsCount = allListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            return "profile";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @GetMapping("/profile/{userId}")
    public String userProfile(@PathVariable String userId, Model model, HttpSession session) {
        try {
            addNotificationCount(model, session);

            User profileUser = getUserUseCase.findByIdOrThrow(userId);

            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            model.addAttribute("user", profileUser);
            model.addAttribute("section", "items");
            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("listings", activeListings);
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

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
            addNotificationCount(model, session);

            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

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
            addNotificationCount(model, session);

            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            List<Listing> soldListings = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

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
            addNotificationCount(model, session);

            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            List<Offer> myOffers = offerRepository.findByBuyerId(userId);
            List<Offer> receivedOffers = offerRepository.findBySellerId(userId);

            List<Transaction> myPurchases = transactionRepository.findByBuyerId(userId);
            List<Transaction> mySales = transactionRepository.findBySellerId(userId);

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

            for (String listingId : listingIds) {
                Optional<Listing> listingOpt = listingRepository.findById(listingId);
                if (listingOpt.isPresent()) {
                    listingMap.put(listingId, listingOpt.get());
                }
            }

            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            model.addAttribute("user", user);
            model.addAttribute("section", "orders");
            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());

            model.addAttribute("myOffers", myOffersWithListings);
            model.addAttribute("receivedOffers", receivedOffersWithListings);

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
            addNotificationCount(model, session);

            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

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
            addNotificationCount(model, session);

            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

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
            addNotificationCount(model, session);

            User user = getUserUseCase.findByIdOrThrow(userId);
            List<Listing> userListings = getListingUseCase.getByUserId(userId);

            List<Listing> activeListings = userListings.stream()
                    .filter(listing -> "ACTIVE".equals(listing.getStatus()))
                    .collect(Collectors.toList());

            long soldItemsCount = userListings.stream()
                    .filter(listing -> "SOLD".equals(listing.getStatus()))
                    .count();

            model.addAttribute("user", user);
            model.addAttribute("section", "settings");
            model.addAttribute("soldItemsCount", soldItemsCount);
            model.addAttribute("activeListingsCount", activeListings.size());
            model.addAttribute("followersCount", 0);
            model.addAttribute("followingCount", 0);

            return "profile";
        } catch (Exception e) {
            LoggerUtility.logError("Error loading settings for user " + userId + ": " + e.getMessage());
            return "redirect:/";
        }
    }

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