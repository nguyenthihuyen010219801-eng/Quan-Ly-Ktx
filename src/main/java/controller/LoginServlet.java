package com.dormitory.controller;

import com.dormitory.config.Database;
import com.dormitory.util.PasswordUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@WebServlet("/api/login")
public class LoginServlet extends BaseServlet {
    private static final String INVALID_CREDENTIALS = "Tên đăng nhập hoặc mật khẩu không đúng";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> body = readJson(request);
        String username = text(body, "username");
        String password = text(body, "password");

        String sql = """
                SELECT id, username, password, full_name, role, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()
                        || !"active".equals(resultSet.getString("status"))
                        || !PasswordUtil.verify(password, resultSet.getString("password"))) {
                    writeJson(response, jsonMap("success", false, "message", INVALID_CREDENTIALS));
                    return;
                }

                Map<String, Object> user = jsonMap(
                        "id", resultSet.getInt("id"),
                        "username", resultSet.getString("username"),
                        "full_name", resultSet.getString("full_name"),
                        "role", resultSet.getString("role"),
                        "status", resultSet.getString("status")
                );
                HttpSession session = request.getSession(true);
                session.setAttribute("accountUserId", resultSet.getInt("id"));
                session.setAttribute("accountUsername", resultSet.getString("username"));
                session.setAttribute("accountFullName", resultSet.getString("full_name"));
                session.setAttribute("accountRole", resultSet.getString("role"));
                session.setMaxInactiveInterval(30 * 60);
                try (PreparedStatement updateLogin = connection.prepareStatement("UPDATE users SET last_login=CURRENT_TIMESTAMP WHERE id=?")) {
                    updateLogin.setInt(1, resultSet.getInt("id"));
                    updateLogin.executeUpdate();
                }
                writeJson(response, jsonMap(
                        "success", true,
                        "message", "Đăng nhập thành công",
                        "user", user
                ));
            }
        } catch (Exception exception) {
            writeJson(response, 500, jsonMap("success", false, "message", "Lỗi server"));
        }
    }
}
