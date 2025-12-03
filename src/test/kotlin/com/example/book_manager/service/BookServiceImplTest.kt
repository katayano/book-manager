package com.example.book_manager.service

import com.example.book_manager.domain.BookStatus
import com.example.book_manager.dto.BookCreateRequest
import com.example.book_manager.dto.BookUpdateRequest
import com.example.book_manager.exception.BusinessRuleViolationException
import com.example.book_manager.exception.NotFoundException
import com.example.book_manager.jooq.tables.records.BooksRecord
import com.example.book_manager.repository.AuthorRepository
import com.example.book_manager.repository.BookRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import java.time.OffsetDateTime

class BookServiceImplTest {

    private val bookRepository: BookRepository = mock()
    private val authorRepository: AuthorRepository = mock()
    private val bookService = BookServiceImpl(bookRepository, authorRepository)

    @Test
    fun `createBook - 正常系 - 書籍が正常に作成される`() {
        // given
        val request = BookCreateRequest(
            title = "テスト書籍",
            price = 1000,
            status = BookStatus.UNPUBLISHED,
            authorIds = listOf(1L, 2L)
        )

        val bookRecord = BooksRecord().apply {
            id = 1L
            title = request.title
            price = request.price
            status = request.status.name
            createdAt = OffsetDateTime.now()
            updatedAt = OffsetDateTime.now()
        }

        whenever(authorRepository.existsByIds(request.authorIds)).thenReturn(true)
        whenever(bookRepository.create(request.title, request.price, request.status)).thenReturn(bookRecord)

        // when
        val result = bookService.createBook(request)

        // then
        assertEquals(1L, result.id)
        assertEquals("テスト書籍", result.title)
        assertEquals(1000, result.price)
        assertEquals(BookStatus.UNPUBLISHED, result.status)
        assertEquals(listOf(1L, 2L), result.authorIds)

        verify(authorRepository).existsByIds(request.authorIds)
        verify(bookRepository).create(request.title, request.price, request.status)
        verify(bookRepository).addAuthors(1L, request.authorIds)
    }

    @Test
    fun `createBook - 異常系 - 著者が存在しない場合はNotFoundException`() {
        // given
        val request = BookCreateRequest(
            title = "テスト書籍",
            price = 1000,
            status = BookStatus.UNPUBLISHED,
            authorIds = listOf(999L)
        )

        whenever(authorRepository.existsByIds(request.authorIds)).thenReturn(false)

        // when & then
        val exception = assertThrows<NotFoundException> {
            bookService.createBook(request)
        }

        assertEquals("One or more authors not found", exception.message)
        verify(authorRepository).existsByIds(request.authorIds)
        verify(bookRepository, never()).create(any(), any(), any())
    }

    @Test
    fun `updateBook - 正常系 - 書籍が正常に更新される`() {
        // given
        val bookId = 1L
        val request = BookUpdateRequest(
            title = "更新後のタイトル",
            price = 2000,
            status = BookStatus.PUBLISHED,
            authorIds = listOf(1L, 3L)
        )

        val existingBook = BooksRecord().apply {
            id = bookId
            title = "元のタイトル"
            price = 1000
            status = BookStatus.UNPUBLISHED.name
            createdAt = OffsetDateTime.now()
            updatedAt = OffsetDateTime.now()
        }

        val updatedBook = BooksRecord().apply {
            id = bookId
            title = request.title
            price = request.price
            status = request.status.name
            createdAt = existingBook.createdAt
            updatedAt = OffsetDateTime.now()
        }

        whenever(bookRepository.findById(bookId)).thenReturn(existingBook)
        whenever(authorRepository.existsByIds(request.authorIds)).thenReturn(true)
        whenever(bookRepository.update(bookId, request.title, request.price, request.status))
            .thenReturn(updatedBook)

        // when
        val result = bookService.updateBook(bookId, request)

        // then
        assertEquals(bookId, result.id)
        assertEquals("更新後のタイトル", result.title)
        assertEquals(2000, result.price)
        assertEquals(BookStatus.PUBLISHED, result.status)
        assertEquals(listOf(1L, 3L), result.authorIds)

        verify(bookRepository).findById(bookId)
        verify(authorRepository).existsByIds(request.authorIds)
        verify(bookRepository).update(bookId, request.title, request.price, request.status)
        verify(bookRepository).removeAllAuthors(bookId)
        verify(bookRepository).addAuthors(bookId, request.authorIds)
    }

