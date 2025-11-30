package com.example.book_manager.service

import com.example.book_manager.domain.BookStatus
import com.example.book_manager.dto.AuthorCreateRequest
import com.example.book_manager.dto.AuthorResponse
import com.example.book_manager.dto.AuthorUpdateRequest
import com.example.book_manager.dto.BookResponse
import com.example.book_manager.exception.NotFoundException
import com.example.book_manager.repository.AuthorRepository
import com.example.book_manager.repository.BookRepository
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

        return AuthorResponse(
            id = authorRecord.id!!,
            name = authorRecord.name!!,
            birthDate = authorRecord.birthDate!!,
            createdAt = authorRecord.createdAt!!,
            updatedAt = authorRecord.updatedAt!!
        )
    }

    override fun updateAuthor(authorId: Long, request: AuthorUpdateRequest): AuthorResponse {
        val updated = authorRepository.update(
            authorId = authorId,
            name = request.name,
            birthDate = request.birthDate
        ) ?: throw NotFoundException("Author not found: $authorId")

        return AuthorResponse(
            id = updated.id!!,
            name = updated.name!!,
            birthDate = updated.birthDate!!,
            createdAt = updated.createdAt!!,
            updatedAt = updated.updatedAt!!
        )
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
            val authorIds = bookRepository.findAuthorIdsByBookId(book.id!!)
            BookResponse(
                id = book.id!!,
                title = book.title!!,
                price = book.price!!,
                status = BookStatus.valueOf(book.status!!),
                authorIds = authorIds,
                createdAt = book.createdAt!!,
                updatedAt = book.updatedAt!!
            )
        }
    }
}

