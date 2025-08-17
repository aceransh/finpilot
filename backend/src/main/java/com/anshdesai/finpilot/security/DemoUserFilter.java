package com.anshdesai.finpilot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(10)
public class DemoUserFilter extends OncePerRequestFilter {

    private final CurrentUser currentUser;

    public DemoUserFilter(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 1) Read dev header; fall back to "demo"
        String uid = request.getHeader("X-Demo-User");
        if (uid == null || uid.isBlank()) {
            uid = "demo";
        }

        // 2) Stash on our request-scoped holder
        currentUser.setUid(uid);

        // 3) Continue the chain
        filterChain.doFilter(request, response);
    }
}