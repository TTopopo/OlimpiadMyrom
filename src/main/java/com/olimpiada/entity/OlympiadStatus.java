package com.olimpiada.entity;

public enum OlympiadStatus {
    DRAFT("Черновик - олимпиада создана, но еще не опубликована"),
    PUBLISHED("Опубликована - олимпиада доступна для просмотра"),
    ACTIVE("Активна - олимпиада идет, участники могут отправлять ответы"),
    FINISHED("Завершена - олимпиада закончилась"),
    CANCELLED("Отменена - олимпиада отменена");

    private final String description;

    OlympiadStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
} 