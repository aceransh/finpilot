package com.anshdesai.finpilot.service;

import com.anshdesai.finpilot.model.Category;
import com.anshdesai.finpilot.model.Rule;
import com.anshdesai.finpilot.model.Transaction;
import com.anshdesai.finpilot.repository.RuleRepository;
import com.anshdesai.finpilot.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class RuleEngineService {

    private final RuleRepository ruleRepo;
    private final CurrentUser currentUser;   // ⬅️ new

    public RuleEngineService(RuleRepository ruleRepo, CurrentUser currentUser) {
        this.ruleRepo = ruleRepo;
        this.currentUser = currentUser;
    }

    /** Try to find a Category for the given merchant using enabled rules (priority ASC). */
    public Optional<Category> apply(String merchant) {
        String norm = normalize(merchant);
        String uid  = currentUser.userId();
        for (Rule r : ruleRepo.findAllByUserIdAndEnabledTrueOrderByPriorityAsc(uid)) {
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

    public void applyCategoryIfUnlocked(Transaction t) {
        if (t == null) return;
        // Do nothing if user (or a previous rule) has locked it
        if (t.isCategoryLocked()) return;

        // Normalize the merchant the same way you do elsewhere
        String merchantNorm = normalize(t.getMerchant());

        // Try to find a matching category via rules; if found, set and lock
        this.apply(merchantNorm).ifPresent(cat -> {
            t.setCategoryRef(cat);
            t.setCategory(cat.getName());   // keep legacy text in sync
            t.setCategoryLocked(true);      // lock because a rule made the decision
        });
    }
}