package com.example.demo.data.repository;

import com.example.demo.domain.entities.Category;
import com.example.demo.data.util.LoggerUtility;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Repository
public class CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    // RowMapper to convert database rows to Category objects
    private final RowMapper<Category> categoryRowMapper = new RowMapper<Category>() {
        @Override
        public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
            Category category = new Category(
                    rs.getInt("CategoryID"),
                    rs.getString("Name"),
                    rs.getString("Description")
            );

            // Set parent ID if it exists
            try {
                int parentID = rs.getInt("ParentID");
                if (!rs.wasNull()) {
                    category.setParentID(parentID);
                }
            } catch (SQLException e) {
                // Ignore if the ParentID column doesn't exist
                LoggerUtility.logWarning("Error reading ParentID: " + e.getMessage());
            }

            return category;
        }
    };

    public CategoryRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        LoggerUtility.logEvent("CategoryRepository initialized");
    }

    // Create a new category
    public Category save(Category category) {
        String sql = "INSERT INTO categories (Name, Description, ParentID) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql,
                category.getName(),
                category.getDescription(),
                category.getParentID()
        );

        LoggerUtility.logEvent("Category created: " + category.getName());
        return category;
    }

    // Find category by ID
    public Optional<Category> findById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE CategoryID = ?";

        try {
            Category category = jdbcTemplate.queryForObject(sql, categoryRowMapper, categoryId);
            return Optional.of(category);
        } catch (EmptyResultDataAccessException e) {
            LoggerUtility.logWarning("Category not found: " + categoryId);
            return Optional.empty();
        }
    }

    // Find category by name
    public Optional<Category> findByName(String name) {
        String sql = "SELECT * FROM categories WHERE Name = ?";

        try {
            Category category = jdbcTemplate.queryForObject(sql, categoryRowMapper, name);
            return Optional.of(category);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Get all categories
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY Name";
        return jdbcTemplate.query(sql, categoryRowMapper);
    }

    // Get main categories (those without a parent)
    public List<Category> findMainCategories() {
        String sql = "SELECT * FROM categories WHERE ParentID IS NULL ORDER BY Name";
        return jdbcTemplate.query(sql, categoryRowMapper);
    }

    // Get subcategories for a parent category
    public List<Category> findSubcategories(int parentId) {
        String sql = "SELECT * FROM categories WHERE ParentID = ? ORDER BY Name";
        return jdbcTemplate.query(sql, categoryRowMapper, parentId);
    }

    // Get all descendant category IDs (including the category itself)
    public List<Integer> findAllDescendantCategoryIds(int categoryId) {
        List<Integer> descendantIds = new ArrayList<>();
        descendantIds.add(categoryId);

        String sql = "SELECT CategoryID FROM categories WHERE ParentID = ?";
        List<Integer> childIds = jdbcTemplate.queryForList(sql, Integer.class, categoryId);

        for (Integer childId : childIds) {
            descendantIds.addAll(findAllDescendantCategoryIds(childId));
        }

        return descendantIds;
    }

    // Update a category
    public Category update(Category category) {
        String sql = "UPDATE categories SET Name = ?, Description = ?, ParentID = ? WHERE CategoryID = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                category.getName(),
                category.getDescription(),
                category.getParentID(),
                category.getCategoryID()
        );

        if (rowsAffected == 0) {
            LoggerUtility.logError("Update failed - category not found: " + category.getCategoryID());
            throw new RuntimeException("Category not found: " + category.getCategoryID());
        }

        LoggerUtility.logEvent("Category updated: " + category.getName());
        return category;
    }

    // Delete a category
    public void delete(int categoryId) {
        // Check if there are listings linked to the category
        String checkSql = "SELECT COUNT(*) FROM listings WHERE CategoryID = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, categoryId);

        if (count != null && count > 0) {
            LoggerUtility.logError("Cannot delete category with linked listings: " + categoryId);
            throw new RuntimeException("Cannot delete category with linked listings");
        }

        // Check if there are subcategories
        String checkSubSql = "SELECT COUNT(*) FROM categories WHERE ParentID = ?";
        Integer subCount = jdbcTemplate.queryForObject(checkSubSql, Integer.class, categoryId);

        if (subCount != null && subCount > 0) {
            LoggerUtility.logError("Cannot delete category with subcategories: " + categoryId);
            throw new RuntimeException("Cannot delete category with subcategories");
        }

        String sql = "DELETE FROM categories WHERE CategoryID = ?";
        int rowsAffected = jdbcTemplate.update(sql, categoryId);

        if (rowsAffected == 0) {
            LoggerUtility.logError("Delete failed - category not found: " + categoryId);
            throw new RuntimeException("Category not found: " + categoryId);
        }

        LoggerUtility.logEvent("Category deleted: " + categoryId);
    }

    // Count listings in a category
    public int countListingsInCategory(int categoryId) {
        String sql = "SELECT COUNT(*) FROM listings WHERE CategoryID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, categoryId);
        return count != null ? count : 0;
    }

    // Count listings in a category and all its subcategories
    public int countListingsInCategoryHierarchy(int categoryId) {
        List<Integer> categoryIds = findAllDescendantCategoryIds(categoryId);

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM listings WHERE CategoryID IN (");
        for (int i = 0; i < categoryIds.size(); i++) {
            sql.append(i > 0 ? ", ?" : "?");
        }
        sql.append(")");

        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, categoryIds.toArray());
    }

    // Check if a category exists
    public boolean categoryExists(int categoryId) {
        String sql = "SELECT COUNT(*) FROM categories WHERE CategoryID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, categoryId);
        return count != null && count > 0;
    }
}