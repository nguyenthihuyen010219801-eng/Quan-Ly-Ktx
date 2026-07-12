# Quản lý ký túc xá

## 1. Thông tin nhóm

- Thành viên 1: Hồ Ngọc Anh Thư - 2305CT0697
- Thành viên 2: Lê Nguyễn Minh Ngọc -2305CT0530
- Thành viên 3: Lê Nguyễn Ngọc Hân -2305CT2030
- Thành viên 4: Quách Thị Như Ý- 2305CT0571

## 2. Mô tả đề tài

Website hỗ trợ quản lý các công việc trong ký túc xá. Hệ thống được sử dụng bởi quản lý và nhân viên để theo dõi sinh viên, tòa nhà, phòng, dịch vụ, hóa đơn và các yêu cầu của sinh viên.

Ngoài các chức năng quản lý dữ liệu, hệ thống còn có phần thu chi, báo cáo thống kê, quản lý tài khoản và cài đặt chung.

## 3. Công nghệ sử dụng

- Java 17
- JSP
- Jakarta Servlet 6.0
- JDBC
- HTML
- CSS
- JavaScript
- Maven
- Apache Tomcat 10.1.55
- MySQL 8.0 trở lên
- Gson 2.11.0
- jBCrypt 0.4

## 4. Các chức năng chính

- Đăng nhập, đăng xuất và phân quyền quản lý, nhân viên
- Xem tổng quan
- Quản lý sinh viên
- Quản lý phòng
- Quản lý tòa nhà
- Quản lý hóa đơn
- Quản lý dịch vụ
- Quản lý yêu cầu và phản hồi
- Quản lý thu chi
- Báo cáo và thống kê
- Quản lý tài khoản
- Cài đặt hệ thống

## 5. Hướng dẫn cài đặt

1. Clone hoặc giải nén project, sau đó mở thư mục chứa file `pom.xml` bằng IDE.

2. Cài JDK 17 trở lên, Maven, MySQL 8.0 trở lên và Apache Tomcat 10.1.55. File `run.bat` hiện dùng JDK 23 để build mã nguồn theo Java 17.

3. Mở MySQL Workbench và chạy toàn bộ file `database/FullDatabase.sql`. File này tạo database `quan_ly_ky_tuc_xa`, các bảng và dữ liệu ban đầu.

4. Cấu hình kết nối tại `src/main/java/com/dormitory/config/Database.java`. Giá trị mặc định là MySQL tại `localhost:3306`, tài khoản `root`. Có thể cấu hình bằng các biến môi trường `DB_SERVER_URL`, `DB_URL`, `DB_USER` và `DB_PASSWORD`.

5. Build project tại thư mục chứa `pom.xml` bằng lệnh:

```text
mvn clean package
```

C:\netbeans\java\maven\bin\mvn.cmd clean package

Hoặc:

run.bat

Sau đó truy cập:

http://localhost:8080/QuanLyKyTucXa/

6. Sau khi build, lấy file `target/QuanLyKyTucXa.war` và chép vào thư mục `webapps` của Apache Tomcat.

7. Khởi động Tomcat rồi truy cập:

```text
http://localhost:8080/QuanLyKyTucXa/
```

Có thể sửa `JAVA_HOME`, `MAVEN_HOME` và `CATALINA_HOME` trong `run.bat`, sau đó chạy file này để build và deploy project.

## 6. Tài khoản demo

Quản lý:

Username: quanly

Password: 123456

## 7. Video thuyết trình và demo

Video demo:

Video thuyết trình:
