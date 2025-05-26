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
}