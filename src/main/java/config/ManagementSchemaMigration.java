package com.dormitory.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;

public final class ManagementSchemaMigration {
    private ManagementSchemaMigration() {}

    public static void run(ServletContext context) {
        try (Connection connection = Database.getConnection()) {
            if (!hasColumn(connection, "rooms", "room_type")) {
                execute(connection, "ALTER TABLE rooms ADD COLUMN room_type VARCHAR(50) NOT NULL DEFAULT 'Tiêu chuẩn' AFTER building_id");
            }
            execute(connection, "ALTER TABLE rooms MODIFY COLUMN status VARCHAR(30) NOT NULL DEFAULT 'Còn trống'");
            execute(connection, "UPDATE rooms SET status=CASE WHEN status IN ('Đang bảo trì','Bảo trì','Tạm khóa') THEN 'Bảo trì' WHEN current_quantity>=capacity THEN 'Đã đầy' ELSE 'Còn trống' END");
            execute(connection, "ALTER TABLE rooms MODIFY COLUMN status ENUM('Còn trống','Đã đầy','Bảo trì') NOT NULL DEFAULT 'Còn trống'");
            context.log("Đã chuẩn bị schema cho phòng, tòa nhà và dịch vụ");
        } catch (SQLException e) {
            context.log("Không thể chuẩn bị schema cho ba module quản lý", e);
        }
    }

    private static boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), null, table, column)) {
            return columns.next();
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
}
