package com.olimpiada.entity;

public enum EducationLevel {
    BACHELOR("Бакалавриат"),
    MASTER("Магистратура"),
    SPO("СПО");

    private final String displayName;

    EducationLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 