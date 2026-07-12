package com.dormitory.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletContext;

public final class AccountSchemaMigration {
    private AccountSchemaMigration() {}

    public static void run(ServletContext context) {
        try (Connection connection = Database.getConnection()) {
            addColumn(connection, "email", "VARCHAR(100) NULL");
            addColumn(connection, "phone", "VARCHAR(20) NULL");
            try (PreparedStatement statement = connection.prepareStatement(
                    "ALTER TABLE users MODIFY role ENUM('quanly','nhanvien') NOT NULL DEFAULT 'nhanvien'")) {
                statement.executeUpdate();
            }
            context.log("Đã chuẩn bị schema quản lý tài khoản");
        } catch (Exception exception) {
            context.log("Không thể chuẩn bị schema quản lý tài khoản", exception);
        }
    }

    private static void addColumn(Connection connection, String name, String definition) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, "users", name)) {
            if (columns.next()) return;
        }
        try (PreparedStatement statement = connection.prepareStatement("ALTER TABLE users ADD COLUMN " + name + " " + definition)) {
            statement.executeUpdate();
        }
    }
}
