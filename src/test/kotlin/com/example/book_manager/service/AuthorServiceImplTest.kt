package com.example.book_manager.service

import com.example.book_manager.domain.BookStatus
import com.example.book_manager.dto.AuthorCreateRequest
import com.example.book_manager.dto.AuthorUpdateRequest
import com.example.book_manager.exception.NotFoundException
import com.example.book_manager.jooq.tables.records.AuthorsRecord
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
import java.time.LocalDate
import java.time.OffsetDateTime

class AuthorServiceImplTest {

    private val authorRepository: AuthorRepository = mock()
    private val bookRepository: BookRepository = mock()
    private val authorService = AuthorServiceImpl(authorRepository, bookRepository)

    @Test
    fun `createAuthor - 正常系 - 著者が正常に作成される`() {
        // given
        val request = AuthorCreateRequest(
            name = "山田太郎",
            birthDate = LocalDate.of(1980, 1, 1)
        )

        val authorRecord = AuthorsRecord().apply {
            id = 1L
            name = request.name
            birthDate = request.birthDate
            createdAt = OffsetDateTime.now()
            updatedAt = OffsetDateTime.now()
        }

        whenever(authorRepository.create(request.name, request.birthDate)).thenReturn(authorRecord)

        // when
        val result = authorService.createAuthor(request)

        // then
        assertEquals(1L, result.id)
        assertEquals("山田太郎", result.name)
        assertEquals(LocalDate.of(1980, 1, 1), result.birthDate)

        verify(authorRepository).create(request.name, request.birthDate)
    }

    @Test
    fun `updateAuthor - 正常系 - 著者が正常に更新される`() {
        // given
        val authorId = 1L
        val request = AuthorUpdateRequest(
            name = "鈴木花子",
            birthDate = LocalDate.of(1990, 5, 15)
        )

        val updatedAuthor = AuthorsRecord().apply {
            id = authorId
            name = request.name
            birthDate = request.birthDate
            createdAt = OffsetDateTime.now().minusDays(10)
            updatedAt = OffsetDateTime.now()
        }

        whenever(authorRepository.update(authorId, request.name, request.birthDate))
            .thenReturn(updatedAuthor)

        // when
        val result = authorService.updateAuthor(authorId, request)

        // then
        assertEquals(authorId, result.id)
        assertEquals("鈴木花子", result.name)
        assertEquals(LocalDate.of(1990, 5, 15), result.birthDate)

        verify(authorRepository).update(authorId, request.name, request.birthDate)
    }

    @Test
    fun `updateAuthor - 異常系 - 著者が存在しない場合はNotFoundException`() {
        // given
        val authorId = 999L
        val request = AuthorUpdateRequest(
            name = "鈴木花子",
            birthDate = LocalDate.of(1990, 5, 15)
        )

        whenever(authorRepository.update(authorId, request.name, request.birthDate))
            .thenReturn(null)

        // when & then
        val exception = assertThrows<NotFoundException> {
            authorService.updateAuthor(authorId, request)
        }

        assertEquals("Author not found: $authorId", exception.message)
        verify(authorRepository).update(authorId, request.name, request.birthDate)
    }

    @Test
    fun `deleteAuthor - 正常系 - 著者が正常に削除される`() {
        // given
        val authorId = 1L
        whenever(authorRepository.delete(authorId)).thenReturn(1)

        // when
        authorService.deleteAuthor(authorId)

        // then
        verify(authorRepository).delete(authorId)
    }

    @Test
    fun `deleteAuthor - 異常系 - 著者が存在しない場合はNotFoundException`() {
        // given
        val authorId = 999L
        whenever(authorRepository.delete(authorId)).thenReturn(0)

        // when & then
        val exception = assertThrows<NotFoundException> {
            authorService.deleteAuthor(authorId)
        }

        assertEquals("Author not found: $authorId", exception.message)
        verify(authorRepository).delete(authorId)
    }

