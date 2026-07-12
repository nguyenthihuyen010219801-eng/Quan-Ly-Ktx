-- QuanLyKyTucXa - Full database for MySQL 8.0+
-- Standalone script: run directly in MySQL Workbench with UTF-8 encoding.
-- Safe to run repeatedly: no DROP TABLE, DROP DATABASE, or DELETE.

CREATE DATABASE IF NOT EXISTS quan_ly_ky_tuc_xa
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE quan_ly_ky_tuc_xa;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
  id INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  role ENUM('quanly','nhanvien') NOT NULL DEFAULT 'nhanvien',
  status ENUM('active','locked') DEFAULT 'active',
  last_login DATETIME DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  email VARCHAR(100) DEFAULT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY username (username),
  UNIQUE KEY uq_users_email (email),
  UNIQUE KEY uq_users_phone (phone),
  KEY idx_users_role_status (role,status),
  KEY idx_users_email (email),
  KEY idx_users_phone (phone),
  KEY idx_users_last_login (last_login),
  KEY idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS buildings (
  id INT NOT NULL AUTO_INCREMENT,
  building_code VARCHAR(20) NOT NULL,
  building_name VARCHAR(100) NOT NULL,
  gender_type ENUM('Nam','Nữ','Nam/Nữ') DEFAULT 'Nam/Nữ',
  floors INT DEFAULT 1,
  note TEXT,
  status ENUM('Đang hoạt động','Bảo trì','Ngừng hoạt động') DEFAULT 'Đang hoạt động',
  PRIMARY KEY (id),
  UNIQUE KEY building_code (building_code),
  KEY idx_buildings_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS rooms (
  id INT NOT NULL AUTO_INCREMENT,
  room_code VARCHAR(20) NOT NULL,
  room_name VARCHAR(100) DEFAULT NULL,
  building_id INT DEFAULT NULL,
  room_type VARCHAR(50) NOT NULL DEFAULT 'Tiêu chuẩn',
  floor INT DEFAULT 1,
  capacity INT NOT NULL,
  current_quantity INT DEFAULT 0,
  price DECIMAL(12,2) DEFAULT 0.00,
  status ENUM('Còn trống','Đã đầy','Bảo trì') NOT NULL DEFAULT 'Còn trống',
  PRIMARY KEY (id),
  UNIQUE KEY room_code (room_code),
  KEY idx_rooms_building_status (building_id,status),
  KEY idx_rooms_status (status),
  CONSTRAINT rooms_ibfk_1 FOREIGN KEY (building_id) REFERENCES buildings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS students (
  id INT NOT NULL AUTO_INCREMENT,
  student_code VARCHAR(30) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  gender ENUM('Nam','Nữ') NOT NULL,
  birthday DATE DEFAULT NULL,
  phone VARCHAR(15) DEFAULT NULL,
  email VARCHAR(100) DEFAULT NULL,
  citizen_id VARCHAR(20) DEFAULT NULL,
  address TEXT,
  parent_name VARCHAR(100) DEFAULT NULL,
  parent_phone VARCHAR(15) DEFAULT NULL,
  faculty VARCHAR(100) DEFAULT NULL,
  major VARCHAR(100) DEFAULT NULL,
  class_name VARCHAR(50) DEFAULT NULL,
  school_year VARCHAR(20) DEFAULT NULL,
  room_id INT DEFAULT NULL,
  checkin_date DATE DEFAULT NULL,
  checkout_date DATE DEFAULT NULL,
  status ENUM('Đang ở','Chờ duyệt','Đã trả phòng') DEFAULT 'Chờ duyệt',
  avatar VARCHAR(255) DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY student_code (student_code),
  KEY idx_students_room_status (room_id,status),
  KEY idx_students_status_created (status,created_at),
  KEY idx_students_checkin_date (checkin_date),
  KEY idx_students_created_at (created_at),
  CONSTRAINT students_ibfk_1 FOREIGN KEY (room_id) REFERENCES rooms(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS services (
  id INT NOT NULL AUTO_INCREMENT,
  service_code VARCHAR(20) NOT NULL,
  service_name VARCHAR(100) NOT NULL,
  unit VARCHAR(30) NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  note TEXT,
  status VARCHAR(30) NOT NULL DEFAULT 'Đang hoạt động',
  service_type VARCHAR(50) NOT NULL DEFAULT 'Khác',
  icon VARCHAR(50) NOT NULL DEFAULT 'fa-box',
  color VARCHAR(20) NOT NULL DEFAULT 'blue',
  usage_count INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY service_code (service_code),
  KEY idx_services_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS requests (
  id INT NOT NULL AUTO_INCREMENT,
  request_code VARCHAR(20) DEFAULT NULL,
  student_id INT NOT NULL,
  request_type VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  priority ENUM('Cao','Trung bình','Thấp') NOT NULL DEFAULT 'Trung bình',
  status ENUM('Mới tiếp nhận','Đang xử lý','Đã xử lý','Đã đóng') NOT NULL DEFAULT 'Mới tiếp nhận',
  due_date DATE DEFAULT NULL,
  assignee VARCHAR(100) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY request_code (request_code),
  KEY idx_requests_student (student_id),
  KEY idx_requests_status_created (status,created_at),
  KEY idx_requests_priority_due (priority,due_date),
  KEY idx_requests_created_at (created_at),
  CONSTRAINT requests_ibfk_1 FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS request_responses (
  id INT NOT NULL AUTO_INCREMENT,
  request_id INT NOT NULL,
  response_content TEXT NOT NULL,
  responder VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_request_responses_request_created (request_id,created_at),
  CONSTRAINT request_responses_ibfk_1 FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoices (
  id INT NOT NULL AUTO_INCREMENT,
  invoice_code VARCHAR(20) NOT NULL,
  student_id INT NOT NULL,
  billing_period VARCHAR(7) NOT NULL,
  electricity_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  water_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  service_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  status ENUM('Đã thanh toán','Chưa thanh toán','Đang xử lý','Đã hủy','Quá hạn') NOT NULL DEFAULT 'Chưa thanh toán',
  payment_method VARCHAR(50) DEFAULT NULL,
  note TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  due_date DATE NOT NULL,
  paid_at DATETIME DEFAULT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY invoice_code (invoice_code),
  KEY idx_invoices_student (student_id),
  KEY idx_invoices_status_created (status,created_at),
  KEY idx_invoices_due_date (due_date),
  KEY idx_invoices_paid_at (paid_at),
  KEY idx_invoices_billing_period (billing_period),
  KEY idx_invoices_created_at (created_at),
  CONSTRAINT invoices_ibfk_1 FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS finance_transactions (
  id INT NOT NULL AUTO_INCREMENT,
  transaction_code VARCHAR(20) NOT NULL,
  student_id INT DEFAULT NULL,
  transaction_date DATETIME NOT NULL,
  content VARCHAR(500) NOT NULL,
  transaction_type ENUM('Thu','Chi') NOT NULL,
  category VARCHAR(100) NOT NULL,
  amount DECIMAL(14,2) NOT NULL,
  payment_method VARCHAR(50) NOT NULL,
  performed_by VARCHAR(100) NOT NULL,
  note TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY transaction_code (transaction_code),
  KEY idx_finance_date (transaction_date),
  KEY idx_finance_type (transaction_type),
  KEY idx_finance_category (category),
  KEY idx_finance_student (student_id),
  KEY idx_finance_type_date (transaction_type,transaction_date),
  KEY idx_finance_created_at (created_at),
  CONSTRAINT fk_finance_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS system_settings (
  setting_key VARCHAR(100) NOT NULL,
  setting_value TEXT,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_schema_migrations (
  migration_key VARCHAR(100) NOT NULL,
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (migration_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Compatibility ALTER statements for databases created by an older project version.
-- MySQL 8 does not support ADD COLUMN IF NOT EXISTS, therefore metadata is checked first.
DELIMITER $$
DROP PROCEDURE IF EXISTS full_database_compatibility$$
CREATE PROCEDURE full_database_compatibility()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='users' AND column_name='email') THEN
    ALTER TABLE users ADD COLUMN email VARCHAR(100) DEFAULT NULL;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='users' AND column_name='phone') THEN
    ALTER TABLE users ADD COLUMN phone VARCHAR(20) DEFAULT NULL;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='users' AND column_name='last_login') THEN
    ALTER TABLE users ADD COLUMN last_login DATETIME DEFAULT NULL;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='rooms' AND column_name='room_type') THEN
    ALTER TABLE rooms ADD COLUMN room_type VARCHAR(50) NOT NULL DEFAULT 'Tiêu chuẩn' AFTER building_id;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='services' AND column_name='service_type') THEN
    ALTER TABLE services ADD COLUMN service_type VARCHAR(50) NOT NULL DEFAULT 'Khác';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='services' AND column_name='icon') THEN
    ALTER TABLE services ADD COLUMN icon VARCHAR(50) NOT NULL DEFAULT 'fa-box';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='services' AND column_name='color') THEN
    ALTER TABLE services ADD COLUMN color VARCHAR(20) NOT NULL DEFAULT 'blue';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='services' AND column_name='usage_count') THEN
    ALTER TABLE services ADD COLUMN usage_count INT NOT NULL DEFAULT 0;
  END IF;
  ALTER TABLE users MODIFY COLUMN role ENUM('quanly','nhanvien') NOT NULL DEFAULT 'nhanvien';
  ALTER TABLE rooms MODIFY COLUMN status ENUM('Còn trống','Đã đầy','Bảo trì') NOT NULL DEFAULT 'Còn trống';
  ALTER TABLE services MODIFY COLUMN status VARCHAR(30) NOT NULL DEFAULT 'Đang hoạt động';
END$$
CALL full_database_compatibility()$$
DROP PROCEDURE IF EXISTS full_database_compatibility$$
DELIMITER ;

-- Default manager. Password is BCrypt for the project default password: 123456.
INSERT IGNORE INTO users (username,password,full_name,role,status)
VALUES ('quanly','$2a$12$NtQm/tyZs1WyiaW7Sj6lGOaFggwk3rsbXnxNf6qk2Rk706mZXki1K','Nguyễn Thị Lan','quanly','active');

INSERT IGNORE INTO buildings (building_code,building_name,gender_type,floors,status) VALUES
('A','Tòa A','Nam',5,'Đang hoạt động'),
('B','Tòa B','Nữ',5,'Đang hoạt động'),
('C','Tòa C','Nam/Nữ',6,'Đang hoạt động'),
('D','Tòa D','Nam',4,'Bảo trì');

INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'A101','Phòng A101',id,'Tiêu chuẩn',1,4,4,1200000,'Đã đầy' FROM buildings WHERE building_code='A';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'A102','Phòng A102',id,'Tiêu chuẩn',1,4,3,1200000,'Còn trống' FROM buildings WHERE building_code='A';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'A103','Phòng A103',id,'Tiêu chuẩn',1,4,1,1200000,'Còn trống' FROM buildings WHERE building_code='A';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'A104','Phòng A104',id,'Tiêu chuẩn',1,4,0,1200000,'Còn trống' FROM buildings WHERE building_code='A';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'B201','Phòng B201',id,'Tiêu chuẩn',2,6,6,1500000,'Đã đầy' FROM buildings WHERE building_code='B';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'B202','Phòng B202',id,'Tiêu chuẩn',2,6,5,1500000,'Còn trống' FROM buildings WHERE building_code='B';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'B203','Phòng B203',id,'Tiêu chuẩn',2,6,2,1500000,'Còn trống' FROM buildings WHERE building_code='B';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'B204','Phòng B204',id,'Tiêu chuẩn',2,6,0,1500000,'Còn trống' FROM buildings WHERE building_code='B';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'C301','Phòng C301',id,'Tiêu chuẩn',3,8,8,1800000,'Đã đầy' FROM buildings WHERE building_code='C';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'C302','Phòng C302',id,'Tiêu chuẩn',3,8,7,1800000,'Còn trống' FROM buildings WHERE building_code='C';
INSERT IGNORE INTO rooms (room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status)
SELECT 'C303','Phòng C303',id,'Tiêu chuẩn',3,8,0,1800000,'Còn trống' FROM buildings WHERE building_code='C';

INSERT IGNORE INTO students
(student_code,full_name,gender,birthday,phone,email,citizen_id,address,parent_name,parent_phone,faculty,major,class_name,school_year,room_id,checkin_date,status)
SELECT 'SV001','Nguyễn Văn An','Nam','2004-03-15','0911111111','an@gmail.com','079001111111','TP Hồ Chí Minh','Nguyễn Văn Hùng','0909000001','Công nghệ thông tin','Kỹ thuật phần mềm','DH22CNTT01','2022-2026',id,'2024-09-01','Đang ở' FROM rooms WHERE room_code='A101';
INSERT IGNORE INTO students
(student_code,full_name,gender,birthday,phone,email,citizen_id,address,parent_name,parent_phone,faculty,major,class_name,school_year,room_id,checkin_date,status)
SELECT 'SV002','Trần Thị Mai','Nữ','2004-06-21','0911111112','mai@gmail.com','079001111112','Đồng Nai','Trần Văn Nam','0909000002','Quản trị kinh doanh','Marketing','DH22QTKD01','2022-2026',id,'2024-09-01','Đang ở' FROM rooms WHERE room_code='B201';
INSERT IGNORE INTO students
(student_code,full_name,gender,birthday,phone,email,citizen_id,address,parent_name,parent_phone,faculty,major,class_name,school_year,room_id,checkin_date,status)
SELECT 'SV003','Lê Minh Đức','Nam','2003-12-09','0911111113','duc@gmail.com','079001111113','Bình Dương','Lê Văn Đức','0909000003','Điện tử','Điện tử','DH21DT01','2021-2025',id,'2024-09-05','Đang ở' FROM rooms WHERE room_code='A102';
INSERT IGNORE INTO students
(student_code,full_name,gender,birthday,phone,email,citizen_id,address,parent_name,parent_phone,faculty,major,class_name,school_year,room_id,checkin_date,status)
SELECT 'SV004','Phạm Ngọc Linh','Nữ','2004-08-18','0911111114','linh@gmail.com','079001111114','Long An','Phạm Văn Hải','0909000004','Ngôn ngữ Anh','Ngôn ngữ Anh','DH22NNA01','2022-2026',id,'2024-09-10','Đang ở' FROM rooms WHERE room_code='B202';
INSERT IGNORE INTO students
(student_code,full_name,gender,birthday,phone,email,citizen_id,address,parent_name,parent_phone,faculty,major,class_name,school_year,room_id,checkin_date,status)
SELECT 'SV005','Hoàng Gia Bảo','Nam','2005-01-01','0911111115','bao@gmail.com','079001111115','Tây Ninh','Hoàng Văn Nam','0909000005','Công nghệ thông tin','Hệ thống thông tin','DH23CNTT01','2023-2027',id,'2024-09-15','Đang ở' FROM rooms WHERE room_code='A103';
INSERT IGNORE INTO students
(student_code,full_name,gender,birthday,phone,email,citizen_id,address,parent_name,parent_phone,faculty,major,class_name,school_year,room_id,checkin_date,status)
VALUES ('SV006','Võ Thanh Huy','Nam','2005-03-02','0911111116','huy@gmail.com','079001111116','Tiền Giang','Võ Văn Long','0909000006','Kế toán','Kế toán','DH23KT01','2023-2027',NULL,NULL,'Chờ duyệt');

INSERT IGNORE INTO services
(service_code,service_name,service_type,unit,unit_price,status,note,icon,color,usage_count) VALUES
('DV001','Internet','Internet','Tháng',120000,'Đang hoạt động','Dịch vụ Internet tốc độ cao','fa-wifi','blue',1024),
('DV002','Nước sinh hoạt','Tiện ích','m³',15000,'Đang hoạt động','Nước sinh hoạt theo định mức','fa-droplet','cyan',876),
('DV003','Điện sinh hoạt','Tiện ích','kWh',3500,'Đang hoạt động','Điện sinh hoạt theo công tơ','fa-bolt','yellow',980),
('DV004','Giặt ủi','Giặt ủi','Lần',20000,'Đang hoạt động','Giặt quần áo bằng máy','fa-soap','navy',456),
('DV005','Vệ sinh phòng','Vệ sinh','Lần',25000,'Đang hoạt động','Vệ sinh phòng theo yêu cầu','fa-bucket','green',210),
('DV006','Gửi xe','Tiện ích','Tháng',100000,'Tạm dừng','Gửi xe máy tại bãi xe KTX','fa-square-parking','blue',180),
('DV007','Máy lạnh','Tiện ích','Tháng',150000,'Đang hoạt động','Sử dụng máy lạnh trong phòng','fa-fan','cyan',320),
('DV008','Phòng gym','Thể thao','Tháng',80000,'Tạm dừng','Sử dụng phòng gym KTX','fa-dumbbell','purple',145),
('DV009','Thuê tủ đồ','Tiện ích','Tháng',30000,'Ngừng cung cấp','Thuê tủ đồ cá nhân','fa-box','orange',80),
('DV010','Bảo hiểm y tế','Y tế','Tháng',45000,'Đang hoạt động','Bảo hiểm y tế cho sinh viên','fa-shield-heart','red',275),
('DV011','Sân thể thao','Thể thao','Giờ',50000,'Đang hoạt động','Đặt sân thể thao ký túc xá','fa-volleyball','purple',130),
('DV012','In ấn tài liệu','Khác','Trang',1000,'Đang hoạt động','In ấn tài liệu dành cho sinh viên','fa-print','navy',250);

INSERT IGNORE INTO system_settings (setting_key,setting_value) VALUES
('system_name','Hệ thống quản lý ký túc xá'),
('dormitory_name','Ký túc xá'),
('address',''),
('email',''),
('phone',''),
('footer','Hệ thống quản lý ký túc xá'),
('announcement','');

INSERT IGNORE INTO app_schema_migrations (migration_key) VALUES
('service_reference_data_v1'),
('migration_v2'),
('migration_v3_indexes_accounts'),
('full_database_v1');

CREATE OR REPLACE VIEW vw_dashboard AS
SELECT
  (SELECT COUNT(*) FROM students) AS total_students,
  (SELECT COUNT(*) FROM rooms) AS total_rooms,
  (SELECT COUNT(*) FROM rooms WHERE current_quantity < capacity AND status <> 'Bảo trì') AS available_rooms,
  (SELECT COUNT(*) FROM invoices WHERE status IN ('Chưa thanh toán','Quá hạn')) AS unpaid_invoices,
  (SELECT COALESCE(SUM(amount),0) FROM finance_transactions WHERE transaction_type='Thu') AS total_revenue,
  (SELECT COUNT(*) FROM requests WHERE status IN ('Mới tiếp nhận','Đang xử lý')) AS pending_requests;

CREATE OR REPLACE VIEW vw_room_statistics AS
SELECT b.id AS building_id,b.building_code,b.building_name,
       COUNT(r.id) AS total_rooms,
       COALESCE(SUM(r.capacity),0) AS total_capacity,
       COALESCE(SUM(r.current_quantity),0) AS occupied_slots,
       COALESCE(SUM(r.capacity-r.current_quantity),0) AS available_slots
FROM buildings b
LEFT JOIN rooms r ON r.building_id=b.id
GROUP BY b.id,b.building_code,b.building_name;

CREATE OR REPLACE VIEW vw_account_statistics AS
SELECT role,status,COUNT(*) AS total_accounts,
       MAX(last_login) AS latest_login
FROM users
GROUP BY role,status;

CREATE OR REPLACE VIEW vw_report AS
SELECT DATE_FORMAT(f.transaction_date,'%Y-%m') AS report_month,
       SUM(CASE WHEN f.transaction_type='Thu' THEN f.amount ELSE 0 END) AS total_income,
       SUM(CASE WHEN f.transaction_type='Chi' THEN f.amount ELSE 0 END) AS total_expense,
       SUM(CASE WHEN f.transaction_type='Thu' THEN f.amount ELSE -f.amount END) AS balance
FROM finance_transactions f
GROUP BY DATE_FORMAT(f.transaction_date,'%Y-%m');
