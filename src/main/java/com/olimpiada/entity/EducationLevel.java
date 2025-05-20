package com.olimpiada.entity;

public enum EducationLevel {
    BACHELOR("Бакалавриат"),
    MASTER("Магистратура");

    private final String displayName;

    EducationLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 