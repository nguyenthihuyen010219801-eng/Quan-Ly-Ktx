package com.dormitory.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;

public final class RequestFeedbackSchemaMigration {
    private RequestFeedbackSchemaMigration() {}

    public static void run(ServletContext context) {
        String requestsSql = """
            CREATE TABLE IF NOT EXISTS requests (
                id INT AUTO_INCREMENT PRIMARY KEY,
                request_code VARCHAR(20) UNIQUE,
                student_id INT NOT NULL,
                request_type VARCHAR(100) NOT NULL,
                content TEXT NOT NULL,
                priority ENUM('Cao','Trung bình','Thấp') NOT NULL DEFAULT 'Trung bình',
                status ENUM('Mới tiếp nhận','Đang xử lý','Đã xử lý','Đã đóng') NOT NULL DEFAULT 'Mới tiếp nhận',
                due_date DATE,
                assignee VARCHAR(100),
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT fk_requests_student FOREIGN KEY (student_id) REFERENCES students(id)
            ) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
            """;
        String responsesSql = """
            CREATE TABLE IF NOT EXISTS request_responses (
                id INT AUTO_INCREMENT PRIMARY KEY,
                request_id INT NOT NULL,
                response_content TEXT NOT NULL,
                responder VARCHAR(100) NOT NULL,
                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT fk_responses_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE
            ) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
            """;
        try (Connection connection = Database.getConnection()) {
            execute(connection, requestsSql);
            execute(connection, responsesSql);
            seedInitialData(connection);
            context.log("Đã chuẩn bị schema Yêu cầu - Phản hồi");
        } catch (SQLException e) {
            context.log("Không thể chuẩn bị schema Yêu cầu - Phản hồi", e);
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    private static void seedInitialData(Connection connection) throws SQLException {
        try (PreparedStatement count = connection.prepareStatement("SELECT COUNT(*) FROM requests"); ResultSet rows = count.executeQuery()) {
            rows.next();
            if (rows.getInt(1) > 0) return;
        }

        List<Integer> studentIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM students WHERE room_id IS NOT NULL ORDER BY student_code"); ResultSet rows = statement.executeQuery()) {
            while (rows.next()) studentIds.add(rows.getInt(1));
        }
        if (studentIds.isEmpty()) return;

        String[] types = {"Sửa chữa cơ sở vật chất","Vệ sinh","Điện, nước","Dịch vụ khác","An ninh, an toàn","Khác"};
        String[] contents = {"Bồn nước bị rò rỉ","Nhà vệ sinh bẩn","Đèn phòng không sáng","Đề nghị lắp thêm kệ","Mất trộm xe đạp","Cửa sổ bị kẹt","Rác không được dọn","Quạt trần không chạy","Đề nghị đổi mật khẩu wifi","Tường bị thấm nước"};
        String[] firstTypes = {types[0],types[1],types[2],types[3],types[4],types[0],types[1],types[2],types[3],types[0]};
        String[] firstPriorities = {"Cao","Trung bình","Cao","Thấp","Cao","Trung bình","Trung bình","Thấp","Thấp","Cao"};
        String[] firstStatuses = {"Đang xử lý","Đã xử lý","Đang xử lý","Đã đóng","Đã xử lý","Đang xử lý","Đã xử lý","Đã đóng","Đã xử lý","Đang xử lý"};
        String[] firstCreated = {"2025-05-20T09:15","2025-05-20T08:30","2025-05-19T16:45","2025-05-19T14:20","2025-05-18T10:05","2025-05-18T09:30","2025-05-17T15:00","2025-05-17T11:30","2025-05-16T10:20","2025-05-15T14:10"};
        String[] firstDue = {"2025-05-22","2025-05-20","2025-05-21","2025-05-19","2025-05-18","2025-05-20","2025-05-17","2025-05-17","2025-05-16","2025-05-17"};
        String[] assignees = {"Trần Văn Minh","Phạm Thị Hương","Nguyễn Thị Lan"};
        String insertSql = "INSERT INTO requests(request_code,student_id,request_type,content,priority,status,due_date,assignee,created_at) VALUES(?,?,?,?,?,?,?,?,?)";

        connection.setAutoCommit(false);
        try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (int i = 0; i < 10; i++) {
                LocalDateTime created = LocalDateTime.parse(firstCreated[i]);
                LocalDate due = LocalDate.parse(firstDue[i]);
                bindSeed(insert, 1256-i, studentIds.get(i%studentIds.size()), firstTypes[i], contents[i], firstPriorities[i], firstStatuses[i], due, assignees[i%assignees.length], created);
                insert.addBatch();
            }
            String[] remainingTypes = expand(types, new int[]{55,34,22,18,11,6});
            String[] remainingPriorities = expand(new String[]{"Cao","Trung bình","Thấp"}, new int[]{24,83,39});
            String[] remainingStatuses = expand(new String[]{"Đã xử lý","Đang xử lý","Đã đóng"}, new int[]{94,38,14});
            for (int i = 0; i < 146; i++) {
                LocalDateTime created = LocalDateTime.of(2025,5,15,13,30).minusHours(i * 3L);
                String type = remainingTypes[i];
                String content = switch (type) {
                    case "Sửa chữa cơ sở vật chất" -> "Kiểm tra và sửa chữa thiết bị trong phòng";
                    case "Vệ sinh" -> "Đề nghị hỗ trợ vệ sinh khu vực phòng ở";
                    case "Điện, nước" -> "Kiểm tra hệ thống điện nước trong phòng";
                    case "Dịch vụ khác" -> "Đề nghị hỗ trợ dịch vụ ký túc xá";
                    case "An ninh, an toàn" -> "Đề nghị kiểm tra an ninh khu vực";
                    default -> "Yêu cầu hỗ trợ khác của sinh viên";
                };
                bindSeed(insert,1246-i,studentIds.get(i%studentIds.size()),type,content,remainingPriorities[i],remainingStatuses[i],created.toLocalDate().plusDays(2),assignees[i%assignees.length],created);
                insert.addBatch();
            }
            insert.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static void bindSeed(PreparedStatement statement,int number,int studentId,String type,String content,String priority,String status,LocalDate due,String assignee,LocalDateTime created)throws SQLException{
        statement.setString(1,"YC"+String.format("%07d",number));
        statement.setInt(2,studentId);
        statement.setString(3,type);
        statement.setString(4,content);
        statement.setString(5,priority);
        statement.setString(6,status);
        statement.setDate(7,java.sql.Date.valueOf(due));
        statement.setString(8,assignee);
        statement.setTimestamp(9,Timestamp.valueOf(created));
    }

    private static String[] expand(String[] values,int[] counts){List<String> result=new ArrayList<>();for(int i=0;i<values.length;i++)for(int j=0;j<counts[i];j++)result.add(values[i]);return result.toArray(String[]::new);}
}
