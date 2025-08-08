package com.anshdesai.finpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class FinpilotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinpilotBackendApplication.class, args);
    }

    @GetMapping
    public String welcome(){
        return "Welcome to Finpilot";
    }
}
