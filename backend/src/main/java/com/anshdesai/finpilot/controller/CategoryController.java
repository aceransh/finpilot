package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.api.CategoryRequest;
import com.anshdesai.finpilot.api.CategoryResponse;
import com.anshdesai.finpilot.model.Category;
import com.anshdesai.finpilot.security.CurrentUser;
import com.anshdesai.finpilot.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUser currentUser;

    public CategoryController(CategoryService categoryService, CurrentUser currentUser) {
        this.categoryService = categoryService;
        this.currentUser = currentUser;
    }
    // GET all — ownership enforced in service
    @GetMapping
    public List<CategoryResponse> list() {
        String uid = currentUser.userId();
        return categoryService.list(uid).stream()
                .map(CategoryController::toResponse)
                .toList();
    }

    // GET one — ownership enforced in service
    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable UUID id) {
        Category cat = categoryService.get(id, currentUser.userId());
        return toResponse(cat);
    }

    // POST create — stamps user_id in service
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest req) {
        Category saved = categoryService.create(req, currentUser.userId());
        return toResponse(saved);
    }

    // PUT update — ownership enforced in service
    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest req) {
        Category saved = categoryService.update(id, req, currentUser.userId());
        return toResponse(saved);
    }

    // DELETE — ownership + FK conflict handled in service
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        categoryService.delete(id, currentUser.userId());
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