    @Test
    fun `updateBook - 異常系 - 書籍が存在しない場合はNotFoundException`() {
        // given
        val bookId = 999L
        val request = BookUpdateRequest(
            title = "更新後のタイトル",
            price = 2000,
            status = BookStatus.PUBLISHED,
            authorIds = listOf(1L)
        )

        whenever(bookRepository.findById(bookId)).thenReturn(null)

        // when & then
        val exception = assertThrows<NotFoundException> {
            bookService.updateBook(bookId, request)
        }

        assertEquals("Book not found: $bookId", exception.message)
        verify(bookRepository).findById(bookId)
        verify(bookRepository, never()).update(any(), any(), any(), any())
    }

    @Test
    fun `updateBook - 異常系 - 出版済みから未出版への変更は禁止`() {
        // given
        val bookId = 1L
        val request = BookUpdateRequest(
            title = "更新後のタイトル",
            price = 2000,
            status = BookStatus.UNPUBLISHED,
            authorIds = listOf(1L)
        )

        val existingBook = BooksRecord().apply {
            id = bookId
            title = "元のタイトル"
            price = 1000
            status = BookStatus.PUBLISHED.name
            createdAt = OffsetDateTime.now()
            updatedAt = OffsetDateTime.now()
        }

        whenever(bookRepository.findById(bookId)).thenReturn(existingBook)

        // when & then
        val exception = assertThrows<BusinessRuleViolationException> {
            bookService.updateBook(bookId, request)
        }

        assertEquals("Cannot change status from PUBLISHED to UNPUBLISHED", exception.message)
        verify(bookRepository).findById(bookId)
        verify(bookRepository, never()).update(any(), any(), any(), any())
    }

    @Test
    fun `updateBook - 異常系 - 著者が存在しない場合はNotFoundException`() {
        // given
        val bookId = 1L
        val request = BookUpdateRequest(
            title = "更新後のタイトル",
            price = 2000,
            status = BookStatus.PUBLISHED,
            authorIds = listOf(999L)
        )

        val existingBook = BooksRecord().apply {
            id = bookId
            title = "元のタイトル"
            price = 1000
            status = BookStatus.UNPUBLISHED.name
            createdAt = OffsetDateTime.now()
            updatedAt = OffsetDateTime.now()
        }

        whenever(bookRepository.findById(bookId)).thenReturn(existingBook)
        whenever(authorRepository.existsByIds(request.authorIds)).thenReturn(false)

        // when & then
        val exception = assertThrows<NotFoundException> {
            bookService.updateBook(bookId, request)
        }

        assertEquals("One or more authors not found", exception.message)
        verify(bookRepository).findById(bookId)
        verify(authorRepository).existsByIds(request.authorIds)
        verify(bookRepository, never()).update(any(), any(), any(), any())
    }

    @Test
    fun `deleteBook - 正常系 - 書籍が正常に削除される`() {
        // given
        val bookId = 1L
        whenever(bookRepository.delete(bookId)).thenReturn(1)

        // when
        bookService.deleteBook(bookId)

        // then
        verify(bookRepository).delete(bookId)
    }

    @Test
    fun `deleteBook - 異常系 - 書籍が存在しない場合はNotFoundException`() {
        // given
        val bookId = 999L
        whenever(bookRepository.delete(bookId)).thenReturn(0)

        // when & then
        val exception = assertThrows<NotFoundException> {
            bookService.deleteBook(bookId)
        }

        assertEquals("Book not found: $bookId", exception.message)
        verify(bookRepository).delete(bookId)
    }
}

