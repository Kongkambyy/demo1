package com.example.demo.presentation;

import com.example.demo.domain.entities.Listing;
import com.example.demo.domain.usecases.listing.SearchListingsUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    private final SearchListingsUseCase searchListingsUseCase;

    @Autowired
    public SearchController(SearchListingsUseCase searchListingsUseCase) {
        this.searchListingsUseCase = searchListingsUseCase;
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String designer,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String ship,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter,
            Model model
    ) {
        // Process price range if specified
        Integer minPrice = null;
        Integer maxPrice = null;
        if (price != null) {
            String[] priceRange = price.split("-");
            if (priceRange.length > 0) {
                try {
                    minPrice = Integer.parseInt(priceRange[0]);
                    if (priceRange.length > 1 && !priceRange[1].equals("+")) {
                        maxPrice = Integer.parseInt(priceRange[1]);
                    }
                } catch (NumberFormatException e) {
                    // Handle parsing error
                }
            }
        }

        // If designer is specified, use it as a brand filter
        String brand = designer;

        // Apply sorting based on the selected option
        String sortField = null;
        boolean sortAscending = true;
        if (sort != null) {
            switch (sort) {
                case "latest":
                    sortField = "createdDate";
                    sortAscending = false;
                    break;
                case "price_asc":
                    sortField = "price";
                    sortAscending = true;
                    break;
                case "price_desc":
                    sortField = "price";
                    sortAscending = false;
                    break;
            }
        }

        // Use SearchListingsUseCase to get listings from the repository
        List<Listing> listings = searchListingsUseCase.execute(
                keyword,     // Search terms
                minPrice,    // Minimum price
                maxPrice,    // Maximum price
                condition,   // Product condition
                brand        // Brand/designer
        );

        // Sort the results based on the selected sorting (in a real app, this would be database-side)
        if (sortField != null) {
            if (sortField.equals("price")) {
                if (sortAscending) {
                    listings.sort((a, b) -> Integer.compare(a.getPrice(), b.getPrice()));
                } else {
                    listings.sort((a, b) -> Integer.compare(b.getPrice(), a.getPrice()));
                }
            } else if (sortField.equals("createdDate")) {
                listings.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));
            }
        }

        // Add listings to the model
        model.addAttribute("listings", listings);

        // Set up breadcrumb based on filter parameters
        if (category != null) {
            model.addAttribute("breadcrumb", getCategoryBreadcrumb(category));
            model.addAttribute("category", formatCategoryName(category));
        } else if (designer != null) {
            model.addAttribute("breadcrumb", "Designers");
            model.addAttribute("category", formatDesignerName(designer));
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("breadcrumb", "Search");
            model.addAttribute("category", "Search Results for \"" + keyword + "\"");
        } else {
            model.addAttribute("breadcrumb", "Shop");
            model.addAttribute("category", "All Products");
        }

        // Count from the actual result set
        model.addAttribute("count", listings.size());

        // Track active filters for UI highlighting
        if (sort != null) model.addAttribute("activeSort", sort);
        if (category != null) model.addAttribute("activeCategory", category);
        if (designer != null) model.addAttribute("activeDesigner", designer);
        if (condition != null) model.addAttribute("activeCondition", condition);
        if (size != null) model.addAttribute("activeSize", size);
        if (color != null) model.addAttribute("activeColor", color);
        if (material != null) model.addAttribute("activeMaterial", material);
        if (ship != null) model.addAttribute("activeShip", ship);
        if (price != null) model.addAttribute("activePrice", price);
        if (filter != null) model.addAttribute("activeFilter", filter);

        return "search";
    }

    @GetMapping("/search/category")
    public String searchByCategory(@RequestParam String name, Model model) {
        return search(null, name, null, null, null, null, null, null, null, null, null, model);
    }

    @GetMapping("/search/designer")
    public String searchByDesigner(@RequestParam String name, Model model) {
        return search(null, null, name, null, null, null, null, null, null, null, null, model);
    }

    // Helper methods for formatting display names
    private String getCategoryBreadcrumb(String category) {
        switch (category.toLowerCase()) {
            case "shirts":
            case "jackets":
            case "shoes":
                return "Men's Fashion";
            case "dresses":
            case "bags":
            case "heels":
                return "Women's Fashion";
            case "skincare":
            case "makeup":
            case "fragrance":
                return "Beauty";
            case "furniture":
            case "decor":
            case "textiles":
                return "Home";
            default:
                return "All Categories";
        }
    }

    private String formatCategoryName(String category) {
        // Capitalize first letter
        if (category == null || category.isEmpty()) {
            return "All Products";
        }
        return category.substring(0, 1).toUpperCase() + category.substring(1);
    }

    private String formatDesignerName(String designer) {
        // Designer names often have specific formatting
        switch (designer.toLowerCase()) {
            case "gucci":
                return "GUCCI";
            case "prada":
                return "PRADA";
            case "balenciaga":
                return "BALENCIAGA";
            default:
                return designer.toUpperCase();
        }
    }
}