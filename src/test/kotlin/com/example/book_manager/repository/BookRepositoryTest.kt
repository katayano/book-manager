package com.example.book_manager.repository

import com.example.book_manager.domain.BookStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class BookRepositoryTest {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @AfterEach
    fun cleanup() {
        // テストデータのクリーンアップは@Transactionalにより自動的にロールバックされる
    }

    @Test
    fun `create - 書籍を正常に作成できる`() {
        // when
        val result = bookRepository.create(
            title = "テスト書籍",
            price = 1000,
            status = BookStatus.UNPUBLISHED
        )

        // then
        assertNotNull(result.id)
        assertEquals("テスト書籍", result.title)
        assertEquals(1000, result.price)
        assertEquals(BookStatus.UNPUBLISHED.name, result.status)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `update - 書籍を正常に更新できる`() {
        // given
        val created = bookRepository.create(
            title = "元のタイトル",
            price = 1000,
            status = BookStatus.UNPUBLISHED
        )
        val bookId = created.id!!

        // when
        val result = bookRepository.update(
            bookId = bookId,
            title = "更新後のタイトル",
            price = 2000,
            status = BookStatus.PUBLISHED
        )

        // then
        assertNotNull(result)
        assertEquals(bookId, result?.id)
        assertEquals("更新後のタイトル", result?.title)
        assertEquals(2000, result?.price)
        assertEquals(BookStatus.PUBLISHED.name, result?.status)
    }

    @Test
    fun `update - 存在しない書籍の更新はnullを返す`() {
        // when
        val result = bookRepository.update(
            bookId = 999L,
            title = "更新後のタイトル",
            price = 2000,
            status = BookStatus.PUBLISHED
        )

        // then
        assertNull(result)
    }

    @Test
    fun `findById - 指定したIDの書籍を取得できる`() {
        // given
        val created = bookRepository.create(
            title = "テスト書籍",
            price = 1000,
            status = BookStatus.UNPUBLISHED
        )
        val bookId = created.id!!

        // when
        val result = bookRepository.findById(bookId)

        // then
        assertNotNull(result)
        assertEquals(bookId, result?.id)
        assertEquals("テスト書籍", result?.title)
    }

    @Test
    fun `findById - 存在しないIDの場合はnullを返す`() {
        // when
        val result = bookRepository.findById(999L)

        // then
        assertNull(result)
    }

    @Test
    fun `delete - 書籍を正常に削除できる`() {
        // given
        val created = bookRepository.create(
            title = "削除対象書籍",
            price = 1000,
            status = BookStatus.UNPUBLISHED
        )
        val bookId = created.id!!

        // when
        val deletedCount = bookRepository.delete(bookId)

        // then
        assertEquals(1, deletedCount)
        assertNull(bookRepository.findById(bookId))
    }

    @Test
    fun `delete - 存在しない書籍の削除は0を返す`() {
        // when
        val deletedCount = bookRepository.delete(999L)

        // then
        assertEquals(0, deletedCount)
    }

    @Test
    fun `addAuthors - 書籍に著者を紐付けできる`() {
        // given
        val author1 = authorRepository.create("著者1", java.time.LocalDate.now())
        val author2 = authorRepository.create("著者2", java.time.LocalDate.now())
        val book = bookRepository.create("書籍", 1000, BookStatus.UNPUBLISHED)
        val bookId = book.id!!
        val authorIds = listOf(author1.id!!, author2.id!!)

        // when
        bookRepository.addAuthors(bookId, authorIds)

        // then
        val result = bookRepository.findAuthorIdsByBookId(bookId)
        assertEquals(2, result.size)
        assertTrue(result.containsAll(authorIds))
    }

    @Test
    fun `removeAllAuthors - 書籍の著者を全て削除できる`() {
        // given
        val author1 = authorRepository.create("著者1", java.time.LocalDate.now())
        val author2 = authorRepository.create("著者2", java.time.LocalDate.now())
        val book = bookRepository.create("書籍", 1000, BookStatus.UNPUBLISHED)
        val bookId = book.id!!
        bookRepository.addAuthors(bookId, listOf(author1.id!!, author2.id!!))

        // when
        bookRepository.removeAllAuthors(bookId)

        // then
        val result = bookRepository.findAuthorIdsByBookId(bookId)
        assertEquals(0, result.size)
    }

    @Test
    fun `findAuthorIdsByBookId - 書籍に紐づく著者IDのリストを取得できる`() {
        // given
        val author1 = authorRepository.create("著者1", java.time.LocalDate.now())
        val author2 = authorRepository.create("著者2", java.time.LocalDate.now())
        val book = bookRepository.create("書籍", 1000, BookStatus.UNPUBLISHED)
        val bookId = book.id!!
        val authorIds = listOf(author1.id!!, author2.id!!)
        bookRepository.addAuthors(bookId, authorIds)

        // when
        val result = bookRepository.findAuthorIdsByBookId(bookId)

        // then
        assertEquals(2, result.size)
        assertTrue(result.containsAll(authorIds))
    }

    @Test
    fun `findBooksByAuthorId - 著者に紐づく書籍のリストを取得できる`() {
        // given
        val author = authorRepository.create("著者", java.time.LocalDate.now())
        val authorId = author.id!!

        val book1 = bookRepository.create("書籍1", 1000, BookStatus.PUBLISHED)
        val book2 = bookRepository.create("書籍2", 2000, BookStatus.UNPUBLISHED)

        bookRepository.addAuthors(book1.id!!, listOf(authorId))
        bookRepository.addAuthors(book2.id!!, listOf(authorId))

        // when
        val result = bookRepository.findBooksByAuthorId(authorId, null)

        // then
        assertEquals(2, result.size)
        assertTrue(result.any { it.title == "書籍1" })
        assertTrue(result.any { it.title == "書籍2" })
    }

    @Test
    fun `findBooksByAuthorId - ステータスで絞り込みができる`() {
        // given
        val author = authorRepository.create("著者", java.time.LocalDate.now())
        val authorId = author.id!!

        val book1 = bookRepository.create("出版済み書籍", 1000, BookStatus.PUBLISHED)
        val book2 = bookRepository.create("未出版書籍", 2000, BookStatus.UNPUBLISHED)

        bookRepository.addAuthors(book1.id!!, listOf(authorId))
        bookRepository.addAuthors(book2.id!!, listOf(authorId))

        // when
        val result = bookRepository.findBooksByAuthorId(authorId, BookStatus.PUBLISHED)

        // then
        assertEquals(1, result.size)
        assertEquals("出版済み書籍", result[0].title)
        assertEquals(BookStatus.PUBLISHED.name, result[0].status)
    }
}

