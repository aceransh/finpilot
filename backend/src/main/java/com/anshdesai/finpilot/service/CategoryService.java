package com.anshdesai.finpilot.service;

import com.anshdesai.finpilot.api.CategoryRequest;
import com.anshdesai.finpilot.model.Category;
import com.anshdesai.finpilot.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /* ---------- Defaults (immutable data we want to seed per user) ---------- */

    private record SeedCat(String name, String type, String color) {}

    // Keep these values valid for your DB constraints (type must be EXPENSE/INCOME, name not null, etc.)
    private static final List<SeedCat> DEFAULTS = List.of(
            new SeedCat("Entertainment",          "EXPENSE", "#FB8C00"),
            new SeedCat("Food & drink",           "EXPENSE", "#E53935"),
            new SeedCat("General merchandise",    "EXPENSE", "#3949AB"),
            new SeedCat("General services",       "EXPENSE", "#6D4C41"),
            new SeedCat("Government & non-profit","EXPENSE", "#5E35B1"),
            new SeedCat("Home improvement",       "EXPENSE", "#1E88E5"),
            new SeedCat("Income",                 "INCOME",  "#00897B"),
            new SeedCat("Loan payments",          "EXPENSE", "#546E7A"),
            new SeedCat("Medical",                "EXPENSE", "#00838F"),
            new SeedCat("Personal care",          "EXPENSE", "#D81B60"),
            new SeedCat("Rent & utilities",       "EXPENSE", "#43A047"),
            new SeedCat("Transfer in",            "INCOME",  "#26A69A"),
            new SeedCat("Transfer out",           "EXPENSE", "#26C6DA"),
            new SeedCat("Transportation",         "EXPENSE", "#7CB342"),
            new SeedCat("Travel",                 "EXPENSE", "#F4511E"),
            new SeedCat("Bank fees",              "EXPENSE", "#8E24AA")
    );

    /**
     * Best-effort default seed. If anything goes wrong, we log and return,
     * so GET /categories never fails because of seeding.
     */
    private void seedDefaultsIfEmpty(String userId) {
        // 1) Quick existence check: if the user already has categories, do nothing.
        List<Category> existing = categoryRepository.findAllByUserIdOrderByName(userId);
        if (!existing.isEmpty()) {
            return;
        }

        // 2) Build entities to insert.
        List<Category> toInsert = new ArrayList<>(DEFAULTS.size());
        for (SeedCat d : DEFAULTS) {
            Category c = new Category();
            c.setUserId(userId);
            c.setName(d.name());
            c.setType(d.type());
            c.setColor(d.color());
            toInsert.add(c);
        }

        // 3) Try saving. If a constraint or race causes an exception, we log and move on.
        try {
            categoryRepository.saveAll(toInsert);
            log.info("Seeded {} default categories for user {}", toInsert.size(), userId);
        } catch (DataIntegrityViolationException dive) {
            // Most likely: parallel request already seeded or a unique/constraint conflict.
            log.warn("Default category seed skipped for user {} (constraint/race): {}", userId, dive.getMessage());
        } catch (Exception e) {
            // Catch-all: never fail the read path because of seeding.
            log.warn("Default category seed failed for user {}: {}", userId, e.toString());
        }
    }

    /* ------------------------------ CRUD ------------------------------ */

    /** Read all categories for a user (ordered by name), seeding defaults if the user has none. */
    public List<Category> list(String userId) {
        seedDefaultsIfEmpty(userId);  // guarded; won’t throw
        return categoryRepository.findAllByUserIdOrderByName(userId);
    }

    /** Read a single category by id, scoped to the user. */
    public Category get(UUID id, String userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    /** Create a category for the user. */
    public Category create(CategoryRequest req, String userId) {
        Category c = new Category();
        c.setUserId(userId);
        c.setName(req.getName());
        c.setType(req.getType());
        c.setColor(req.getColor());
        return categoryRepository.save(c);
    }

    /** Update a category, only if it belongs to the user. */
    public Category update(UUID id, CategoryRequest req, String userId) {
        Category existing = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        existing.setName(req.getName());
        existing.setType(req.getType());
        existing.setColor(req.getColor());
        return categoryRepository.save(existing);
    }

    /** Delete a category, only if it belongs to the user. */
    public void delete(UUID id, String userId) {
        Category existing = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        try {
            categoryRepository.delete(existing);
        } catch (DataIntegrityViolationException e) {
            // e.g., FK from transactions -> categories
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category is in use and cannot be deleted");
        }
    }
}