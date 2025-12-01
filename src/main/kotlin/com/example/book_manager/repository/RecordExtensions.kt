package com.example.book_manager.repository

import com.example.book_manager.domain.BookStatus
import com.example.book_manager.dto.AuthorResponse
import com.example.book_manager.dto.BookResponse
import com.example.book_manager.jooq.tables.records.AuthorsRecord
import com.example.book_manager.jooq.tables.records.BooksRecord

/**
 * BooksRecordをBookResponseに変換する拡張関数
 * null安全性を保ちながら、データベースのNOT NULL制約を考慮した変換を行う
 */
fun BooksRecord.toBookResponse(authorIds: List<Long>): BookResponse {
    return BookResponse(
        id = this.id ?: throw IllegalStateException("Book ID is null"),
        title = this.title ?: throw IllegalStateException("Book title is null"),
        price = this.price ?: throw IllegalStateException("Book price is null"),
        status = this.status?.let { BookStatus.valueOf(it) }
            ?: throw IllegalStateException("Book status is null"),
        authorIds = authorIds,
        createdAt = this.createdAt ?: throw IllegalStateException("Book createdAt is null"),
        updatedAt = this.updatedAt ?: throw IllegalStateException("Book updatedAt is null")
    )
}

/**
 * AuthorsRecordをAuthorResponseに変換する拡張関数
 * null安全性を保ちながら、データベースのNOT NULL制約を考慮した変換を行う
 */
fun AuthorsRecord.toAuthorResponse(): AuthorResponse {
    return AuthorResponse(
        id = this.id ?: throw IllegalStateException("Author ID is null"),
        name = this.name ?: throw IllegalStateException("Author name is null"),
        birthDate = this.birthDate ?: throw IllegalStateException("Author birthDate is null"),
        createdAt = this.createdAt ?: throw IllegalStateException("Author createdAt is null"),
        updatedAt = this.updatedAt ?: throw IllegalStateException("Author updatedAt is null")
    )
}

