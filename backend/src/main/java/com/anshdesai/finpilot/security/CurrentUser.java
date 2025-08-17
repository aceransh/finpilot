package com.anshdesai.finpilot.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class CurrentUser {
    private String uid = "demo";

    public String userId() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}