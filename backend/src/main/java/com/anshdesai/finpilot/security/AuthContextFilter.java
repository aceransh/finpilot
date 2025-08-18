package com.anshdesai.finpilot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Optional;

/**
 * Sets CurrentUser for each request.
 * Order of precedence:
 *  1) Authorization: Bearer <Firebase ID token>  (real user)
 *  2) X-Demo-User header                         (local dev override)
 *  3) "demo" fallback                            (default)
 *
 * In this step we DO NOT verify Firebase yet to avoid adding dependencies.
 * The hook verifyFirebase(...) is a placeholder we'll implement next.
 */
@Component
@Order(10) // run early, before controllers
public class AuthContextFilter extends OncePerRequestFilter {

    private final CurrentUser currentUser;
    private final FirebaseAuth firebaseAuth;

    @Value("${app.auth.allow-demo:true}")
    private boolean allowDemo;   // dev-friendly default


    public AuthContextFilter(CurrentUser currentUser, FirebaseAuth firebaseAuth) {
        this.currentUser = currentUser;
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * Placeholder for Firebase verification.
     * NEXT STEP: Implement using Firebase Admin SDK:
     *   FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
     *   return Optional.of(token.getUid());
     */
    private Optional<FirebaseToken> verifyFirebase(String idToken) {
        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken);
            return Optional.of(decoded);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // --- 1) Try real auth first (Firebase) ---
        String uid = null;
        String email = null;
        String name = null;
        String authz = request.getHeader("Authorization");
        if (StringUtils.hasText(authz) && authz.startsWith("Bearer ")) {
            String idToken = authz.substring("Bearer ".length()).trim();
            if (StringUtils.hasText(idToken)) {
                Optional<FirebaseToken> tokOpt = verifyFirebase(idToken);
                if (tokOpt.isPresent()) {
                    FirebaseToken tok = tokOpt.get();
                    uid = tok.getUid();
                    email = tok.getEmail();       // may be null if not shared
                    name  = tok.getName();        // may be null if not set
                }
            }
        }

        // --- 2) If no real auth, allow dev/demo header only when enabled ---
        if (!StringUtils.hasText(uid)) {
            String demoUid = request.getHeader("X-Demo-User");
            if (allowDemo && StringUtils.hasText(demoUid)) {
                uid = demoUid.trim();
            }
        }

        // --- 3) If still no uid, either fall back to "demo" (when allowed) or 401 ---
        if (!StringUtils.hasText(uid)) {
            if (allowDemo) {
                uid = "demo"; // convenient local default
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"unauthorized\"}");
                return;
            }
        }

        // --- 4) Stamp the request-scoped bean & continue ---
        currentUser.setUid(uid);
        currentUser.setEmail(email);          // may be null in demo mode
        currentUser.setDisplayName(name);     // may be null in demo mode
        response.setHeader("X-Auth-User", uid);
        if (email != null) response.setHeader("X-Auth-Email", email);
        if (name  != null) response.setHeader("X-Auth-Name",  name);
        filterChain.doFilter(request, response);
    }
}