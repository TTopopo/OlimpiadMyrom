package com.olimpiada.entity;

public enum TaskType {
    SINGLE_CHOICE("Один правильный ответ"),
    MULTIPLE_CHOICE("Несколько правильных ответов"),
    TEXT_ANSWER("Текстовый ответ"),
    CODE_ANSWER("Ответ с кодом");

    private final String displayName;

    TaskType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 