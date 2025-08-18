package com.anshdesai.finpilot.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class FirebaseConfig {

    // NEW: allow a Spring property fallback
    @Value("${firebase.credentials:}")
    private String firebaseCredentialsPath; // empty if not set

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // If already initialized (e.g., by tests), just reuse it.
        List<FirebaseApp> apps = FirebaseApp.getApps();
        if (!apps.isEmpty()) {
            return apps.get(0);
        }

        GoogleCredentials credentials;

        // 1) Prefer explicit service account file via ENV
        String saPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        // 2) If ENV missing, try Spring property
        if ((saPath == null || saPath.isBlank()) &&
                firebaseCredentialsPath != null && !firebaseCredentialsPath.isBlank()) {
            saPath = firebaseCredentialsPath;
        }

        if (saPath != null && !saPath.isBlank()) {
            try (InputStream in = new FileInputStream(saPath)) {
                credentials = GoogleCredentials.fromStream(in);
            }
        } else {
            // 3) Last resort: Application Default Credentials (gcloud ADC)
            credentials = GoogleCredentials.getApplicationDefault();
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp app) {
        return FirebaseAuth.getInstance(app);
    }
}