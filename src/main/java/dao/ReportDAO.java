package dao;

import config.Database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO extends DaoSupport {
    public Map<String, Object> getReport(LocalDate from, LocalDate to) throws SQLException {
        Map<String, Object> report = new LinkedHashMap<>();
        try (Connection connection = Database.getConnection()) {
            report.put("summary", summary(connection, from, to));
            report.put("monthlyRevenue", monthlyRevenue(connection, from, to));
            report.put("studentsByBuilding", studentsByBuilding(connection));
            report.put("roomRatio", roomRatio(connection));
            report.put("invoiceStatus", invoiceStatus(connection, from, to));
            report.put("newStudents", newStudents(connection, from, to));
            report.put("availableRooms", availableRooms(connection));
            report.put("unpaidInvoices", unpaidInvoices(connection, from, to));
            report.put("pendingRequests", pendingRequests(connection, from, to));
        }
        return report;
    }

    private Map<String, Object> summary(Connection connection, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT
                  (SELECT COUNT(*) FROM students) total_students,
                  (SELECT COUNT(*) FROM rooms) total_rooms,
                  (SELECT COUNT(*) FROM rooms WHERE current_quantity < capacity AND status <> 'Bảo trì') empty_rooms,
                  (SELECT COUNT(*) FROM rooms WHERE current_quantity > 0) used_rooms,
                  (SELECT COUNT(*) FROM invoices WHERE DATE(created_at) BETWEEN ? AND ?) total_invoices,
                  (SELECT COUNT(*) FROM invoices WHERE status IN ('Chưa thanh toán','Quá hạn') AND DATE(created_at) BETWEEN ? AND ?) unpaid_invoices,
                  (SELECT COALESCE(SUM(amount),0) FROM finance_transactions WHERE transaction_type='Thu' AND DATE(transaction_date) BETWEEN ? AND ?) revenue,
                  (SELECT COUNT(*) FROM requests WHERE DATE(created_at) BETWEEN ? AND ?) total_requests
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setRange(statement, 1, from, to); setRange(statement, 3, from, to);
            setRange(statement, 5, from, to); setRange(statement, 7, from, to);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return row(resultSet);
            }
        }
    }

    private List<Map<String, Object>> monthlyRevenue(Connection connection, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT DATE_FORMAT(transaction_date,'%Y-%m') month_key,
                       DATE_FORMAT(transaction_date,'%m/%Y') label,
                       COALESCE(SUM(amount),0) value
                FROM finance_transactions
                WHERE transaction_type='Thu' AND DATE(transaction_date) BETWEEN ? AND ?
                GROUP BY month_key,label ORDER BY month_key
                """;
        return rangeRows(connection, sql, from, to);
    }

    private List<Map<String, Object>> studentsByBuilding(Connection connection) throws SQLException {
        String sql = """
                SELECT b.building_name label, COUNT(s.id) value
                FROM buildings b LEFT JOIN rooms r ON r.building_id=b.id
                LEFT JOIN students s ON s.room_id=r.id
                GROUP BY b.id,b.building_name ORDER BY b.building_code
                """;
        return rows(connection, sql);
    }

    private List<Map<String, Object>> roomRatio(Connection connection) throws SQLException {
        String sql = """
                SELECT CASE
                  WHEN status='Bảo trì' THEN 'Bảo trì'
                  WHEN current_quantity=0 THEN 'Phòng trống'
                  WHEN current_quantity>=capacity THEN 'Đã đầy'
                  ELSE 'Đang sử dụng' END label, COUNT(*) value
                FROM rooms GROUP BY label ORDER BY label
                """;
        return rows(connection, sql);
    }

    private List<Map<String, Object>> invoiceStatus(Connection connection, LocalDate from, LocalDate to) throws SQLException {
        return rangeRows(connection, "SELECT status label,COUNT(*) value FROM invoices WHERE DATE(created_at) BETWEEN ? AND ? GROUP BY status ORDER BY status", from, to);
    }

    private List<Map<String, Object>> newStudents(Connection connection, LocalDate from, LocalDate to) throws SQLException {
        return rangeRows(connection, """
                SELECT s.student_code,s.full_name,COALESCE(r.room_code,'Chưa xếp') room_code,s.created_at
                FROM students s LEFT JOIN rooms r ON r.id=s.room_id
                WHERE DATE(s.created_at) BETWEEN ? AND ? ORDER BY s.created_at DESC LIMIT 8
                """, from, to);
    }

    private List<Map<String, Object>> availableRooms(Connection connection) throws SQLException {
        return rows(connection, """
                SELECT r.room_code,b.building_name,r.capacity,r.current_quantity,
                       (r.capacity-r.current_quantity) available_slots
                FROM rooms r JOIN buildings b ON b.id=r.building_id
                WHERE r.current_quantity<r.capacity AND r.status<>'Bảo trì'
                ORDER BY available_slots DESC,r.room_code LIMIT 8
                """);
    }

    private List<Map<String, Object>> unpaidInvoices(Connection connection, LocalDate from, LocalDate to) throws SQLException {
        return rangeRows(connection, """
                SELECT i.invoice_code,s.full_name,i.total_amount,i.status,i.due_date
                FROM invoices i JOIN students s ON s.id=i.student_id
                WHERE i.status IN ('Chưa thanh toán','Quá hạn') AND DATE(i.created_at) BETWEEN ? AND ?
                ORDER BY i.due_date,i.id DESC LIMIT 8
                """, from, to);
    }

    private List<Map<String, Object>> pendingRequests(Connection connection, LocalDate from, LocalDate to) throws SQLException {
        return rangeRows(connection, """
                SELECT q.request_code,s.full_name,q.request_type,q.priority,q.status,q.created_at
                FROM requests q JOIN students s ON s.id=q.student_id
                WHERE q.status IN ('Mới tiếp nhận','Đang xử lý') AND DATE(q.created_at) BETWEEN ? AND ?
                ORDER BY q.created_at DESC LIMIT 8
                """, from, to);
    }

    private List<Map<String, Object>> rangeRows(Connection connection, String sql, LocalDate from, LocalDate to) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setRange(statement, 1, from, to);
            try (ResultSet resultSet = statement.executeQuery()) { return rows(resultSet); }
        }
    }

    private List<Map<String, Object>> rows(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) { return rows(resultSet); }
    }

    private void setRange(PreparedStatement statement, int index, LocalDate from, LocalDate to) throws SQLException {
        statement.setDate(index, Date.valueOf(from));
        statement.setDate(index + 1, Date.valueOf(to));
    }
}
