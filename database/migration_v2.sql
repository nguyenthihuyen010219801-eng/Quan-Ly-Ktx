-- Migration bổ sung schema cho QuanLyKyTucXa (MySQL 8.0+)
-- An toàn khi chạy lặp lại: không DROP/DELETE và không ghi đè dữ liệu hiện có.

USE quan_ly_ky_tuc_xa;

DELIMITER $$

DROP PROCEDURE IF EXISTS migrate_quan_ly_ky_tuc_xa_v2$$
CREATE PROCEDURE migrate_quan_ly_ky_tuc_xa_v2()
BEGIN
    /* Account: giữ nguyên cột password vì toàn bộ Java/DAO dùng cột này cho BCrypt. */
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'email'
    ) THEN
        ALTER TABLE users ADD COLUMN email VARCHAR(100) NULL AFTER role;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'phone'
    ) THEN
        ALTER TABLE users ADD COLUMN phone VARCHAR(20) NULL AFTER email;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'last_login'
    ) THEN
        ALTER TABLE users ADD COLUMN last_login DATETIME NULL AFTER status;
    END IF;

    /* Settings: đúng các key mà SettingsServlet/SettingsDAO đang sử dụng. */
    CREATE TABLE IF NOT EXISTS system_settings (
        setting_key VARCHAR(100) NOT NULL,
        setting_value TEXT NULL,
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        PRIMARY KEY (setting_key)
    ) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    INSERT IGNORE INTO system_settings (setting_key, setting_value) VALUES
        ('system_name', 'Hệ thống quản lý ký túc xá'),
        ('dormitory_name', 'Ký túc xá'),
        ('address', ''),
        ('email', ''),
        ('phone', ''),
        ('footer', 'Hệ thống quản lý ký túc xá'),
        ('announcement', '');

    CREATE TABLE IF NOT EXISTS app_schema_migrations (
        migration_key VARCHAR(100) NOT NULL,
        applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (migration_key)
    ) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

    /* Account indexes. username đã có UNIQUE KEY từ schema gốc. */
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='users' AND index_name='idx_users_role_status') THEN
        CREATE INDEX idx_users_role_status ON users(role, status);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='users' AND index_name='idx_users_email') THEN
        CREATE INDEX idx_users_email ON users(email);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='users' AND index_name='idx_users_phone') THEN
        CREATE INDEX idx_users_phone ON users(phone);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='users' AND index_name='idx_users_last_login') THEN
        CREATE INDEX idx_users_last_login ON users(last_login);
    END IF;

    /* Building / Room / Student: khóa ngoại đã có trong schema gốc; bổ sung index lọc và join. */
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='buildings' AND index_name='idx_buildings_status') THEN
        CREATE INDEX idx_buildings_status ON buildings(status);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='rooms' AND index_name='idx_rooms_building_status') THEN
        CREATE INDEX idx_rooms_building_status ON rooms(building_id, status);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='students' AND index_name='idx_students_room_status') THEN
        CREATE INDEX idx_students_room_status ON students(room_id, status);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='students' AND index_name='idx_students_status_created') THEN
        CREATE INDEX idx_students_status_created ON students(status, created_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='students' AND index_name='idx_students_checkin_date') THEN
        CREATE INDEX idx_students_checkin_date ON students(checkin_date);
    END IF;

    /* Service. service_code đã có UNIQUE KEY từ schema gốc. */
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='services' AND index_name='idx_services_status') THEN
        CREATE INDEX idx_services_status ON services(status);
    END IF;

    /* Request / response. */
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='requests' AND index_name='idx_requests_student') THEN
        CREATE INDEX idx_requests_student ON requests(student_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='requests' AND index_name='idx_requests_status_created') THEN
        CREATE INDEX idx_requests_status_created ON requests(status, created_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='requests' AND index_name='idx_requests_priority_due') THEN
        CREATE INDEX idx_requests_priority_due ON requests(priority, due_date);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='request_responses' AND index_name='idx_request_responses_request_created') THEN
        CREATE INDEX idx_request_responses_request_created ON request_responses(request_id, created_at);
    END IF;

    /* Invoice / Finance: tối ưu đúng các bộ lọc trong servlet và ReportDAO. */
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='invoices' AND index_name='idx_invoices_student') THEN
        CREATE INDEX idx_invoices_student ON invoices(student_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='invoices' AND index_name='idx_invoices_status_created') THEN
        CREATE INDEX idx_invoices_status_created ON invoices(status, created_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='invoices' AND index_name='idx_invoices_due_date') THEN
        CREATE INDEX idx_invoices_due_date ON invoices(due_date);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='invoices' AND index_name='idx_invoices_paid_at') THEN
        CREATE INDEX idx_invoices_paid_at ON invoices(paid_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='invoices' AND index_name='idx_invoices_billing_period') THEN
        CREATE INDEX idx_invoices_billing_period ON invoices(billing_period);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='finance_transactions' AND index_name='idx_finance_student') THEN
        CREATE INDEX idx_finance_student ON finance_transactions(student_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='finance_transactions' AND index_name='idx_finance_type_date') THEN
        CREATE INDEX idx_finance_type_date ON finance_transactions(transaction_type, transaction_date);
    END IF;

    INSERT IGNORE INTO app_schema_migrations(migration_key) VALUES ('migration_v2');
END$$

CALL migrate_quan_ly_ky_tuc_xa_v2()$$
DROP PROCEDURE IF EXISTS migrate_quan_ly_ky_tuc_xa_v2$$

DELIMITER ;
