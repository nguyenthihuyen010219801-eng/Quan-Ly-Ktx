package com.dormitory.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {
    protected static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    protected void writeJson(HttpServletResponse response, Object body) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(GSON.toJson(body));
    }

    protected void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        writeJson(response, body);
    }

    protected Map<String, Object> readJson(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding("UTF-8");
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        if (body.length() == 0) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> data = GSON.fromJson(body.toString(), MAP_TYPE);
        return data == null ? new LinkedHashMap<>() : data;
    }

    protected Map<String, Object> jsonMap(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index + 1 < entries.length; index += 2) {
            map.put(String.valueOf(entries[index]), entries[index + 1]);
        }
        return map;
    }

    protected List<Map<String, Object>> rows(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        while (resultSet.next()) {
            rows.add(row(resultSet));
        }
        return rows;
    }

    protected Map<String, Object> row(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Map<String, Object> row = new LinkedHashMap<>();
        for (int column = 1; column <= metaData.getColumnCount(); column++) {
            row.put(metaData.getColumnLabel(column), normalizeValue(resultSet.getObject(column)));
        }
        return row;
    }

    protected void setParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int index = 0; index < params.size(); index++) {
            statement.setObject(index + 1, params.get(index));
        }
    }

    protected String text(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    protected Integer integer(Map<String, Object> data, String key) {
        Object rawValue = data.get(key);
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Number) {
            return ((Number) rawValue).intValue();
        }

        String value = String.valueOf(rawValue).trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.contains(".")) {
            return BigDecimal.valueOf(Double.parseDouble(value)).intValue();
        }
        return Integer.valueOf(value);
    }

    protected Date sqlDate(Map<String, Object> data, String key) {
        String value = text(data, key);
        if (value == null) {
            return null;
        }
        return Date.valueOf(parseDate(value));
    }

    private LocalDate parseDate(String value) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported browser/user-entered date format.
            }
        }

        throw new IllegalArgumentException("Ngày không hợp lệ: " + value);
    }

    private Object normalizeValue(Object value) {
        if (value instanceof Date) {
            return value.toString();
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime().toString();
        }
        if (value instanceof LocalDate || value instanceof LocalDateTime || value instanceof LocalTime) {
            return value.toString();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        return value;
    }
}
