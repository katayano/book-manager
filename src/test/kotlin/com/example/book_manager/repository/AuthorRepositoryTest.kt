package com.example.book_manager.repository

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class AuthorRepositoryTest {

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @AfterEach
    fun cleanup() {
        // テストデータのクリーンアップは@Transactionalにより自動的にロールバックされる
    }

    @Test
    fun `create - 著者を正常に作成できる`() {
        // when
        val result = authorRepository.create(
            name = "山田太郎",
            birthDate = LocalDate.of(1980, 1, 1)
        )

        // then
        assertNotNull(result.id)
        assertEquals("山田太郎", result.name)
        assertEquals(LocalDate.of(1980, 1, 1), result.birthDate)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `update - 著者を正常に更新できる`() {
        // given
        val created = authorRepository.create(
            name = "山田太郎",
            birthDate = LocalDate.of(1980, 1, 1)
        )
        val authorId = created.id!!

        // when
        val result = authorRepository.update(
            authorId = authorId,
            name = "鈴木花子",
            birthDate = LocalDate.of(1990, 5, 15)
        )

        // then
        assertNotNull(result)
        assertEquals(authorId, result?.id)
        assertEquals("鈴木花子", result?.name)
        assertEquals(LocalDate.of(1990, 5, 15), result?.birthDate)
    }

    @Test
    fun `update - 存在しない著者の更新はnullを返す`() {
        // when
        val result = authorRepository.update(
            authorId = 999L,
            name = "鈴木花子",
            birthDate = LocalDate.of(1990, 5, 15)
        )

        // then
        assertNull(result)
    }

    @Test
    fun `findById - 指定したIDの著者を取得できる`() {
        // given
        val created = authorRepository.create(
            name = "山田太郎",
            birthDate = LocalDate.of(1980, 1, 1)
        )
        val authorId = created.id!!

        // when
        val result = authorRepository.findById(authorId)

        // then
        assertNotNull(result)
        assertEquals(authorId, result?.id)
        assertEquals("山田太郎", result?.name)
        assertEquals(LocalDate.of(1980, 1, 1), result?.birthDate)
    }

    @Test
    fun `findById - 存在しないIDの場合はnullを返す`() {
        // when
        val result = authorRepository.findById(999L)

        // then
        assertNull(result)
    }

    @Test
    fun `delete - 著者を正常に削除できる`() {
        // given
        val created = authorRepository.create(
            name = "削除対象著者",
            birthDate = LocalDate.of(1980, 1, 1)
        )
        val authorId = created.id!!

        // when
        val deletedCount = authorRepository.delete(authorId)

        // then
        assertEquals(1, deletedCount)
        assertNull(authorRepository.findById(authorId))
    }

    @Test
    fun `delete - 存在しない著者の削除は0を返す`() {
        // when
        val deletedCount = authorRepository.delete(999L)

        // then
        assertEquals(0, deletedCount)
    }

    @Test
    fun `existsById - 著者が存在する場合はtrueを返す`() {
        // given
        val created = authorRepository.create(
            name = "山田太郎",
            birthDate = LocalDate.of(1980, 1, 1)
        )
        val authorId = created.id!!

        // when
        val result = authorRepository.existsById(authorId)

        // then
        assertTrue(result)
    }

    @Test
    fun `existsById - 著者が存在しない場合はfalseを返す`() {
        // when
        val result = authorRepository.existsById(999L)

        // then
        assertFalse(result)
    }

    @Test
    fun `existsByIds - 全ての著者が存在する場合はtrueを返す`() {
        // given
        val author1 = authorRepository.create("著者1", LocalDate.now())
        val author2 = authorRepository.create("著者2", LocalDate.now())
        val authorIds = listOf(author1.id!!, author2.id!!)

        // when
        val result = authorRepository.existsByIds(authorIds)

        // then
        assertTrue(result)
    }

    @Test
    fun `existsByIds - 一部の著者が存在しない場合はfalseを返す`() {
        // given
        val author1 = authorRepository.create("著者1", LocalDate.now())
        val authorIds = listOf(author1.id!!, 999L)

        // when
        val result = authorRepository.existsByIds(authorIds)

        // then
        assertFalse(result)
    }

    @Test
    fun `existsByIds - 全ての著者が存在しない場合はfalseを返す`() {
        // when
        val result = authorRepository.existsByIds(listOf(998L, 999L))

        // then
        assertFalse(result)
    }

    @Test
    fun `existsByIds - 空リストの場合はfalseを返す`() {
        // when
        val result = authorRepository.existsByIds(emptyList())

        // then
        assertFalse(result)
    }
}

