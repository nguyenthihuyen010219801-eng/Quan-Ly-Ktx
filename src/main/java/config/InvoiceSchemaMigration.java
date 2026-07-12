package com.dormitory.config;

import jakarta.servlet.ServletContext;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class InvoiceSchemaMigration {
    private InvoiceSchemaMigration() {}

    public static void run(ServletContext context) {
        String sql = """
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
                payment_method VARCHAR(50),
                note TEXT,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                due_date DATE NOT NULL,
                paid_at DATETIME,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT fk_invoices_student FOREIGN KEY (student_id) REFERENCES students(id)
            ) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
            """;
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute(); seed(connection); context.log("Đã chuẩn bị schema hóa đơn");
        } catch (SQLException e) { context.log("Không thể chuẩn bị schema hóa đơn", e); }
    }

    private static void seed(Connection connection) throws SQLException {
        try (PreparedStatement count=connection.prepareStatement("SELECT COUNT(*) FROM invoices");ResultSet rs=count.executeQuery()){rs.next();if(rs.getInt(1)>0)return;}
        List<Integer> students=new ArrayList<>();try(PreparedStatement ps=connection.prepareStatement("SELECT id FROM students WHERE room_id IS NOT NULL ORDER BY student_code");ResultSet rs=ps.executeQuery()){while(rs.next())students.add(rs.getInt(1));}if(students.isEmpty())return;
        String[] codes={"HD0001258","HD0001257","HD0001256","HD0001255","HD0001254","HD0001253","HD0001252","HD0001251","HD0001250","HD0001249"};
        double[][] amounts={{350000,120000,200000},{320000,100000,200000},{450000,150000,250000},{300000,100000,200000},{400000,120000,250000},{380000,110000,200000},{250000,90000,150000},{260000,80000,150000},{210000,70000,100000},{200000,70000,100000}};
        String[] statuses={"Quá hạn","Chưa thanh toán","Quá hạn","Đã thanh toán","Đã thanh toán","Đã thanh toán","Đã thanh toán","Chưa thanh toán","Quá hạn","Đã thanh toán"};
        String insert="INSERT INTO invoices(invoice_code,student_id,billing_period,electricity_amount,water_amount,service_amount,total_amount,status,created_at,due_date,paid_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try(PreparedStatement ps=connection.prepareStatement(insert)){for(int i=0;i<10;i++){LocalDateTime created=LocalDateTime.of(2025,i<5?5:4,20-i%5,9,15);double total=amounts[i][0]+amounts[i][1]+amounts[i][2];ps.setString(1,codes[i]);ps.setInt(2,students.get(i%students.size()));ps.setString(3,i<5?"05/2025":"04/2025");ps.setDouble(4,amounts[i][0]);ps.setDouble(5,amounts[i][1]);ps.setDouble(6,amounts[i][2]);ps.setDouble(7,total);ps.setString(8,statuses[i]);ps.setTimestamp(9,Timestamp.valueOf(created));ps.setDate(10,java.sql.Date.valueOf(created.toLocalDate().plusDays(10)));if("Đã thanh toán".equals(statuses[i]))ps.setTimestamp(11,Timestamp.valueOf(created.plusDays(3)));else ps.setNull(11,Types.TIMESTAMP);ps.addBatch();}ps.executeBatch();}
    }
}
