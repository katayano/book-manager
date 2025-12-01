package com.example.book_manager.service

import com.example.book_manager.domain.BookStatus
import com.example.book_manager.dto.BookCreateRequest
import com.example.book_manager.dto.BookResponse
import com.example.book_manager.dto.BookUpdateRequest
import com.example.book_manager.exception.BusinessRuleViolationException
import com.example.book_manager.exception.NotFoundException
import com.example.book_manager.repository.AuthorRepository
import com.example.book_manager.repository.BookRepository
import com.example.book_manager.repository.toBookResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository
) : BookService {

    override fun createBook(request: BookCreateRequest): BookResponse {
        // 著者が全員存在することを確認
        if (!authorRepository.existsByIds(request.authorIds)) {
            throw NotFoundException("One or more authors not found")
        }

        // 書籍を登録
        val bookRecord = bookRepository.create(
            title = request.title,
            price = request.price,
            status = request.status
        )

        // 中間テーブルに著者を紐付け
        val bookId = bookRecord.id ?: throw IllegalStateException("Book ID is null after creation")
        bookRepository.addAuthors(bookId, request.authorIds)

        return bookRecord.toBookResponse(request.authorIds)
    }

    override fun updateBook(bookId: Long, request: BookUpdateRequest): BookResponse {
        // 既存の書籍を取得
        val existingBook = bookRepository.findById(bookId)
            ?: throw NotFoundException("Book not found: $bookId")

        // 出版済み→未出版への変更を禁止
        val currentStatus = existingBook.status?.let { BookStatus.valueOf(it) }
            ?: throw IllegalStateException("Book status is null")
        if (currentStatus == BookStatus.PUBLISHED && request.status == BookStatus.UNPUBLISHED) {
            throw BusinessRuleViolationException("Cannot change status from PUBLISHED to UNPUBLISHED")
        }

        // 著者が全員存在することを確認
        if (!authorRepository.existsByIds(request.authorIds)) {
            throw NotFoundException("One or more authors not found")
        }

        // 書籍を更新
        val updated = bookRepository.update(
            bookId = bookId,
            title = request.title,
            price = request.price,
            status = request.status
        ) ?: throw NotFoundException("Book not found: $bookId")

        // 著者を更新(既存を削除して再登録)
        bookRepository.removeAllAuthors(bookId)
        bookRepository.addAuthors(bookId, request.authorIds)

        return updated.toBookResponse(request.authorIds)
    }

    override fun deleteBook(bookId: Long) {
        val deleted = bookRepository.delete(bookId)
        if (deleted == 0) {
            throw NotFoundException("Book not found: $bookId")
        }
    }
}

