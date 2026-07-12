package com.dormitory.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;
import util.PasswordUtil;

public final class PasswordHashMigration {
    private PasswordHashMigration() {
    }

    public static void run(ServletContext context) {
        List<LegacyPassword> legacyPasswords = new ArrayList<>();
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement select = connection.prepareStatement("SELECT id, password FROM users");
                     ResultSet resultSet = select.executeQuery()) {
                    while (resultSet.next()) {
                        String storedPassword = resultSet.getString("password");
                        if (!PasswordUtil.isBCryptHash(storedPassword)) {
                            legacyPasswords.add(new LegacyPassword(resultSet.getInt("id"), storedPassword));
                        }
                    }
                }

                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE users SET password = ? WHERE id = ? AND password = ?")) {
                    for (LegacyPassword legacy : legacyPasswords) {
                        update.setString(1, PasswordUtil.hash(legacy.password()));
                        update.setInt(2, legacy.id());
                        update.setString(3, legacy.password());
                        update.addBatch();
                    }
                    if (!legacyPasswords.isEmpty()) {
                        update.executeBatch();
                    }
                }

                try (PreparedStatement seed = connection.prepareStatement("""
                        INSERT IGNORE INTO users(username, password, full_name, role, status)
                        VALUES (?, ?, ?, ?, ?)
                        """)) {
                    seed.setString(1, "quanly");
                    seed.setString(2, PasswordUtil.hash("123456"));
                    seed.setString(3, "Nguyễn Thị Lan");
                    seed.setString(4, "quanly");
                    seed.setString(5, "active");
                    seed.executeUpdate();
                }

                connection.commit();
                context.log("Đã kiểm tra migration BCrypt cho mật khẩu người dùng");
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception exception) {
            context.log("Không thể migration mật khẩu người dùng sang BCrypt", exception);
        }
    }

    private record LegacyPassword(int id, String password) {
    }
}
