package com.gilbert.demo.domain.usecases.category;

import com.gilbert.demo.data.repository.CategoryRepository;
import com.gilbert.demo.data.util.LoggerUtility;
import org.springframework.stereotype.Service;

@Service
public class GetCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public GetCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        LoggerUtility.logEvent("GetCategoryUseCase initialized");
    }
}