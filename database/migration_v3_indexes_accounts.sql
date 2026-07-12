-- Migration v3: bo sung rang buoc tai khoan va index phuc vu thong ke.
-- MySQL 8.0+. Co the chay lap lai, khong DROP TABLE/DATABASE va khong sua/xoa du lieu.

USE quan_ly_ky_tuc_xa;

DELIMITER $$

DROP PROCEDURE IF EXISTS migrate_quan_ly_ky_tuc_xa_v3$$
CREATE PROCEDURE migrate_quan_ly_ky_tuc_xa_v3()
BEGIN
    /*
      users.password la cot BCrypt dang duoc LoginServlet, UserDAO va SettingsDAO su dung.
      Khong tao password_hash de tranh hai nguon du lieu mat khau.
    */
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'users'
          AND index_name = 'uq_users_email'
    ) AND NOT EXISTS (
        SELECT 1 FROM users
        WHERE email IS NOT NULL AND TRIM(email) <> ''
        GROUP BY email HAVING COUNT(*) > 1
    ) THEN
        CREATE UNIQUE INDEX uq_users_email ON users(email);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'users'
          AND index_name = 'uq_users_phone'
    ) AND NOT EXISTS (
        SELECT 1 FROM users
        WHERE phone IS NOT NULL AND TRIM(phone) <> ''
        GROUP BY phone HAVING COUNT(*) > 1
    ) THEN
        CREATE UNIQUE INDEX uq_users_phone ON users(phone);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='users' AND index_name='idx_users_created_at') THEN
        CREATE INDEX idx_users_created_at ON users(created_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='rooms' AND index_name='idx_rooms_status') THEN
        CREATE INDEX idx_rooms_status ON rooms(status);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='students' AND index_name='idx_students_created_at') THEN
        CREATE INDEX idx_students_created_at ON students(created_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='requests' AND index_name='idx_requests_created_at') THEN
        CREATE INDEX idx_requests_created_at ON requests(created_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='invoices' AND index_name='idx_invoices_created_at') THEN
        CREATE INDEX idx_invoices_created_at ON invoices(created_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='finance_transactions' AND index_name='idx_finance_created_at') THEN
        CREATE INDEX idx_finance_created_at ON finance_transactions(created_at);
    END IF;

    INSERT IGNORE INTO app_schema_migrations(migration_key)
    VALUES ('migration_v3_indexes_accounts');
END$$

CALL migrate_quan_ly_ky_tuc_xa_v3()$$
DROP PROCEDURE IF EXISTS migrate_quan_ly_ky_tuc_xa_v3$$

DELIMITER ;
