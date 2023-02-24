package com.example.presentations.questionnaire.domain.models


enum class FieldSettings(val key: String) {
    MASK("mask"),
    TITLE("title"),
    HINT("hint"),
    INPUT_TYPE("type"),
    HIDDEN_KEY("hidden_key"),
    MIN_DATE("min_date"),
    MAX_DATE("max_date")
}