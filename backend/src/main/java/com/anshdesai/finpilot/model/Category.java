package com.anshdesai.finpilot.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 👇 NEW: owner column
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false) // EXPENSE | INCOME
    private String type;

    private String color;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    // 👇 NEW getter/setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}