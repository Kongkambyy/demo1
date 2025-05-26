package com.example.demo.domain.usecases.category;

import com.example.demo.data.repository.CategoryRepository;
import com.example.demo.domain.entities.Category;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GetCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public GetCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        LoggerUtility.logEvent("GetCategoriesUseCase initialized");
    }

    public List<Category> execute() {
        LoggerUtility.logEvent("Retrieving all categories");
        return categoryRepository.findAll();
    }
}