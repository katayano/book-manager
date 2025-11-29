CREATE TABLE authors (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255)        NOT NULL,
    birth_date  DATE                NOT NULL CHECK (birth_date <= CURRENT_DATE),
    created_at  TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE TABLE books (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    price       INTEGER      NOT NULL CHECK (price >= 0),
    status      VARCHAR(32)  NOT NULL CHECK (status IN ('UNPUBLISHED', 'PUBLISHED')),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE books_authors (
    book_id     BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    author_id   BIGINT NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

CREATE INDEX idx_books_authors_author_id ON books_authors(author_id);

