package com.dormitory.config;

import jakarta.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseInitializer {
    private static final String SCRIPT_PATH = "/sql/quan_ly_ky_tuc_xa.sql";

    private DatabaseInitializer() {
    }

    public static void run(ServletContext context) {
        try {
            String sql = readScript();
            List<String> statements = splitStatements(sql);

            try (Connection connection = Database.getServerConnection()) {
                for (String command : statements) {
                    if (!command.isBlank()) {
                        String seedTable = seedTable(command);
                        if (seedTable != null && tableHasRows(connection, seedTable)) {
                            context.log("Bỏ qua dữ liệu mẫu bảng " + seedTable + " vì bảng đã có dữ liệu");
                            continue;
                        }
                        try (PreparedStatement statement = connection.prepareStatement(command)) {
                            statement.execute();
                        }
                    }
                }
            }

            context.log("Đã tự động nạp database từ " + SCRIPT_PATH);
        } catch (Exception e) {
            context.log("Không tự động nạp được database từ " + SCRIPT_PATH, e);
        }
    }

    private static String readScript() throws Exception {
        InputStream input = DatabaseInitializer.class.getResourceAsStream(SCRIPT_PATH);
        if (input == null) {
            throw new IllegalStateException("Không tìm thấy file SQL: " + SCRIPT_PATH);
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("--")) {
                    builder.append(line).append('\n');
                }
            }
        }
        return builder.toString();
    }

    private static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;

        for (int index = 0; index < sql.length(); index++) {
            char value = sql.charAt(index);
            char previous = index > 0 ? sql.charAt(index - 1) : '\0';

            if (value == '\'' && previous != '\\') {
                inSingleQuote = !inSingleQuote;
            }

            if (value == ';' && !inSingleQuote) {
                statements.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(value);
            }
        }

        String last = current.toString().trim();
        if (!last.isEmpty()) {
            statements.add(last);
        }
        return statements;
    }

    private static String seedTable(String command) {
        String normalized = command.stripLeading().toLowerCase();
        if (normalized.startsWith("insert ignore into users")) {
            return "users";
        }
        if (normalized.startsWith("insert ignore into buildings")) {
            return "buildings";
        }
        if (normalized.startsWith("insert ignore into rooms")) {
            return "rooms";
        }
        if (normalized.startsWith("insert ignore into students")) {
            return "students";
        }
        return null;
    }

    private static boolean tableHasRows(Connection connection, String tableName) {
        String sql = "SELECT COUNT(*) AS total FROM quan_ly_ky_tuc_xa." + tableName;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt("total") > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
