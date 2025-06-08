package com.olimpiada.entity;

public enum ResultStatus {
    PENDING("Ожидает проверки"),
    IN_PROGRESS("В процессе"),
    COMPLETED("Завершено"),
    DISQUALIFIED("Дисквалифицирован");

    private final String displayName;
    ResultStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
} 