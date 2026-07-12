package com.dormitory.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.servlet.ServletContext;

public final class ServiceSchemaMigration {
    private ServiceSchemaMigration() {}

    public static void run(ServletContext context) {
        try (Connection connection = Database.getConnection()) {
            addColumn(connection, "service_type", "VARCHAR(50) NOT NULL DEFAULT 'Khác'");
            addColumn(connection, "icon", "VARCHAR(50) NOT NULL DEFAULT 'fa-box'");
            addColumn(connection, "color", "VARCHAR(20) NOT NULL DEFAULT 'blue'");
            addColumn(connection, "usage_count", "INT NOT NULL DEFAULT 0");
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE services MODIFY status VARCHAR(30) NOT NULL DEFAULT 'Đang hoạt động'");
            }
            seedReferenceServices(connection);
            context.log("Đã đồng bộ dữ liệu quản lý dịch vụ theo giao diện chuẩn");
        } catch (SQLException error) {
            context.log("Không thể đồng bộ schema quản lý dịch vụ", error);
        }
    }

    private static void addColumn(Connection connection, String name, String definition) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet columns = meta.getColumns(connection.getCatalog(), null, "services", name)) {
            if (columns.next()) return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE services ADD COLUMN " + name + " " + definition);
        }
    }

    private static void seedReferenceServices(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS app_schema_migrations (migration_key VARCHAR(100) PRIMARY KEY, applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
        try (PreparedStatement check = connection.prepareStatement("SELECT 1 FROM app_schema_migrations WHERE migration_key='service_reference_data_v1'"); ResultSet result = check.executeQuery()) {
            if (result.next()) return;
        }
        String[][] rows = {
            {"DV001","Internet","Internet","Tháng","120000","Đang hoạt động","Dịch vụ Internet tốc độ cao","fa-wifi","blue","1024"},
            {"DV002","Nước sinh hoạt","Tiện ích","m³","15000","Đang hoạt động","Nước sinh hoạt theo định mức","fa-droplet","cyan","876"},
            {"DV003","Điện sinh hoạt","Tiện ích","kWh","3500","Đang hoạt động","Điện sinh hoạt theo công tơ","fa-bolt","yellow","980"},
            {"DV004","Giặt ủi","Giặt ủi","Lần","20000","Đang hoạt động","Giặt quần áo bằng máy","fa-soap","navy","456"},
            {"DV005","Vệ sinh phòng","Vệ sinh","Lần","25000","Đang hoạt động","Vệ sinh phòng theo yêu cầu","fa-bucket","green","210"},
            {"DV006","Gửi xe","Tiện ích","Tháng","100000","Tạm dừng","Gửi xe máy tại bãi xe KTX","fa-square-parking","blue","180"},
            {"DV007","Máy lạnh","Tiện ích","Tháng","150000","Đang hoạt động","Sử dụng máy lạnh trong phòng","fa-fan","cyan","320"},
            {"DV008","Phòng gym","Thể thao","Tháng","80000","Tạm dừng","Sử dụng phòng gym KTX","fa-dumbbell","purple","145"},
            {"DV009","Thuê tủ đồ","Tiện ích","Tháng","30000","Ngừng cung cấp","Thuê tủ đồ cá nhân","fa-box","orange","80"},
            {"DV010","Bảo hiểm y tế","Y tế","Tháng","45000","Đang hoạt động","Bảo hiểm y tế cho sinh viên","fa-shield-heart","red","275"},
            {"DV011","Sân thể thao","Thể thao","Giờ","50000","Đang hoạt động","Đặt sân thể thao ký túc xá","fa-volleyball","purple","130"},
            {"DV012","In ấn tài liệu","Khác","Trang","1000","Đang hoạt động","In ấn tài liệu dành cho sinh viên","fa-print","navy","250"}
        };
        String sql = "INSERT INTO services(service_code,service_name,service_type,unit,unit_price,status,note,icon,color,usage_count) VALUES(?,?,?,?,?,?,?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE service_name=VALUES(service_name),service_type=VALUES(service_type),unit=VALUES(unit),unit_price=VALUES(unit_price),status=VALUES(status),note=VALUES(note),icon=VALUES(icon),color=VALUES(color),usage_count=VALUES(usage_count)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String[] row : rows) {
                statement.setString(1,row[0]); statement.setString(2,row[1]); statement.setString(3,row[2]); statement.setString(4,row[3]);
                statement.setBigDecimal(5,new java.math.BigDecimal(row[4])); statement.setString(6,row[5]); statement.setString(7,row[6]);
                statement.setString(8,row[7]); statement.setString(9,row[8]); statement.setInt(10,Integer.parseInt(row[9])); statement.addBatch();
            }
            statement.executeBatch();
        }
        try (PreparedStatement mark = connection.prepareStatement("INSERT INTO app_schema_migrations(migration_key) VALUES('service_reference_data_v1')")) {
            mark.executeUpdate();
        }
    }
}
