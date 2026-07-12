package com.dormitory.controller;

import com.dormitory.config.Database;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@WebServlet("/api/manage/dashboard")
public class DashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String statsSql = """
                SELECT
                    (SELECT COUNT(*) FROM students) AS totalStudents,
                    (SELECT COUNT(*) FROM students WHERE status = 'Đang ở') AS activeStudents,
                    (SELECT COUNT(*) FROM students WHERE status = 'Chờ duyệt') AS pendingStudents,
                    (SELECT COUNT(*) FROM rooms WHERE status = 'Đã đầy') AS usedRooms,
                    (SELECT COUNT(*) FROM rooms WHERE current_quantity < capacity AND status != 'Bảo trì') AS emptyRooms,
                    (SELECT COUNT(*) FROM rooms WHERE status = 'Bảo trì') AS maintenanceRooms,
                    (SELECT COUNT(*) FROM students WHERE status = 'Đang ở') AS unpaidInvoices,
                    (SELECT COUNT(*) FROM students WHERE status = 'Chờ duyệt') AS newFeedback,
                    0 AS violationStudents,
                    (SELECT COUNT(*) FROM students WHERE phone IS NULL OR phone = '' OR email IS NULL OR email = '' OR room_id IS NULL) AS incompleteProfiles,
                    (SELECT COALESCE(SUM(r.price), 0)
                     FROM students s
                     LEFT JOIN rooms r ON s.room_id = r.id
                     WHERE s.status = 'Đang ở') AS revenueThisMonth,
                    (SELECT COALESCE(SUM(GREATEST(capacity - current_quantity, 0)), 0) FROM rooms) AS availableBeds
                """;
        String buildingSql = """
                SELECT COALESCE(b.building_name, 'Chưa phân tòa') AS building_name, COUNT(s.id) AS total
                FROM students s
                LEFT JOIN rooms r ON s.room_id = r.id
                LEFT JOIN buildings b ON r.building_id = b.id
                GROUP BY COALESCE(b.building_name, 'Chưa phân tòa')
                ORDER BY total DESC
                """;
        String roomStatusSql = """
                SELECT status, COUNT(*) AS total
                FROM rooms
                GROUP BY status
                ORDER BY total DESC
                """;
        String monthlyRevenueSql = """
                SELECT MONTH(COALESCE(s.checkin_date, s.created_at)) AS month,
                       COALESCE(SUM(r.price), 0) AS revenue
                FROM students s
                LEFT JOIN rooms r ON s.room_id = r.id
                WHERE YEAR(COALESCE(s.checkin_date, s.created_at)) = YEAR(CURDATE())
                GROUP BY MONTH(COALESCE(s.checkin_date, s.created_at))
                ORDER BY month
                """;
        String recentSql = studentSelectSql("") + " ORDER BY s.created_at DESC, s.id DESC LIMIT 4";
        String invoiceSql = """
                SELECT s.id, s.student_code, s.full_name, s.created_at, r.room_code, COALESCE(r.price, 0) AS amount
                FROM students s
                LEFT JOIN rooms r ON s.room_id = r.id
                WHERE s.status = 'Đang ở'
                ORDER BY s.id DESC
                LIMIT 4
                """;
        String repairSql = """
                SELECT r.id, r.room_code, r.status, b.building_name,
                       CASE
                           WHEN r.status = 'Đang bảo trì' THEN 'Phòng đang trong quá trình bảo trì'
                           WHEN r.current_quantity >= r.capacity THEN 'Phòng đã đầy, cần kiểm tra sức chứa'
                           ELSE 'Kiểm tra định kỳ cơ sở vật chất'
                       END AS note
                FROM rooms r
                LEFT JOIN buildings b ON r.building_id = b.id
                ORDER BY (r.status = 'Đang bảo trì') DESC, r.id DESC
                LIMIT 4
                """;
        String feedbackSql = """
                SELECT s.id, s.student_code, s.full_name, s.created_at, r.room_code,
                       CASE
                           WHEN s.status = 'Chờ duyệt' THEN 'Mong được duyệt hồ sơ nhận phòng'
                           WHEN s.room_id IS NULL THEN 'Cần hỗ trợ sắp xếp phòng ở'
                           ELSE 'Cập nhật thông tin sinh viên mới nhất'
                       END AS message
                FROM students s
                LEFT JOIN rooms r ON s.room_id = r.id
                ORDER BY (s.status = 'Chờ duyệt') DESC, s.updated_at DESC, s.id DESC
                LIMIT 4
                """;

        try (Connection connection = Database.getConnection()) {
            Map<String, Object> stats = firstRow(connection, statsSql);
            List<Map<String, Object>> byBuilding = queryRows(connection, buildingSql);
            List<Map<String, Object>> roomStatus = queryRows(connection, roomStatusSql);
            List<Map<String, Object>> monthlyRevenue = queryRows(connection, monthlyRevenueSql);
            List<Map<String, Object>> recentStudents = queryRows(connection, recentSql);
            List<Map<String, Object>> recentInvoices = queryRows(connection, invoiceSql);
            List<Map<String, Object>> recentRepairs = queryRows(connection, repairSql);
            List<Map<String, Object>> recentFeedback = queryRows(connection, feedbackSql);

            boolean manager = "quanly".equals(request.getSession(false).getAttribute("accountRole"));
            if (!manager) {
                stats.remove("revenueThisMonth");
                monthlyRevenue = List.of();
            }

            writeJson(response, jsonMap(
                    "stats", stats,
                    "byBuilding", byBuilding,
                    "roomStatus", roomStatus,
                    "monthlyRevenue", monthlyRevenue,
                    "recentStudents", recentStudents,
                    "recentInvoices", recentInvoices,
                    "recentRepairs", recentRepairs,
                    "recentFeedback", recentFeedback
            ));
        } catch (Exception e) {
            writeJson(response, 500, jsonMap("message", "Không tải được tổng quan"));
        }
    }

    private Map<String, Object> firstRow(Connection connection, String sql) throws Exception {
        List<Map<String, Object>> data = queryRows(connection, sql);
        return data.isEmpty() ? jsonMap() : data.get(0);
    }

    private List<Map<String, Object>> queryRows(Connection connection, String sql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return rows(resultSet);
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
}
