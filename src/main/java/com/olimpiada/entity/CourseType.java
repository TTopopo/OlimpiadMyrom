package com.olimpiada.entity;

public enum CourseType {
    BACHELOR("Бакалавриат"),
    MASTER("Магистратура"),
    SPO("СПО");

    private final String displayName;

    CourseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 