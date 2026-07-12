package com.dormitory.config;

import jakarta.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class FinanceSchemaMigration {
    private FinanceSchemaMigration() {}

    public static void run(ServletContext context) {
        String sql = """
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
            ) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
            """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
            seed(connection);
            context.log("Đã chuẩn bị schema quản lý thu chi");
        } catch (SQLException e) {
            context.log("Không thể chuẩn bị schema quản lý thu chi", e);
        }
    }

    private static void seed(Connection connection) throws SQLException {
        try (PreparedStatement count = connection.prepareStatement("SELECT COUNT(*) FROM finance_transactions");
             ResultSet result = count.executeQuery()) {
            result.next();
            if (result.getInt(1) > 0) return;
        }
        List<Integer> students = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM students ORDER BY student_code LIMIT 10");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) students.add(result.getInt(1));
        }
        String[] contents = {"Thu tiền phòng 5 sinh viên phòng A101", "Chi tiền điện tháng 5", "Thu tiền đặt cọc phòng B205", "Chi mua vật tư vệ sinh", "Thu tiền nước tháng 5", "Chi sửa chữa phòng A203", "Thu tiền phòng 3 sinh viên phòng C301", "Chi phí internet tháng 5", "Thu tiền dịch vụ giặt là", "Chi mua bóng đèn thay thế"};
        String[] types = {"Thu", "Chi", "Thu", "Chi", "Thu", "Chi", "Thu", "Chi", "Thu", "Chi"};
        String[] categories = {"Tiền phòng", "Điện", "Đặt cọc", "Vệ sinh", "Nước", "Sửa chữa", "Tiền phòng", "Internet", "Dịch vụ", "Sửa chữa"};
        long[] amounts = {5500000, 12500000, 2000000, 1250000, 8750000, 3800000, 3300000, 2200000, 1450000, 650000};
        String[] methods = {"Tiền mặt", "Chuyển khoản", "Tiền mặt", "Tiền mặt", "Chuyển khoản", "Chuyển khoản", "Tiền mặt", "Chuyển khoản", "Tiền mặt", "Tiền mặt"};
        String[] performers = {"Nguyễn Thị Lan", "Trần Văn Minh", "Nguyễn Thị Lan", "Phạm Thị Hương", "Nguyễn Thị Lan", "Trần Văn Minh", "Nguyễn Thị Lan", "Trần Văn Minh", "Nguyễn Thị Lan", "Phạm Thị Hương"};
        LocalDateTime[] dates = {LocalDateTime.of(2025,5,23,10,30),LocalDateTime.of(2025,5,23,9,15),LocalDateTime.of(2025,5,22,16,45),LocalDateTime.of(2025,5,22,11,20),LocalDateTime.of(2025,5,21,14,10),LocalDateTime.of(2025,5,21,10,5),LocalDateTime.of(2025,5,20,9,30),LocalDateTime.of(2025,5,19,15,0),LocalDateTime.of(2025,5,19,11,30),LocalDateTime.of(2025,5,18,10,20)};
        String insert = "INSERT INTO finance_transactions(transaction_code,student_id,transaction_date,content,transaction_type,category,amount,payment_method,performed_by) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            for (int i = 0; i < contents.length; i++) {
                statement.setString(1, "GD" + String.format("%07d", 56 - i));
                if (students.isEmpty()) statement.setNull(2, java.sql.Types.INTEGER); else statement.setInt(2, students.get(i % students.size()));
                statement.setTimestamp(3, Timestamp.valueOf(dates[i]));
                statement.setString(4, contents[i]); statement.setString(5, types[i]); statement.setString(6, categories[i]);
                statement.setLong(7, amounts[i]); statement.setString(8, methods[i]); statement.setString(9, performers[i]); statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
