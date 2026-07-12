CREATE DATABASE IF NOT EXISTS quan_ly_ky_tuc_xa
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
USE quan_ly_ky_tuc_xa;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('quanly') DEFAULT 'quanly',
    status ENUM('active','locked') DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tòa nhà
CREATE TABLE IF NOT EXISTS buildings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    building_code VARCHAR(20) NOT NULL UNIQUE,
    building_name VARCHAR(100) NOT NULL,
    gender_type ENUM('Nam','Nữ','Nam/Nữ') DEFAULT 'Nam/Nữ',
    floors INT DEFAULT 1,
    note TEXT,
    status ENUM('Đang hoạt động','Bảo trì','Ngừng hoạt động') DEFAULT 'Đang hoạt động'
);

INSERT IGNORE INTO buildings(building_code, building_name, gender_type, floors, status)
VALUES
('A', 'Tòa A', 'Nam', 5, 'Đang hoạt động'),
('B', 'Tòa B', 'Nữ', 5, 'Đang hoạt động'),
('C', 'Tòa C', 'Nam/Nữ', 6, 'Đang hoạt động'),
('D', 'Tòa D', 'Nam', 4, 'Bảo trì');

-- Phòng
CREATE TABLE IF NOT EXISTS rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_code VARCHAR(20) NOT NULL UNIQUE,
    room_name VARCHAR(100),
    building_id INT,
    room_type VARCHAR(50) NOT NULL DEFAULT 'Tiêu chuẩn',
    floor INT DEFAULT 1,
    capacity INT NOT NULL,
    current_quantity INT DEFAULT 0,
    price DECIMAL(12,2) DEFAULT 0,
    status ENUM('Còn trống','Đã đầy','Bảo trì') NOT NULL DEFAULT 'Còn trống',
    FOREIGN KEY (building_id) REFERENCES buildings(id)
);

INSERT IGNORE INTO rooms(room_code, room_name, building_id, capacity, current_quantity, price, status)
VALUES
('A101','Phòng A101',1,4,4,1200000,'Đã đầy'),
('A102','Phòng A102',1,4,3,1200000,'Còn trống'),
('A103','Phòng A103',1,4,1,1200000,'Còn trống'),
('A104','Phòng A104',1,4,0,1200000,'Còn trống'),

('B201','Phòng B201',2,6,6,1500000,'Đã đầy'),
('B202','Phòng B202',2,6,5,1500000,'Còn trống'),
('B203','Phòng B203',2,6,2,1500000,'Còn trống'),
('B204','Phòng B204',2,6,0,1500000,'Còn trống'),

('C301','Phòng C301',3,8,8,1800000,'Đã đầy'),
('C302','Phòng C302',3,8,7,1800000,'Còn trống'),
('C303','Phòng C303',3,8,0,1800000,'Còn trống');

-- Dịch vụ ký túc xá
CREATE TABLE IF NOT EXISTS services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_code VARCHAR(20) NOT NULL UNIQUE,
    service_name VARCHAR(100) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    note TEXT,
    status ENUM('Đang dùng','Ngưng dùng') NOT NULL DEFAULT 'Đang dùng'
);
-- Sinh viên
CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    gender ENUM('Nam','Nữ') NOT NULL,
    birthday DATE,
    phone VARCHAR(15),
    email VARCHAR(100),
    citizen_id VARCHAR(20),
    address TEXT,
    parent_name VARCHAR(100),
    parent_phone VARCHAR(15),

    faculty VARCHAR(100),
    major VARCHAR(100),
    class_name VARCHAR(50),
    school_year VARCHAR(20),

    room_id INT,
    checkin_date DATE,
    checkout_date DATE,
    status ENUM('Đang ở','Chờ duyệt','Đã trả phòng') DEFAULT 'Chờ duyệt',
    avatar VARCHAR(255),

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- Yêu cầu và lịch sử phản hồi
CREATE TABLE IF NOT EXISTS requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    request_code VARCHAR(20) UNIQUE,
    student_id INT NOT NULL,
    request_type VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    priority ENUM('Cao','Trung bình','Thấp') NOT NULL DEFAULT 'Trung bình',
    status ENUM('Mới tiếp nhận','Đang xử lý','Đã xử lý','Đã đóng') NOT NULL DEFAULT 'Mới tiếp nhận',
    due_date DATE,
    assignee VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS request_responses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    response_content TEXT NOT NULL,
    responder VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_code VARCHAR(20) NOT NULL UNIQUE,
    student_id INT NOT NULL,
    billing_period VARCHAR(7) NOT NULL,
    electricity_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    water_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    service_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status ENUM('Đã thanh toán','Chưa thanh toán','Đang xử lý','Đã hủy','Quá hạn') NOT NULL DEFAULT 'Chưa thanh toán',
    payment_method VARCHAR(50), note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NOT NULL, paid_at DATETIME,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


