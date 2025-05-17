package com.example.demo.domain.usecases.category;

import com.example.demo.data.repository.CategoryRepository;
import com.example.demo.domain.entities.Category;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class GetCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public GetCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        LoggerUtility.logEvent("GetCategoryUseCase initialized");
    }

    public Category executeById(int categoryId) {
        LoggerUtility.logEvent("Looking up category with ID: " + categoryId);
        Optional<Category> category = categoryRepository.findById(categoryId);

        if (category.isEmpty()) {
            LoggerUtility.logWarning("Category not found with ID: " + categoryId);
            throw new RuntimeException("Category not found: " + categoryId);
        }

        return category.get();
    }

    public Category executeByName(String name) {
        LoggerUtility.logEvent("Looking up category with name: " + name);
        Optional<Category> category = categoryRepository.findByName(name);

        if (category.isEmpty()) {
            LoggerUtility.logWarning("Category not found with name: " + name);
            throw new RuntimeException("Category not found: " + name);
        }

        return category.get();
    }

    public boolean categoryExists(int categoryId) {
        return categoryRepository.findById(categoryId).isPresent();
    }

    public int getListingCount(int categoryId) {
        return categoryRepository.countListingsInCategory(categoryId);
    }
}