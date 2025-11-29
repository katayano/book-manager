package com.example.book_manager.service

import com.example.book_manager.domain.BookStatus
import com.example.book_manager.dto.AuthorCreateRequest
import com.example.book_manager.dto.AuthorResponse
import com.example.book_manager.dto.AuthorUpdateRequest
import com.example.book_manager.dto.BookResponse

interface AuthorService {
    fun createAuthor(request: AuthorCreateRequest): AuthorResponse
    fun updateAuthor(authorId: Long, request: AuthorUpdateRequest): AuthorResponse
    fun deleteAuthor(authorId: Long)
    fun findBooksByAuthor(authorId: Long, status: BookStatus? = null): List<BookResponse>
}

