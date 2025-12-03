package com.example.book_manager.controller

import com.example.book_manager.domain.BookStatus
import com.example.book_manager.dto.BookCreateRequest
import com.example.book_manager.dto.BookResponse
import com.example.book_manager.dto.BookUpdateRequest
import com.example.book_manager.exception.BusinessRuleViolationException
import com.example.book_manager.exception.NotFoundException
import com.example.book_manager.service.BookService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @MockitoBean
    private lateinit var bookService: BookService

    @Test
    fun `POST books - 正常系 - 書籍を作成できる`() {
        // given
        val request = BookCreateRequest(
            title = "テスト書籍",
            price = 0,
            status = BookStatus.UNPUBLISHED,
            authorIds = listOf(1L, 2L)
        )

        val response = BookResponse(
            id = 1L,
            title = request.title,
            price = request.price,
            status = request.status,
            authorIds = request.authorIds,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )

        whenever(bookService.createBook(request)).thenReturn(response)

        // when & then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("テスト書籍"))
            .andExpect(jsonPath("$.price").value(0))
            .andExpect(jsonPath("$.status").value("UNPUBLISHED"))
            .andExpect(jsonPath("$.authorIds[0]").value(1))
            .andExpect(jsonPath("$.authorIds[1]").value(2))

        verify(bookService).createBook(request)
    }

    @Test
    fun `POST books - 異常系 - タイトルが空の場合はバリデーションエラー`() {
        // given
        val request = mapOf(
            "title" to "",
            "price" to 1000,
            "status" to "UNPUBLISHED",
            "authorIds" to listOf(1L)
        )

        // when & then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(bookService, never()).createBook(any())
    }

    @Test
    fun `POST books - 異常系 - 価格がマイナスの場合はバリデーションエラー`() {
        // given
        val request = mapOf(
            "title" to "テスト書籍",
            "price" to -1,
            "status" to "UNPUBLISHED",
            "authorIds" to listOf(1L)
        )

        // when & then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(bookService, never()).createBook(any())
    }

    @Test
    fun `POST books - 異常系 - authorIdsが空の場合はバリデーションエラー`() {
        // given
        val request = mapOf(
            "title" to "テスト書籍",
            "price" to 1000,
            "status" to "UNPUBLISHED",
            "authorIds" to emptyList<Long>()
        )

        // when & then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(bookService, never()).createBook(any())
    }

    @Test
    fun `POST books - 異常系 - 著者が存在しない場合は404エラー`() {
        // given
        val request = BookCreateRequest(
            title = "テスト書籍",
            price = 1000,
            status = BookStatus.UNPUBLISHED,
            authorIds = listOf(999L)
        )

        whenever(bookService.createBook(any())).thenThrow(NotFoundException("One or more authors not found"))

        // when & then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)

        verify(bookService).createBook(any())
    }

    @Test
    fun `PUT books - 正常系 - 書籍を更新できる`() {
        // given
        val bookId = 1L
        val request = BookUpdateRequest(
            title = "更新後のタイトル",
            price = 2000,
            status = BookStatus.PUBLISHED,
            authorIds = listOf(1L, 3L)
        )

        val response = BookResponse(
            id = bookId,
            title = request.title,
            price = request.price,
            status = request.status,
            authorIds = request.authorIds,
            createdAt = OffsetDateTime.now().minusDays(1),
            updatedAt = OffsetDateTime.now()
        )

        whenever(bookService.updateBook(eq(bookId), eq(request))).thenReturn(response)

        // when & then
        mockMvc.perform(
            put("/api/books/$bookId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("更新後のタイトル"))
            .andExpect(jsonPath("$.price").value(2000))
            .andExpect(jsonPath("$.status").value("PUBLISHED"))

        verify(bookService).updateBook(eq(bookId), eq(request))
    }

    @Test
    fun `PUT books - 異常系 - 書籍が存在しない場合は404エラー`() {
        // given
        val bookId = 999L
        val request = BookUpdateRequest(
            title = "更新後のタイトル",
            price = 2000,
            status = BookStatus.PUBLISHED,
            authorIds = listOf(1L)
        )

        whenever(bookService.updateBook(eq(bookId), eq(request)))
            .thenThrow(NotFoundException("Book not found: $bookId"))

        // when & then
        mockMvc.perform(
            put("/api/books/$bookId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)

        verify(bookService).updateBook(eq(bookId), eq(request))
    }

    @Test
    fun `PUT books - 異常系 - 出版済みから未出版への変更は400エラー`() {
        // given
        val bookId = 1L
        val request = BookUpdateRequest(
            title = "更新後のタイトル",
            price = 2000,
            status = BookStatus.UNPUBLISHED,
            authorIds = listOf(1L)
        )

        whenever(bookService.updateBook(eq(bookId), eq(request)))
            .thenThrow(BusinessRuleViolationException("Cannot change status from PUBLISHED to UNPUBLISHED"))

        // when & then
        mockMvc.perform(
            put("/api/books/$bookId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(bookService).updateBook(eq(bookId), eq(request))
    }

    @Test
    fun `DELETE books - 正常系 - 書籍を削除できる`() {
        // given
        val bookId = 1L
        doNothing().whenever(bookService).deleteBook(bookId)

        // when & then
        mockMvc.perform(delete("/api/books/$bookId"))
            .andExpect(status().isNoContent)

        verify(bookService).deleteBook(bookId)
    }

    @Test
    fun `DELETE books - 異常系 - 書籍が存在しない場合は404エラー`() {
        // given
        val bookId = 999L
        doThrow(NotFoundException("Book not found: $bookId"))
            .whenever(bookService).deleteBook(bookId)

        // when & then
        mockMvc.perform(delete("/api/books/$bookId"))
            .andExpect(status().isNotFound)

        verify(bookService).deleteBook(bookId)
    }
}

