package com.websbaba.nitigrow.domain.model

import java.time.Instant

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val email: String?,
    val avatarUrl: String?,
    val tags: List<String>,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isBlocked: Boolean
) {
    val initial: Char
        get() = name.firstOrNull { it.isLetter() }?.uppercaseChar() ?: '#'
}
