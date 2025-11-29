package com.example.book_manager.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate
import java.time.OffsetDateTime

data class AuthorCreateRequest(
    @field:NotBlank
    val name: String,
    @field:PastOrPresent
    val birthDate: LocalDate
)

data class AuthorUpdateRequest(
    @field:NotBlank
    val name: String,
    @field:PastOrPresent
    val birthDate: LocalDate
)

data class AuthorResponse(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)