INSERT IGNORE INTO students
(student_code,full_name,gender,birthday,phone,email,citizen_id,address,
parent_name,parent_phone,faculty,major,class_name,school_year,
room_id,checkin_date,status)
VALUES

('SV001','Nguyễn Văn An','Nam','2004-03-15','0911111111',
'an@gmail.com','079001111111',
'TP Hồ Chí Minh',
'Nguyễn Văn Hùng','0909000001',
'Công nghệ thông tin',
'Kỹ thuật phần mềm',
'DH22CNTT01',
'2022-2026',
1,
'2024-09-01',
'Đang ở'),

('SV002','Trần Thị Mai','Nữ','2004-06-21','0911111112',
'mai@gmail.com','079001111112',
'Đồng Nai',
'Trần Văn Nam','0909000002',
'Quản trị kinh doanh',
'Marketing',
'DH22QTKD01',
'2022-2026',
5,
'2024-09-01',
'Đang ở'),

('SV003','Lê Minh Đức','Nam','2003-12-09','0911111113',
'duc@gmail.com','079001111113',
'Bình Dương',
'Lê Văn Đức','0909000003',
'Điện tử',
'Điện tử',
'DH21DT01',
'2021-2025',
2,
'2024-09-05',
'Đang ở'),

('SV004','Phạm Ngọc Linh','Nữ','2004-08-18','0911111114',
'linh@gmail.com','079001111114',
'Long An',
'Phạm Văn Hải','0909000004',
'Ngôn ngữ Anh',
'Ngôn ngữ Anh',
'DH22NNA01',
'2022-2026',
6,
'2024-09-10',
'Đang ở'),

('SV005','Hoàng Gia Bảo','Nam','2005-01-01','0911111115',
'bao@gmail.com','079001111115',
'Tây Ninh',
'Hoàng Văn Nam','0909000005',
'Công nghệ thông tin',
'Hệ thống thông tin',
'DH23CNTT01',
'2023-2027',
3,
'2024-09-15',
'Đang ở'),

('SV006','Võ Thanh Huy','Nam','2005-03-02','0911111116',
'huy@gmail.com','079001111116',
'Tiền Giang',
'Võ Văn Long','0909000006',
'Kế toán',
'Kế toán',
'DH23KT01',
'2023-2027',
NULL,
NULL,
'Chờ duyệt');

-- Đồng bộ số lượng phòng theo dữ liệu sinh viên hiện có
UPDATE rooms r
LEFT JOIN (
    SELECT room_id, COUNT(*) AS total
    FROM students
    WHERE room_id IS NOT NULL
    AND status = 'Đang ở'
    GROUP BY room_id
) s ON s.room_id = r.id
SET r.current_quantity = COALESCE(s.total, 0);

-- Bổ sung cột cho database đã tồn tại trước lần chuyển đổi Java Web.
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS room_type VARCHAR(50) NOT NULL DEFAULT 'Tiêu chuẩn' AFTER building_id;
UPDATE rooms SET status = CASE
    WHEN status IN ('Đang bảo trì', 'Bảo trì', 'Tạm khóa') THEN 'Bảo trì'
    WHEN current_quantity >= capacity THEN 'Đã đầy'
    ELSE 'Còn trống'
END;
ALTER TABLE rooms MODIFY COLUMN status ENUM('Còn trống','Đã đầy','Bảo trì') NOT NULL DEFAULT 'Còn trống';

-- Giao dịch thu chi dùng bởi FinanceTransactionServlet.
CREATE TABLE IF NOT EXISTS finance_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_code VARCHAR(20) NOT NULL UNIQUE,
    student_id INT NULL,
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
    INDEX idx_finance_date (transaction_date),
    INDEX idx_finance_type (transaction_type),
    INDEX idx_finance_category (category),
    CONSTRAINT fk_finance_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
