package com.example.book_manager.service

import com.example.book_manager.dto.BookCreateRequest
import com.example.book_manager.dto.BookResponse
import com.example.book_manager.dto.BookUpdateRequest

interface BookService {
    fun createBook(request: BookCreateRequest): BookResponse
    fun updateBook(bookId: Long, request: BookUpdateRequest): BookResponse
    fun deleteBook(bookId: Long)
}

