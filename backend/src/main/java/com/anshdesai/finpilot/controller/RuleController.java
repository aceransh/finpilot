package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.api.RuleRequest;
import com.anshdesai.finpilot.api.RuleResponse;
import com.anshdesai.finpilot.model.Category;
import com.anshdesai.finpilot.model.Rule;
import com.anshdesai.finpilot.repository.CategoryRepository;
import com.anshdesai.finpilot.repository.RuleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleRepository ruleRepo;
    private final CategoryRepository catRepo;

    public RuleController(RuleRepository ruleRepo, CategoryRepository catRepo) {
        this.ruleRepo = ruleRepo;
        this.catRepo = catRepo;
    }

    // --------- CRUD ---------

    @GetMapping
    public List<RuleResponse> list() {
        return ruleRepo.findAllByOrderByPriorityAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RuleResponse create(@Valid @RequestBody RuleRequest req) {
        Rule r = new Rule();
        apply(req, r);
        return toResponse(ruleRepo.save(r));
    }

    @PutMapping("/{id}")
    public RuleResponse update(@PathVariable UUID id, @Valid @RequestBody RuleRequest req) {
        Rule r = ruleRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found"));
        apply(req, r);
        return toResponse(ruleRepo.save(r));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!ruleRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found");
        }
        ruleRepo.deleteById(id);
    }

    // --------- Test endpoint ---------

    public record TestRequest(String merchant) {}
    public record TestResponse(boolean matched, UUID ruleId, UUID categoryId, String categoryName) {}

    @PostMapping("/test")
    public TestResponse test(@RequestBody TestRequest body) {
        String merchant = normalize(body.merchant());
        for (Rule r : ruleRepo.findAllByEnabledTrueOrderByPriorityAsc()) {
            if (matches(merchant, r)) {
                return new TestResponse(
                        true,
                        r.getId(),
                        r.getCategory().getId(),
                        r.getCategory().getName()
                );
            }
        }
        return new TestResponse(false, null, null, null);
    }

    // --------- helpers ---------

    private void apply(RuleRequest req, Rule r) {
        // resolve category
        UUID catId = req.getCategoryId();
        Category cat = catRepo.findById(catId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));

        r.setPattern(req.getPattern().trim());
        r.setMatchType(req.getMatchType().trim());  // "CONTAINS" | "REGEX"
        r.setCategory(cat);
        r.setPriority(req.getPriority() == null ? 100 : req.getPriority());
        r.setEnabled(req.getEnabled() == null || req.getEnabled()); // default true
    }

    private RuleResponse toResponse(Rule r) {
        RuleResponse resp = new RuleResponse();
        resp.setId(r.getId().toString());
        resp.setPattern(r.getPattern());
        resp.setMatchType(r.getMatchType());
        resp.setCategoryId(r.getCategory().getId().toString());   // <-- String
        resp.setCategoryName(r.getCategory().getName());
        resp.setPriority(r.getPriority());
        resp.setEnabled(r.isEnabled());
        return resp;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).trim()
                .replace(",", "")
                .replace(".", "");
    }

    private boolean matches(String merchantNorm, Rule r) {
        if ("CONTAINS".equalsIgnoreCase(r.getMatchType())) {
            return merchantNorm.contains(r.getPattern().toLowerCase(Locale.ROOT));
        }
        if ("REGEX".equalsIgnoreCase(r.getMatchType())) {
            return Pattern.compile(r.getPattern(), Pattern.CASE_INSENSITIVE).matcher(merchantNorm).find();
        }
        return false;
    }
}