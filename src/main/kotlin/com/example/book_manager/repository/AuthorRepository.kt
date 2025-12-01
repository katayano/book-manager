package com.example.book_manager.repository

import com.example.book_manager.jooq.tables.Authors.Companion.AUTHORS
import com.example.book_manager.jooq.tables.records.AuthorsRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.OffsetDateTime

@Repository
class AuthorRepository(
    private val dsl: DSLContext
) {

    fun create(name: String, birthDate: LocalDate): AuthorsRecord {
        return dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("Failed to create author record")
    }

    fun update(authorId: Long, name: String, birthDate: LocalDate): AuthorsRecord? {
        return dsl.update(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .set(AUTHORS.UPDATED_AT, OffsetDateTime.now())
            .where(AUTHORS.ID.eq(authorId))
            .returning()
            .fetchOne()
    }

    fun findById(authorId: Long): AuthorsRecord? {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(authorId))
            .fetchOne()
    }

    fun delete(authorId: Long): Int {
        return dsl.deleteFrom(AUTHORS)
            .where(AUTHORS.ID.eq(authorId))
            .execute()
    }

    fun existsById(authorId: Long): Boolean {
        return dsl.fetchExists(
            dsl.selectFrom(AUTHORS)
                .where(AUTHORS.ID.eq(authorId))
        )
    }

    fun existsByIds(authorIds: List<Long>): Boolean {
        val count = dsl.selectCount()
            .from(AUTHORS)
            .where(AUTHORS.ID.`in`(authorIds))
            .fetchOne(0, Int::class.java) ?: 0
        return count == authorIds.size
    }
}