    @Test
    fun `findBooksByAuthor - 正常系 - 著者の書籍一覧が正常に取得される`() {
        // given
        val authorId = 1L
        val bookRecords = listOf(
            BooksRecord().apply {
                id = 1L
                title = "書籍1"
                price = 1000
                status = BookStatus.PUBLISHED.name
                createdAt = OffsetDateTime.now()
                updatedAt = OffsetDateTime.now()
            },
            BooksRecord().apply {
                id = 2L
                title = "書籍2"
                price = 2000
                status = BookStatus.UNPUBLISHED.name
                createdAt = OffsetDateTime.now()
                updatedAt = OffsetDateTime.now()
            }
        )

        whenever(authorRepository.existsById(authorId)).thenReturn(true)
        whenever(bookRepository.findBooksByAuthorId(authorId, null)).thenReturn(bookRecords)
        whenever(bookRepository.findAuthorIdsByBookId(1L)).thenReturn(listOf(authorId, 2L))
        whenever(bookRepository.findAuthorIdsByBookId(2L)).thenReturn(listOf(authorId))

        // when
        val result = authorService.findBooksByAuthor(authorId)

        // then
        assertEquals(2, result.size)
        assertEquals("書籍1", result[0].title)
        assertEquals(1000, result[0].price)
        assertEquals(BookStatus.PUBLISHED, result[0].status)
        assertEquals(listOf(authorId, 2L), result[0].authorIds)

        assertEquals("書籍2", result[1].title)
        assertEquals(2000, result[1].price)
        assertEquals(BookStatus.UNPUBLISHED, result[1].status)
        assertEquals(listOf(authorId), result[1].authorIds)

        verify(authorRepository).existsById(authorId)
        verify(bookRepository).findBooksByAuthorId(authorId, null)
        verify(bookRepository).findAuthorIdsByBookId(1L)
        verify(bookRepository).findAuthorIdsByBookId(2L)
    }

    @Test
    fun `findBooksByAuthor - 正常系 - ステータス指定で著者の書籍を取得`() {
        // given
        val authorId = 1L
        val bookStatus = BookStatus.PUBLISHED
        val bookRecords = listOf(
            BooksRecord().apply {
                id = 1L
                title = "出版済み書籍"
                price = 1000
                status = BookStatus.PUBLISHED.name
                createdAt = OffsetDateTime.now()
                updatedAt = OffsetDateTime.now()
            }
        )

        whenever(authorRepository.existsById(authorId)).thenReturn(true)
        whenever(bookRepository.findBooksByAuthorId(authorId, bookStatus)).thenReturn(bookRecords)
        whenever(bookRepository.findAuthorIdsByBookId(1L)).thenReturn(listOf(authorId))

        // when
        val result = authorService.findBooksByAuthor(authorId, bookStatus)

        // then
        assertEquals(1, result.size)
        assertEquals("出版済み書籍", result[0].title)
        assertEquals(BookStatus.PUBLISHED, result[0].status)

        verify(authorRepository).existsById(authorId)
        verify(bookRepository).findBooksByAuthorId(authorId, bookStatus)
        verify(bookRepository).findAuthorIdsByBookId(1L)
    }

    @Test
    fun `findBooksByAuthor - 正常系 - 著者の書籍が0件の場合は空リスト`() {
        // given
        val authorId = 1L
        whenever(authorRepository.existsById(authorId)).thenReturn(true)
        whenever(bookRepository.findBooksByAuthorId(authorId, null)).thenReturn(emptyList<BooksRecord>())

        // when
        val result = authorService.findBooksByAuthor(authorId)

        // then
        assertEquals(0, result.size)

        verify(authorRepository).existsById(authorId)
        verify(bookRepository).findBooksByAuthorId(authorId, null)
    }

    @Test
    fun `findBooksByAuthor - 異常系 - 著者が存在しない場合はNotFoundException`() {
        // given
        val authorId = 999L
        whenever(authorRepository.existsById(authorId)).thenReturn(false)

        // when & then
        val exception = assertThrows<NotFoundException> {
            authorService.findBooksByAuthor(authorId)
        }

        assertEquals("Author not found: $authorId", exception.message)
        verify(authorRepository).existsById(authorId)
        verify(bookRepository, never()).findBooksByAuthorId(any(), any())
    }
}

