package com.anshdesai.finpilot;

import com.anshdesai.finpilot.config.PlaidProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(PlaidProperties.class)
public class FinpilotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinpilotBackendApplication.class, args);
    }

    @GetMapping
    public String welcome(){
        return "Welcome to Finpilot";
    }
}
