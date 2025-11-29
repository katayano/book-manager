package com.example.book_manager.controller

import com.example.book_manager.dto.BookCreateRequest
import com.example.book_manager.dto.BookResponse
import com.example.book_manager.dto.BookUpdateRequest
import com.example.book_manager.service.BookService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService
) {

    @PostMapping
    fun createBook(@Valid @RequestBody request: BookCreateRequest): ResponseEntity<BookResponse> {
        val created = bookService.createBook(request)
        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(location).body(created)
    }

    @PutMapping("/{bookId}")
    fun updateBook(
        @PathVariable bookId: Long,
        @Valid @RequestBody request: BookUpdateRequest
    ): ResponseEntity<BookResponse> {
        val updated = bookService.updateBook(bookId, request)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{bookId}")
    fun deleteBook(@PathVariable bookId: Long): ResponseEntity<Void> {
        bookService.deleteBook(bookId)
        return ResponseEntity.noContent().build()
    }
}

