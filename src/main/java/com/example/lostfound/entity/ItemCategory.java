package com.example.lostfound.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {
    ELECTRONICS("Electronics"),
    BAGS("Bags"),
    DOCUMENTS_ID("Documents and ID cards"),
    BOOKS_STATIONERY("Books and stationery"),
    CLOTHING("Clothing"),
    ACCESSORIES("Accessories"),
    KEYS("Keys"),
    OTHER("Other");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ItemCategory fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        for (ItemCategory category : ItemCategory.values()) {
            if (category.displayName.equalsIgnoreCase(value.trim()) ||
                category.name().equalsIgnoreCase(value.trim().replace(" ", "_").replace("&", "").replace("-", "_"))) {
                return category;
            }
        }
        return OTHER;
    }
}
