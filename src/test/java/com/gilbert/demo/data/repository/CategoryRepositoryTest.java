package com.gilbert.demo.data.repository;

import com.gilbert.demo.domain.entities.Category;
import com.gilbert.demo.data.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testFindById() {
        Optional<Category> category = categoryRepository.findById(1);

        assertTrue(category.isPresent());
        assertEquals("MEN", category.get().getName());
        assertEquals("Men's fashion and accessories", category.get().getDescription());
    }

    @Test
    void testFindByName() {
        Optional<Category> category = categoryRepository.findByName("WOMEN");

        assertTrue(category.isPresent());
        assertEquals(2, category.get().getCategoryID());
        assertEquals("Women's fashion and accessories", category.get().getDescription());
    }

    @Test
    void testFindMainCategories() {
        List<Category> mainCategories = categoryRepository.findMainCategories();

        assertFalse(mainCategories.isEmpty());
        assertEquals(5, mainCategories.size());

        for (Category cat : mainCategories) {
            assertNull(cat.getParentID());
        }
    }

    @Test
    void testFindSubcategories() {
        List<Category> subcategories = categoryRepository.findSubcategories(1);

        assertFalse(subcategories.isEmpty());

        for (Category cat : subcategories) {
            assertEquals(1, cat.getParentID());
        }
    }

    @Test
    void testSaveCategory() {
        Category newCategory = new Category(0, "Test Category", "Test Description");
        newCategory.setParentID(1);

        Category saved = categoryRepository.save(newCategory);

        assertNotNull(saved);
        assertEquals("Test Category", saved.getName());
        assertEquals(1, saved.getParentID());
    }
}