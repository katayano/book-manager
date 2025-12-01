package com.example.book_manager.service

import com.example.book_manager.domain.BookStatus
import com.example.book_manager.dto.AuthorCreateRequest
import com.example.book_manager.dto.AuthorResponse
import com.example.book_manager.dto.AuthorUpdateRequest
import com.example.book_manager.dto.BookResponse
import com.example.book_manager.exception.NotFoundException
import com.example.book_manager.repository.AuthorRepository
import com.example.book_manager.repository.BookRepository
import com.example.book_manager.repository.toAuthorResponse
import com.example.book_manager.repository.toBookResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthorServiceImpl(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository
) : AuthorService {

    override fun createAuthor(request: AuthorCreateRequest): AuthorResponse {
        val authorRecord = authorRepository.create(
            name = request.name,
            birthDate = request.birthDate
        )

        return authorRecord.toAuthorResponse()
    }

    override fun updateAuthor(authorId: Long, request: AuthorUpdateRequest): AuthorResponse {
        val updated = authorRepository.update(
            authorId = authorId,
            name = request.name,
            birthDate = request.birthDate
        ) ?: throw NotFoundException("Author not found: $authorId")

        return updated.toAuthorResponse()
    }

    override fun deleteAuthor(authorId: Long) {
        val deleted = authorRepository.delete(authorId)
        if (deleted == 0) {
            throw NotFoundException("Author not found: $authorId")
        }
    }

    override fun findBooksByAuthor(authorId: Long, status: BookStatus?): List<BookResponse> {
        // 著者が存在するか確認
        if (!authorRepository.existsById(authorId)) {
            throw NotFoundException("Author not found: $authorId")
        }

        val bookRecords = bookRepository.findBooksByAuthorId(authorId, status)

        return bookRecords.map { book ->
            val bookId = book.id ?: throw IllegalStateException("Book ID is null")
            val authorIds = bookRepository.findAuthorIdsByBookId(bookId)
            book.toBookResponse(authorIds)
        }
    }
}

