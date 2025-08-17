package com.anshdesai.finpilot.service;

import com.anshdesai.finpilot.api.RuleRequest;
import com.anshdesai.finpilot.model.Category;
import com.anshdesai.finpilot.model.Rule;
import com.anshdesai.finpilot.repository.CategoryRepository;
import com.anshdesai.finpilot.repository.RuleRepository;
import com.anshdesai.finpilot.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RuleService {

    private final RuleRepository ruleRepo;
    private final CategoryRepository catRepo;
    private final CurrentUser currentUser;

    public RuleService(RuleRepository ruleRepo,
                       CategoryRepository catRepo,
                       CurrentUser currentUser) {
        this.ruleRepo = ruleRepo;
        this.catRepo = catRepo;
        this.currentUser = currentUser;
    }

    /** List all rules for the caller, ordered by priority (ASC). */
    public List<Rule> list() {
        return ruleRepo.findAllByUserIdOrderByPriorityAsc(currentUser.userId());
    }

    /** List only enabled rules for the caller, ordered by priority (ASC). */
    public List<Rule> listEnabled() {
        return ruleRepo.findAllByUserIdAndEnabledTrueOrderByPriorityAsc(currentUser.userId());
    }

    /** Get a single rule owned by the caller. */
    public Rule get(UUID id) {
        return ruleRepo.findByIdAndUserId(id, currentUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found"));
    }

    /** Create a new rule for the caller (with category ownership validation). */
    public Rule create(RuleRequest req) {
        Category cat = requireOwnedCategory(req.getCategoryId());

        Rule r = new Rule();
        r.setUserId(currentUser.userId());
        r.setPattern(req.getPattern());
        r.setMatchType(req.getMatchType());
        r.setPriority(req.getPriority());
        r.setEnabled(Boolean.TRUE.equals(req.getEnabled()));
        r.setCategory(cat);

        return ruleRepo.save(r);
    }

    /** Update an existing rule (must belong to caller). */
    public Rule update(UUID id, RuleRequest req) {
        Rule existing = get(id); // ownership enforced

        // If category changed, re-validate ownership
        if (req.getCategoryId() != null &&
                (existing.getCategory() == null ||
                        !req.getCategoryId().equals(existing.getCategory().getId()))) {
            existing.setCategory(requireOwnedCategory(req.getCategoryId()));
        }

        existing.setPattern(req.getPattern());
        existing.setMatchType(req.getMatchType());
        existing.setPriority(req.getPriority());
        existing.setEnabled(Boolean.TRUE.equals(req.getEnabled()));

        return ruleRepo.save(existing);
    }

    /** Delete an existing rule (must belong to caller). */
    public void delete(UUID id) {
        Rule existing = get(id); // ownership enforced
        ruleRepo.delete(existing);
    }

    /** Convenience: toggle enabled flag for a rule the caller owns. */
    public Rule setEnabled(UUID id, boolean enabled) {
        Rule existing = get(id);
        existing.setEnabled(enabled);
        return ruleRepo.save(existing);
    }

    // ---- helpers -----------------------------------------------------------

    private Category requireOwnedCategory(UUID categoryId) {
        if (categoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId is required");
        }
        return catRepo.findByIdAndUserId(categoryId, currentUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
    }
}