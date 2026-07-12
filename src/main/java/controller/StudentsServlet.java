package com.dormitory.controller;

import com.dormitory.config.Database;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/manage/students", "/api/manage/students/*"})
public class StudentsServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = getPathId(request);
        if (id == null) {
            listStudents(request, response);
        } else {
            getStudent(id, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> body = readJson(request);
        StudentPayload data = StudentPayload.from(body, this);

        if (!data.isValid()) {
            writeJson(response, 400, jsonMap(
                    "success", false,
                    "message", "Vui lòng nhập mã sinh viên, họ tên và giới tính"
            ));
            return;
        }

        String sql = """
                INSERT INTO students (
                    student_code, full_name, gender, birthday, phone, email, citizen_id, address,
                    parent_name, parent_phone, faculty, major, class_name, school_year,
                    room_id, checkin_date, checkout_date, status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStudentStatement(statement, data);
            statement.executeUpdate();
            syncRoomQuantities(connection);

            try (ResultSet keys = statement.getGeneratedKeys()) {
                Integer newId = keys.next() ? keys.getInt(1) : null;
                writeJson(response, jsonMap(
                        "success", true,
                        "id", newId,
                        "message", "Thêm sinh viên thành công"
                ));
            }
        } catch (SQLException e) {
            writeJson(response, 500, jsonMap(
                    "success", false,
                    "message", isDuplicateError(e)
                            ? "Mã sinh viên đã tồn tại"
                            : "Không thêm được sinh viên"
            ));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = getPathId(request);
        if (id == null) {
            writeJson(response, 404, jsonMap("success", false, "message", "Không tìm thấy sinh viên"));
            return;
        }

        Map<String, Object> body = readJson(request);
        StudentPayload data = StudentPayload.from(body, this);

        if (!data.isValid()) {
            writeJson(response, 400, jsonMap(
                    "success", false,
                    "message", "Vui lòng nhập mã sinh viên, họ tên và giới tính"
            ));
            return;
        }

        String existingCode;
        try (Connection connection = Database.getConnection()) {
            existingCode = findStudentCode(connection, id);
        } catch (SQLException e) {
            writeJson(response, 500, jsonMap("success", false, "message", "Không tải được sinh viên cần sửa"));
            return;
        }

        if (existingCode == null) {
            writeJson(response, 404, jsonMap("success", false, "message", "Không tìm thấy sinh viên cần sửa"));
            return;
        }
        data.studentCode = existingCode;

        String sql = """
                UPDATE students
                SET student_code = ?, full_name = ?, gender = ?, birthday = ?, phone = ?, email = ?,
                    citizen_id = ?, address = ?, parent_name = ?, parent_phone = ?, faculty = ?,
                    major = ?, class_name = ?, school_year = ?, room_id = ?, checkin_date = ?,
                    checkout_date = ?, status = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStudentStatement(statement, data);
            statement.setInt(19, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                writeJson(response, 404, jsonMap("success", false, "message", "Không tìm thấy sinh viên cần sửa"));
                return;
            }
            syncRoomQuantities(connection);

            writeJson(response, jsonMap(
                    "success", true,
                    "message", "Cập nhật sinh viên thành công"
            ));
        } catch (SQLException e) {
            writeJson(response, 500, jsonMap(
                    "success", false,
                    "message", isDuplicateError(e)
                            ? "Mã sinh viên đã tồn tại"
                            : "Không cập nhật được sinh viên"
            ));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = getPathId(request);
        if (id == null) {
            writeJson(response, 404, jsonMap("success", false, "message", "Không tìm thấy sinh viên"));
            return;
        }

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                writeJson(response, 404, jsonMap("success", false, "message", "Không tìm thấy sinh viên cần xóa"));
                return;
            }
            syncRoomQuantities(connection);

            writeJson(response, jsonMap(
                    "success", true,
                    "message", "Xóa sinh viên thành công"
            ));
        } catch (SQLException e) {
            writeJson(response, 500, jsonMap("success", false, "message", "Không xóa được sinh viên"));
        }
    }

    private void listStudents(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = Math.max(parseInt(request.getParameter("page"), 1), 1);
        int limit = Math.min(Math.max(parseInt(request.getParameter("limit"), 5), 1), 50);
        int offset = (page - 1) * limit;

        List<String> where = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        String search = request.getParameter("search");
        if (search != null && !search.isBlank()) {
            where.add("""
                    (
                        s.student_code LIKE ?
                        OR s.full_name LIKE ?
                        OR s.phone LIKE ?
                        OR s.email LIKE ?
                        OR r.room_code LIKE ?
                    )
                    """);
            String keyword = "%" + search.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        addFilter(where, params, "b.id = ?", request.getParameter("building_id"));
        addFilter(where, params, "s.room_id = ?", request.getParameter("room_id"));
        addFilter(where, params, "s.gender = ?", request.getParameter("gender"));
        addFilter(where, params, "s.status = ?", request.getParameter("status"));

        String whereSql = where.isEmpty() ? "" : "WHERE " + String.join(" AND ", where);
        String countSql = """
                SELECT COUNT(*) AS total
                FROM students s
                LEFT JOIN rooms r ON s.room_id = r.id
                LEFT JOIN buildings b ON r.building_id = b.id
                """ + whereSql;
        String listSql = studentSelectSql(whereSql) + " ORDER BY s.student_code ASC, s.id ASC LIMIT ? OFFSET ?";

        try (Connection connection = Database.getConnection()) {
            int total;
            try (PreparedStatement countStatement = connection.prepareStatement(countSql)) {
                setParams(countStatement, params);
                try (ResultSet countRows = countStatement.executeQuery()) {
                    countRows.next();
                    total = countRows.getInt("total");
                }
            }

            List<Object> listParams = new ArrayList<>(params);
            listParams.add(limit);
            listParams.add(offset);
            List<Map<String, Object>> students;
            try (PreparedStatement listStatement = connection.prepareStatement(listSql)) {
                setParams(listStatement, listParams);
                try (ResultSet resultSet = listStatement.executeQuery()) {
                    students = rows(resultSet);
                }
            }

            writeJson(response, jsonMap(
                    "students", students,
                    "total", total,
                    "page", page,
                    "limit", limit
            ));
        } catch (Exception e) {
            writeJson(response, 500, jsonMap("message", "Không tải được danh sách sinh viên"));
        }
    }

    private void getStudent(Integer id, HttpServletResponse response) throws IOException {
        String sql = studentSelectSql("WHERE s.id = ?") + " LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    writeJson(response, 404, jsonMap("message", "Không tìm thấy sinh viên"));
                    return;
                }
                writeJson(response, row(resultSet));
            }
        } catch (Exception e) {
            writeJson(response, 500, jsonMap("message", "Không tải được sinh viên"));
        }
    }

    private String studentSelectSql(String extraWhere) {
        return """
                SELECT
                    s.*,
                    r.room_code,
                    r.room_name,
                    b.id AS building_id,
                    b.building_code,
                    b.building_name
                FROM students s
                LEFT JOIN rooms r ON s.room_id = r.id
                LEFT JOIN buildings b ON r.building_id = b.id
                """ + extraWhere;
    }

    private void fillStudentStatement(PreparedStatement statement, StudentPayload data) throws SQLException {
        statement.setString(1, data.studentCode);
        statement.setString(2, data.fullName);
        statement.setString(3, data.gender);
        statement.setDate(4, data.birthday);
        statement.setString(5, data.phone);
        statement.setString(6, data.email);
        statement.setString(7, data.citizenId);
        statement.setString(8, data.address);
        statement.setString(9, data.parentName);
        statement.setString(10, data.parentPhone);
        statement.setString(11, data.faculty);
        statement.setString(12, data.major);
        statement.setString(13, data.className);
        statement.setString(14, data.schoolYear);
        if (data.roomId == null) {
            statement.setNull(15, java.sql.Types.INTEGER);
        } else {
            statement.setInt(15, data.roomId);
        }
        statement.setDate(16, data.checkinDate);
        statement.setDate(17, data.checkoutDate);
        statement.setString(18, data.status);
    }

    private void syncRoomQuantities(Connection connection) throws SQLException {
        String sql = """
                UPDATE rooms r
                LEFT JOIN (
                    SELECT room_id, COUNT(*) AS total
                    FROM students
                    WHERE room_id IS NOT NULL
                    AND status = 'Đang ở'
                    GROUP BY room_id
                ) s ON s.room_id = r.id
                SET r.current_quantity = COALESCE(s.total, 0)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private String findStudentCode(Connection connection, Integer id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT student_code FROM students WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("student_code") : null;
            }
        }
    }

    private void addFilter(List<String> where, List<Object> params, String clause, String value) {
        if (value != null && !value.isBlank()) {
            where.add(clause);
            params.add(value.trim());
        }
    }

    private boolean isDuplicateError(SQLException e) {
        String state = e.getSQLState();
        String message = e.getMessage();
        return e.getErrorCode() == 1062
                || "23000".equals(state)
                || (message != null && message.toLowerCase().contains("duplicate"));
    }

    private Integer getPathId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static class StudentPayload {
        private String studentCode;
        private String fullName;
        private String gender;
        private Date birthday;
        private String phone;
        private String email;
        private String citizenId;
        private String address;
        private String parentName;
        private String parentPhone;
        private String faculty;
        private String major;
        private String className;
        private String schoolYear;
        private Integer roomId;
        private Date checkinDate;
        private Date checkoutDate;
        private String status;

        private static StudentPayload from(Map<String, Object> body, StudentsServlet servlet) {
            StudentPayload data = new StudentPayload();
            data.studentCode = servlet.text(body, "student_code");
            data.fullName = servlet.text(body, "full_name");
            data.gender = servlet.text(body, "gender");
            data.birthday = servlet.sqlDate(body, "birthday");
            data.phone = servlet.text(body, "phone");
            data.email = servlet.text(body, "email");
            data.citizenId = servlet.text(body, "citizen_id");
            data.address = servlet.text(body, "address");
            data.parentName = servlet.text(body, "parent_name");
            data.parentPhone = servlet.text(body, "parent_phone");
            data.faculty = servlet.text(body, "faculty");
            data.major = servlet.text(body, "major");
            data.className = servlet.text(body, "class_name");
            data.schoolYear = servlet.text(body, "school_year");
            data.roomId = servlet.integer(body, "room_id");
            data.checkinDate = servlet.sqlDate(body, "checkin_date");
            data.checkoutDate = servlet.sqlDate(body, "checkout_date");
            data.status = servlet.text(body, "status");
            if (data.status == null) {
                data.status = "Chờ duyệt";
            }
            return data;
        }

        private boolean isValid() {
            return studentCode != null && fullName != null && gender != null;
        }
    }
}
