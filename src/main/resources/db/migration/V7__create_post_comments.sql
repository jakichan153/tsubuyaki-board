-- =========================================================================
-- 社内つぶやきボード V7: POST_COMMENTS テーブル
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE post_comments_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE post_comments (
    id          NUMBER(19)         NOT NULL,
    post_id     NUMBER(19)         NOT NULL,
    author      VARCHAR2(30 CHAR)  NOT NULL,
    body        VARCHAR2(280 CHAR) NOT NULL,
    created_at  TIMESTAMP(6)       NOT NULL,
    CONSTRAINT post_comments_pk PRIMARY KEY (id),
    CONSTRAINT post_comments_post_fk FOREIGN KEY (post_id) REFERENCES posts (id)
);

CREATE INDEX post_comments_post_created_idx ON post_comments (post_id, created_at);
