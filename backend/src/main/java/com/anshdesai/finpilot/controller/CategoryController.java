package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.api.CategoryRequest;
import com.anshdesai.finpilot.api.CategoryResponse;
import com.anshdesai.finpilot.model.Category;
import com.anshdesai.finpilot.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET all
    @GetMapping
    public List<CategoryResponse> list() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryController::toResponse)
                .toList();
    }

    // GET one
    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable UUID id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        return toResponse(cat);
    }

    // POST create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest req) {
        Category cat = new Category();
        cat.setName(req.getName());
        cat.setType(req.getType());
        cat.setColor(req.getColor());
        Category saved = categoryRepository.save(cat);
        return toResponse(saved);
    }

    // PUT update
    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest req) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        existing.setName(req.getName());
        existing.setType(req.getType());
        existing.setColor(req.getColor());
        Category saved = categoryRepository.save(existing);
        return toResponse(saved);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        try {
            categoryRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        } catch (DataIntegrityViolationException e) {
            // likely FK constraint (e.g., transactions referencing this category)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category is in use and cannot be deleted");
        }
    }

    // --- mapper ---
    private static CategoryResponse toResponse(Category c) {
        CategoryResponse resp = new CategoryResponse();
        resp.setId(c.getId());
        resp.setName(c.getName());
        resp.setType(c.getType());
        resp.setColor(c.getColor());
        return resp;
    }
}