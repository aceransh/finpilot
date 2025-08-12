package com.anshdesai.finpilot.service;

import com.anshdesai.finpilot.model.Category;
import com.anshdesai.finpilot.model.Rule;
import com.anshdesai.finpilot.repository.RuleRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class RuleEngineService {

    private final RuleRepository ruleRepo;

    public RuleEngineService(RuleRepository ruleRepo) {
        this.ruleRepo = ruleRepo;
    }

    /** Try to find a Category for the given merchant using enabled rules (priority ASC). */
    public Optional<Category> apply(String merchant) {
        String norm = normalize(merchant);
        for (Rule r : ruleRepo.findAllByEnabledTrueOrderByPriorityAsc()) {
            if (matches(norm, r)) {
                return Optional.of(r.getCategory());
            }
        }
        return Optional.empty();
    }

    /** Normalize merchant string for reliable matching. */
    static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT)
                .trim()
                .replace(",", "")
                .replace(".", "");
    }

    /** Check a single rule against an already-normalized merchant. */
    static boolean matches(String merchantNorm, Rule r) {
        String type = r.getMatchType();
        String pattern = r.getPattern();
        if (pattern == null || pattern.isBlank() || type == null) return false;

        return switch (type.toUpperCase(Locale.ROOT)) {
            case "CONTAINS" -> merchantNorm.contains(pattern.toLowerCase(Locale.ROOT));
            case "REGEX" -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                    .matcher(merchantNorm)
                    .find();
            default -> false;
        };
    }
}