package com.anshdesai.finpilot.controller;

import com.anshdesai.finpilot.api.RuleRequest;
import com.anshdesai.finpilot.api.RuleResponse;
import com.anshdesai.finpilot.model.Rule;
import com.anshdesai.finpilot.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    // --------- CRUD ---------

    @GetMapping
    public List<RuleResponse> list() {
        return ruleService.list().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RuleResponse create(@Valid @RequestBody RuleRequest req) {
        Rule saved = ruleService.create(req);
        return toResponse(saved);
    }

    @PutMapping("/{id}")
    public RuleResponse update(@PathVariable UUID id, @Valid @RequestBody RuleRequest req) {
        Rule saved = ruleService.update(id, req);
        return toResponse(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        ruleService.delete(id);
    }

    // --------- Test endpoint ---------

    public record TestRequest(String merchant) {}
    public record TestResponse(boolean matched, UUID ruleId, UUID categoryId, String categoryName) {}

    @PostMapping("/test")
    public TestResponse test(@RequestBody TestRequest body) {
        String merchant = normalize(body.merchant());
        for (Rule r : ruleService.listEnabled()) {
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

    private RuleResponse toResponse(Rule r) {
        RuleResponse resp = new RuleResponse();
        resp.setId(r.getId().toString());
        resp.setPattern(r.getPattern());
        resp.setMatchType(r.getMatchType());
        resp.setCategoryId(r.getCategory().getId().toString());
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