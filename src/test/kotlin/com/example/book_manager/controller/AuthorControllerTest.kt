package com.example.book_manager.controller

import com.example.book_manager.domain.BookStatus
import com.example.book_manager.dto.AuthorCreateRequest
import com.example.book_manager.dto.AuthorResponse
import com.example.book_manager.dto.AuthorUpdateRequest
import com.example.book_manager.dto.BookResponse
import com.example.book_manager.exception.NotFoundException
import com.example.book_manager.service.AuthorService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
class AuthorControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())

    @MockitoBean
    private lateinit var authorService: AuthorService

    @Test
    fun `POST authors - 正常系 - 著者を作成できる`() {
        // given
        val request = AuthorCreateRequest(
            name = "山田太郎",
            birthDate = LocalDate.of(1980, 1, 1)
        )

        val response = AuthorResponse(
            id = 1L,
            name = request.name,
            birthDate = request.birthDate,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )

        whenever(authorService.createAuthor(request)).thenReturn(response)

        // when & then
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("山田太郎"))
            .andExpect(jsonPath("$.birthDate").value("1980-01-01"))

        verify(authorService).createAuthor(request)
    }

    @Test
    fun `POST authors - 異常系 - 名前が空の場合はバリデーションエラー`() {
        // given
        val request = mapOf(
            "name" to "",
            "birthDate" to "1980-01-01"
        )

        // when & then
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(authorService, never()).createAuthor(any())
    }

    @Test
    fun `POST authors - 異常系 - 生年月日が未来の日付の場合はバリデーションエラー`() {
        // given
        val request = mapOf(
            "name" to "山田太郎",
            "birthDate" to LocalDate.now().plusDays(1).toString()
        )

        // when & then
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)

        verify(authorService, never()).createAuthor(any())
    }

    @Test
    fun `PUT authors - 正常系 - 著者を更新できる`() {
        // given
        val authorId = 1L
        val request = AuthorUpdateRequest(
            name = "鈴木花子",
            birthDate = LocalDate.of(1990, 5, 15)
        )

        val response = AuthorResponse(
            id = authorId,
            name = request.name,
            birthDate = request.birthDate,
            createdAt = OffsetDateTime.now().minusDays(10),
            updatedAt = OffsetDateTime.now()
        )

        whenever(authorService.updateAuthor(eq(authorId), eq(request))).thenReturn(response)

        // when & then
        mockMvc.perform(
            put("/api/authors/$authorId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("鈴木花子"))
            .andExpect(jsonPath("$.birthDate").value("1990-05-15"))

        verify(authorService).updateAuthor(eq(authorId), eq(request))
    }

    @Test
    fun `PUT authors - 異常系 - 著者が存在しない場合は404エラー`() {
        // given
        val authorId = 999L
        val request = AuthorUpdateRequest(
            name = "鈴木花子",
            birthDate = LocalDate.of(1990, 5, 15)
        )

        whenever(authorService.updateAuthor(eq(authorId), eq(request)))
            .thenThrow(NotFoundException("Author not found: $authorId"))

        // when & then
        mockMvc.perform(
            put("/api/authors/$authorId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)

        verify(authorService).updateAuthor(eq(authorId), eq(request))
    }

    @Test
    fun `DELETE authors - 正常系 - 著者を削除できる`() {
        // given
        val authorId = 1L
        doNothing().whenever(authorService).deleteAuthor(authorId)

        // when & then
        mockMvc.perform(delete("/api/authors/$authorId"))
            .andExpect(status().isNoContent)

        verify(authorService).deleteAuthor(authorId)
    }

    @Test
    fun `DELETE authors - 異常系 - 著者が存在しない場合は404エラー`() {
        // given
        val authorId = 999L
        doThrow(NotFoundException("Author not found: $authorId"))
            .whenever(authorService).deleteAuthor(authorId)

        // when & then
        mockMvc.perform(delete("/api/authors/$authorId"))
            .andExpect(status().isNotFound)

        verify(authorService).deleteAuthor(authorId)
    }

    @Test
    fun `GET authors - authorId - books - 正常系 - 著者の書籍一覧を取得できる`() {
        // given
        val authorId = 1L
        val books = listOf(
            BookResponse(
                id = 1L,
                title = "書籍1",
                price = 1000,
                status = BookStatus.PUBLISHED,
                authorIds = listOf(authorId),
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            ),
            BookResponse(
                id = 2L,
                title = "書籍2",
                price = 2000,
                status = BookStatus.UNPUBLISHED,
                authorIds = listOf(authorId),
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            )
        )

        whenever(authorService.findBooksByAuthor(authorId, null)).thenReturn(books)

        // when & then
        mockMvc.perform(get("/api/authors/$authorId/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("書籍1"))
            .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
            .andExpect(jsonPath("$[1].title").value("書籍2"))
            .andExpect(jsonPath("$[1].status").value("UNPUBLISHED"))

        verify(authorService).findBooksByAuthor(authorId, null)
    }

    @Test
    fun `GET authors - authorId - books - 正常系 - ステータス指定で著者の書籍を取得できる`() {
        // given
        val authorId = 1L
        val status = BookStatus.PUBLISHED
        val books = listOf(
            BookResponse(
                id = 1L,
                title = "出版済み書籍",
                price = 1000,
                status = BookStatus.PUBLISHED,
                authorIds = listOf(authorId),
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            )
        )

        whenever(authorService.findBooksByAuthor(authorId, status)).thenReturn(books)

        // when & then
        mockMvc.perform(get("/api/authors/$authorId/books?status=PUBLISHED"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("出版済み書籍"))
            .andExpect(jsonPath("$[0].status").value("PUBLISHED"))

        verify(authorService).findBooksByAuthor(authorId, status)
    }

    @Test
    fun `GET authors - authorId - books - 正常系 - 書籍が0件の場合は空配列を返す`() {
        // given
        val authorId = 1L
        whenever(authorService.findBooksByAuthor(authorId, null)).thenReturn(emptyList<BookResponse>())

        // when & then
        mockMvc.perform(get("/api/authors/$authorId/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))

        verify(authorService).findBooksByAuthor(authorId, null)
    }

    @Test
    fun `GET authors - authorId - books - 異常系 - 著者が存在しない場合は404エラー`() {
        // given
        val authorId = 999L
        whenever(authorService.findBooksByAuthor(authorId, null))
            .thenThrow(NotFoundException("Author not found: $authorId"))

        // when & then
        mockMvc.perform(get("/api/authors/$authorId/books"))
            .andExpect(status().isNotFound)

        verify(authorService).findBooksByAuthor(authorId, null)
    }
}

