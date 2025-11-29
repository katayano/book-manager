package com.example.book_manager.controller

import com.example.book_manager.dto.AuthorCreateRequest
import com.example.book_manager.dto.AuthorResponse
import com.example.book_manager.dto.AuthorUpdateRequest
import com.example.book_manager.dto.BookResponse
import com.example.book_manager.domain.BookStatus
import com.example.book_manager.service.AuthorService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/authors")
class AuthorController(
    private val authorService: AuthorService
) {

    @PostMapping
    fun createAuthor(@Valid @RequestBody request: AuthorCreateRequest): ResponseEntity<AuthorResponse> {
        val created = authorService.createAuthor(request)
        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(location).body(created)
    }

    @PutMapping("/{authorId}")
    fun updateAuthor(
        @PathVariable authorId: Long,
        @Valid @RequestBody request: AuthorUpdateRequest
    ): ResponseEntity<AuthorResponse> {
        val updated = authorService.updateAuthor(authorId, request)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{authorId}")
    fun deleteAuthor(@PathVariable authorId: Long): ResponseEntity<Void> {
        authorService.deleteAuthor(authorId)
        return ResponseEntity.noContent().build()
    }


    @GetMapping("/{authorId}/books")
    fun findBooksByAuthor(
        @PathVariable authorId: Long,
        @RequestParam(required = false) status: BookStatus?
    ): ResponseEntity<List<BookResponse>> {
        val books = authorService.findBooksByAuthor(authorId, status)
        return ResponseEntity.ok(books)
    }
}

