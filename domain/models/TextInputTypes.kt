package com.example.presentations.questionnaire.domain.models


enum class TextInputTypes {
    PHONE, NAME, NUMBER, TEXT;

    companion object {
        fun typeOf(key: String?) = values().find { it.name == key } ?: TEXT
    }
}