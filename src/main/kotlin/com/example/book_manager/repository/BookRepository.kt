package com.example.book_manager.repository

import com.example.book_manager.jooq.tables.Books.Companion.BOOKS
import com.example.book_manager.jooq.tables.BooksAuthors.Companion.BOOKS_AUTHORS
import com.example.book_manager.domain.BookStatus
import com.example.book_manager.jooq.tables.records.BooksRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class BookRepository(
    private val dsl: DSLContext
) {

    fun create(title: String, price: Int, status: BookStatus): BooksRecord {
        return dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.STATUS, status.name)
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("Failed to create book record")
    }

    fun update(bookId: Long, title: String, price: Int, status: BookStatus): BooksRecord? {
        return dsl.update(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.STATUS, status.name)
            .set(BOOKS.UPDATED_AT, OffsetDateTime.now())
            .where(BOOKS.ID.eq(bookId))
            .returning()
            .fetchOne()
    }

    fun findById(bookId: Long): BooksRecord? {
        return dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(bookId))
            .fetchOne()
    }

    fun delete(bookId: Long): Int {
        return dsl.deleteFrom(BOOKS)
            .where(BOOKS.ID.eq(bookId))
            .execute()
    }

    fun addAuthors(bookId: Long, authorIds: List<Long>) {
        val batch = authorIds.map { authorId ->
            dsl.insertInto(BOOKS_AUTHORS)
                .set(BOOKS_AUTHORS.BOOK_ID, bookId)
                .set(BOOKS_AUTHORS.AUTHOR_ID, authorId)
        }
        dsl.batch(batch).execute()
    }

    fun removeAllAuthors(bookId: Long) {
        dsl.deleteFrom(BOOKS_AUTHORS)
            .where(BOOKS_AUTHORS.BOOK_ID.eq(bookId))
            .execute()
    }

    fun findAuthorIdsByBookId(bookId: Long): List<Long> {
        return dsl.select(BOOKS_AUTHORS.AUTHOR_ID)
            .from(BOOKS_AUTHORS)
            .where(BOOKS_AUTHORS.BOOK_ID.eq(bookId))
            .fetch(BOOKS_AUTHORS.AUTHOR_ID)
            .mapNotNull { it }
    }

    fun findBooksByAuthorId(authorId: Long, status: BookStatus?): List<BooksRecord> {
        val query = dsl.select(BOOKS.fields().toList())
            .from(BOOKS)
            .join(BOOKS_AUTHORS).on(BOOKS.ID.eq(BOOKS_AUTHORS.BOOK_ID))
            .where(BOOKS_AUTHORS.AUTHOR_ID.eq(authorId))

        if (status != null) {
            query.and(BOOKS.STATUS.eq(status.name))
        }

        return query.fetchInto(BOOKS)
    }
}

