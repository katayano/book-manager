package com.example.book_manager.dto

import com.example.book_manager.domain.BookStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime


 data class BookCreateRequest(
    @field:NotBlank
    val title: String,
    @field:Min(0)
    val price: Int,
    @field:NotNull
    val status: BookStatus,
    @field:NotEmpty
    val authorIds: List<Long>
)

 data class BookUpdateRequest(
    @field:NotBlank
    val title: String,
    @field:Min(0)
    val price: Int,
    @field:NotNull
    val status: BookStatus,
    @field:NotEmpty
    val authorIds: List<Long>
)

data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val status: BookStatus,
    val authorIds: List<Long>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

