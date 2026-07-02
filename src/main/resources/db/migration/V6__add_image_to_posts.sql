-- =========================================================================
-- 社内つぶやきボード V6: POSTS テーブルに添付画像を追加
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

ALTER TABLE posts ADD image_content_type VARCHAR2(100 CHAR);
ALTER TABLE posts ADD image_data BLOB;
