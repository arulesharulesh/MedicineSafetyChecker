package com.example.drugchecker.model;

public enum Severity {
    NONE,
    LOW,
    MODERATE,
    HIGH;

    public static Severity fromStringSafe(String value) {
        if (value == null) return NONE;
        try {
            return Severity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
