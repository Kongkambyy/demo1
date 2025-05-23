package com.example.demo.presentation;

import com.example.demo.domain.entities.Category;
import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.usecases.listing.SearchListingsUseCase;
import com.example.demo.domain.usecases.Notifications.GetNotificationsUseCase;
import com.example.demo.data.repository.CategoryRepository;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

@Controller
public class SearchController {

    private final SearchListingsUseCase searchListingsUseCase;
    private final CategoryRepository categoryRepository;
    private final GetNotificationsUseCase getNotificationsUseCase;

    @Autowired
    public SearchController(SearchListingsUseCase searchListingsUseCase,
                            CategoryRepository categoryRepository,
                            GetNotificationsUseCase getNotificationsUseCase) {
        this.searchListingsUseCase = searchListingsUseCase;
        this.categoryRepository = categoryRepository;
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

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String designer,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String ship,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter,
            Model model,
            HttpSession session
    ) {
        addNotificationCount(model, session);

        // Process price range if specified
        Integer minPrice = null;
        Integer maxPrice = null;
        if (price != null && !price.isEmpty()) {
            try {
                if (price.contains("-")) {
                    String[] priceRange = price.split("-");
                    if (priceRange.length >= 1) {
                        minPrice = Integer.parseInt(priceRange[0]);
                    }
                    if (priceRange.length >= 2 && !priceRange[1].equals("+") && !priceRange[1].isEmpty()) {
                        maxPrice = Integer.parseInt(priceRange[1]);
                    }
                } else if (price.endsWith("+")) {
                    minPrice = Integer.parseInt(price.replace("+", ""));
                }
            } catch (NumberFormatException e) {
                LoggerUtility.logWarning("Invalid price format: " + price);
            }
        }

        // Use designer as brand filter
        String brand = designer;

        // Fetch listings with filters
        List<Listing> listings;
        if (categoryId != null) {
            // Get all subcategory IDs for hierarchical filtering
            List<Integer> categoryIds = categoryRepository.findAllDescendantCategoryIds(categoryId);
            listings = searchListingsUseCase.searchByCategoryHierarchy(keyword, minPrice, maxPrice, condition, brand, categoryIds);
        } else {
            listings = searchListingsUseCase.execute(keyword, minPrice, maxPrice, condition, brand, null);
        }

        // Apply sorting
        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    listings.sort((a, b) -> Integer.compare(a.getPrice(), b.getPrice()));
                    break;
                case "price_desc":
                    listings.sort((a, b) -> Integer.compare(b.getPrice(), a.getPrice()));
                    break;
                case "newest":
                default:
                    listings.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));
                    break;
            }
        }

        // Get category information for UI
        List<Category> mainCategories = categoryRepository.findMainCategories();
        model.addAttribute("mainCategories", mainCategories);

        // Handle category-specific data
        Category selectedCategory = null;
        Category parentCategory = null;
        List<Category> subcategories = null;

        if (categoryId != null) {
            Optional<Category> selectedCategoryOpt = categoryRepository.findById(categoryId);
            if (selectedCategoryOpt.isPresent()) {
                selectedCategory = selectedCategoryOpt.get();
                model.addAttribute("selectedCategory", selectedCategory);

                // Check if this is a subcategory or main category
                if (selectedCategory.getParentID() != null) {
                    // This is a subcategory - get parent and sibling subcategories
                    Optional<Category> parentOpt = categoryRepository.findById(selectedCategory.getParentID());
                    if (parentOpt.isPresent()) {
                        parentCategory = parentOpt.get();
                        model.addAttribute("parentCategory", parentCategory);
                        subcategories = categoryRepository.findSubcategories(selectedCategory.getParentID());
                    }
                } else {
                    // This is a main category - get its subcategories
                    subcategories = categoryRepository.findSubcategories(categoryId);
                }
            }
        }

        if (subcategories != null) {
            model.addAttribute("subcategories", subcategories);
        }

        // Set up breadcrumb and category display
        String breadcrumb = "Shop";
        String categoryDisplay = "All Products";

        if (selectedCategory != null) {
            if (selectedCategory.getParentID() != null && parentCategory != null) {
                // This is a subcategory
                breadcrumb = parentCategory.getName();
                categoryDisplay = selectedCategory.getName();
            } else {
                // This is a main category
                breadcrumb = selectedCategory.getName();
                categoryDisplay = "All " + selectedCategory.getName();
            }
        } else if (designer != null) {
            breadcrumb = "Designers";
            categoryDisplay = formatDesignerName(designer);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            breadcrumb = "Search";
            categoryDisplay = "Search Results for \"" + keyword + "\"";
        }

        model.addAttribute("breadcrumb", breadcrumb);
        model.addAttribute("category", categoryDisplay);

        // Add listings and count
        model.addAttribute("listings", listings);
        model.addAttribute("count", listings.size());

        // Track active filters for UI highlighting
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeSort", sort);
        model.addAttribute("activeCategoryId", categoryId);
        model.addAttribute("activeDesigner", designer);
        model.addAttribute("activeCondition", condition);
        model.addAttribute("activeSize", size);
        model.addAttribute("activeColor", color);
        model.addAttribute("activeMaterial", material);
        model.addAttribute("activeShip", ship);
        model.addAttribute("activePrice", price);
        model.addAttribute("activeFilter", filter);

        String userId = (String) session.getAttribute("userId");
        LoggerUtility.logEvent("Search performed by user: " + (userId != null ? userId : "anonymous") +
                " - keyword: " + keyword + ", categoryId: " + categoryId + ", designer: " + designer);

        return "search";
    }

    @GetMapping("/category/{id}")
    public String categoryView(@PathVariable("id") Integer categoryId, Model model, HttpSession session) {
        return search(null, categoryId, null, null, null, null, null, null, null, null, null, model, session);
    }

    private String formatDesignerName(String designer) {
        if (designer == null) return "";
        switch (designer.toLowerCase()) {
            case "gucci": return "GUCCI";
            case "prada": return "PRADA";
            case "balenciaga": return "BALENCIAGA";
            case "luxury": return "LUXURY BRANDS";
            case "streetwear": return "STREETWEAR";
            case "minimalist": return "MINIMALIST";
            default: return designer.toUpperCase();
        }
    }
}