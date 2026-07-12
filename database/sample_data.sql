USE quan_ly_ky_tuc_xa;

INSERT IGNORE INTO buildings (building_code, building_name, gender_type, floors, status, note) VALUES
('A', 'Tòa nhà A', 'Nam/Nữ', 5, 'Đang hoạt động', 'Dữ liệu mẫu'),
('B', 'Tòa nhà B', 'Nam/Nữ', 5, 'Đang hoạt động', 'Dữ liệu mẫu');

INSERT IGNORE INTO rooms (room_code, room_name, building_id, room_type, floor, capacity, current_quantity, price, status)
SELECT 'A101', 'Phòng A101', id, 'Tiêu chuẩn', 1, 4, 2, 1200000, 'Còn trống' FROM buildings WHERE building_code='A';
INSERT IGNORE INTO rooms (room_code, room_name, building_id, room_type, floor, capacity, current_quantity, price, status)
SELECT 'A102', 'Phòng A102', id, 'Tiêu chuẩn', 1, 4, 0, 1200000, 'Còn trống' FROM buildings WHERE building_code='A';
INSERT IGNORE INTO rooms (room_code, room_name, building_id, room_type, floor, capacity, current_quantity, price, status)
SELECT 'B101', 'Phòng B101', id, 'Máy lạnh', 1, 6, 1, 1500000, 'Còn trống' FROM buildings WHERE building_code='B';

INSERT IGNORE INTO students(student_code,full_name,gender,phone,email,room_id,checkin_date,status)
SELECT 'SVMAU01','Nguyễn Văn An','Nam','0901000001','an@example.com',id,CURDATE(),'Đang ở' FROM rooms WHERE room_code='A101';
INSERT IGNORE INTO students(student_code,full_name,gender,phone,email,room_id,checkin_date,status)
SELECT 'SVMAU02','Trần Thị Bình','Nữ','0901000002','binh@example.com',id,CURDATE(),'Đang ở' FROM rooms WHERE room_code='A101';
INSERT IGNORE INTO students(student_code,full_name,gender,phone,email,room_id,checkin_date,status)
SELECT 'SVMAU03','Lê Minh Châu','Nam','0901000003','chau@example.com',id,CURDATE(),'Đang ở' FROM rooms WHERE room_code='B101';

INSERT IGNORE INTO services(service_code,service_name,unit,unit_price,note,status) VALUES
('DV001','Điện','kWh',3500,'Tính theo đồng hồ','Đang dùng'),
('DV002','Nước','m³',12000,'Tính theo đồng hồ','Đang dùng'),
('DV003','Giữ xe','Tháng',100000,'Phí giữ xe máy','Đang dùng'),
('DV004','Internet','Tháng',50000,'Internet ký túc xá','Đang dùng');
