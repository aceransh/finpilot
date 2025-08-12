package com.anshdesai.finpilot.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CategoryRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "EXPENSE|INCOME", message = "type must be EXPENSE or INCOME")
    private String type;

    // optional hex color like #2E7D32; keep loose for MVP
    private String color;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}