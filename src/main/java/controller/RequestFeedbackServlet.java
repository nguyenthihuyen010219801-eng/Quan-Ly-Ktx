package controller;

import config.Database;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/requests", "/api/requests/*"})
public class RequestFeedbackServlet extends BaseServlet {
    private static final List<String> PRIORITIES = List.of("Cao", "Trung bình", "Thấp");
    private static final List<String> STATUSES = List.of("Mới tiếp nhận", "Đang xử lý", "Đã xử lý", "Đã đóng");

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws jakarta.servlet.ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path path = parsePath(request);
        if (path.id == null) list(request, response); else detail(path.id, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path path = parsePath(request);
        if (path.id != null && "responses".equals(path.action)) addResponse(path.id, request, response);
        else if (path.id == null) create(request, response);
        else writeJson(response, 404, jsonMap("message", "Đường dẫn không hợp lệ"));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path path = parsePath(request);
        if (path.id == null) { writeJson(response, 400, jsonMap("message", "Thiếu mã yêu cầu")); return; }
        update(path.id, request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Path path = parsePath(request);
        if (path.id == null) { writeJson(response, 400, jsonMap("message", "Thiếu mã yêu cầu")); return; }
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM requests WHERE id=?")) {
            statement.setInt(1, path.id);
            if (statement.executeUpdate() == 0) { writeJson(response, 404, jsonMap("message", "Không tìm thấy yêu cầu")); return; }
            writeJson(response, jsonMap("success", true, "message", "Xóa yêu cầu thành công"));
        } catch (SQLException e) { serverError("Không thể xóa yêu cầu", e, response); }
    }

    private void list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = Math.max(parseInt(request.getParameter("page"), 1), 1);
        int limit = Math.min(Math.max(parseInt(request.getParameter("limit"), 10), 1), 100);
        int offset = (page - 1) * limit;
        List<String> where = new ArrayList<>(); List<Object> params = new ArrayList<>();
        String search = trim(request.getParameter("search"));
        if (search != null) {
            where.add("(q.request_code LIKE ? OR s.full_name LIKE ? OR r.room_code LIKE ? OR q.content LIKE ?)");
            String keyword = "%" + search + "%";
            for (int i = 0; i < 4; i++) params.add(keyword);
        }
        addFilter(where, params, "q.request_type = ?", request.getParameter("type"));
        addFilter(where, params, "q.priority = ?", request.getParameter("priority"));
        addFilter(where, params, "q.status = ?", request.getParameter("status"));
        addFilter(where, params, "DATE(q.created_at) >= ?", request.getParameter("from"));
        addFilter(where, params, "DATE(q.created_at) <= ?", request.getParameter("to"));
        String whereSql = where.isEmpty() ? "" : " WHERE " + String.join(" AND ", where);
        String joins = " FROM requests q JOIN students s ON s.id=q.student_id LEFT JOIN rooms r ON r.id=s.room_id ";
        String select = "SELECT q.*,s.student_code,s.full_name,r.room_code " + joins + whereSql + " ORDER BY q.created_at DESC,q.id DESC LIMIT ? OFFSET ?";
        String count = "SELECT COUNT(*) total " + joins + whereSql;
        try (Connection connection = Database.getConnection()) {
            int total;
            try (PreparedStatement statement = connection.prepareStatement(count)) {
                setParams(statement, params); try (ResultSet rs = statement.executeQuery()) { rs.next(); total = rs.getInt(1); }
            }
            List<Object> listParams = new ArrayList<>(params); listParams.add(limit); listParams.add(offset);
            List<Map<String,Object>> items;
            try (PreparedStatement statement = connection.prepareStatement(select)) {
                setParams(statement, listParams); try (ResultSet rs = statement.executeQuery()) { items = rows(rs); }
            }
            Map<String,Object> stats;
            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) total,SUM(status='Đã xử lý') resolved,SUM(status='Đang xử lý') processing,SUM(status='Đã đóng') closed,SUM(status='Mới tiếp nhận') received FROM requests"); ResultSet rs = statement.executeQuery()) { rs.next(); stats = row(rs); }
            writeJson(response, jsonMap("requests",items,"total",total,"page",page,"limit",limit,"stats",stats,"type_counts",groupCounts(connection,"request_type"),"priority_counts",groupCounts(connection,"priority"),"status_counts",groupCounts(connection,"status")));
        } catch (SQLException e) { serverError("Không tải được danh sách yêu cầu", e, response); }
    }

    private void detail(int id, HttpServletResponse response) throws IOException {
        String sql = "SELECT q.*,s.student_code,s.full_name,s.phone,s.email,r.room_code,b.building_name FROM requests q JOIN students s ON s.id=q.student_id LEFT JOIN rooms r ON r.id=s.room_id LEFT JOIN buildings b ON b.id=r.building_id WHERE q.id=?";
        try (Connection connection=Database.getConnection(); PreparedStatement requestStatement=connection.prepareStatement(sql); PreparedStatement responseStatement=connection.prepareStatement("SELECT id,response_content,responder,created_at FROM request_responses WHERE request_id=? ORDER BY created_at,id")) {
            requestStatement.setInt(1,id); Map<String,Object> item;
            try(ResultSet rs=requestStatement.executeQuery()){if(!rs.next()){writeJson(response,404,jsonMap("message","Không tìm thấy yêu cầu"));return;}item=row(rs);}
            responseStatement.setInt(1,id); List<Map<String,Object>> history; try(ResultSet rs=responseStatement.executeQuery()){history=rows(rs);}
            writeJson(response,jsonMap("request",item,"responses",history));
        } catch(SQLException e){serverError("Không tải được chi tiết yêu cầu",e,response);}
    }

    private void create(HttpServletRequest request,HttpServletResponse response)throws IOException{
        try { Map<String,Object> body=readJson(request); Integer studentId=integer(body,"student_id"); String type=text(body,"request_type"),content=text(body,"content"),priority=text(body,"priority"),status=text(body,"status"),assignee=text(body,"assignee"); Date dueDate=date(body,"due_date");
            if(studentId==null||type==null||content==null||!PRIORITIES.contains(priority)||!STATUSES.contains(status)){writeJson(response,400,jsonMap("message","Vui lòng nhập đầy đủ sinh viên, loại yêu cầu, nội dung, ưu tiên và trạng thái hợp lệ"));return;}
            try(Connection connection=Database.getConnection()){connection.setAutoCommit(false);try(PreparedStatement statement=connection.prepareStatement("INSERT INTO requests(student_id,request_type,content,priority,status,due_date,assignee) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){statement.setInt(1,studentId);statement.setString(2,type);statement.setString(3,content);statement.setString(4,priority);statement.setString(5,status);statement.setDate(6,dueDate);statement.setString(7,assignee);statement.executeUpdate();try(ResultSet keys=statement.getGeneratedKeys()){keys.next();int id=keys.getInt(1);String code="YC"+String.format("%07d",id);try(PreparedStatement update=connection.prepareStatement("UPDATE requests SET request_code=? WHERE id=?")){update.setString(1,code);update.setInt(2,id);update.executeUpdate();}connection.commit();writeJson(response,201,jsonMap("success",true,"id",id,"request_code",code,"message","Thêm yêu cầu thành công"));}}catch(Exception e){connection.rollback();throw e;}finally{connection.setAutoCommit(true);}}
        } catch(IllegalArgumentException e){writeJson(response,400,jsonMap("message",e.getMessage()));}catch(Exception e){serverError("Không thể thêm yêu cầu",e,response);}
    }

    private void update(int id,HttpServletRequest request,HttpServletResponse response)throws IOException{
        try{Map<String,Object> body=readJson(request);String status=text(body,"status"),assignee=text(body,"assignee");boolean staff="nhanvien".equals(request.getSession(false).getAttribute("accountRole"));if(staff){if(!STATUSES.contains(status)){writeJson(response,400,jsonMap("message","Trạng thái không hợp lệ"));return;}try(Connection connection=Database.getConnection();PreparedStatement statement=connection.prepareStatement("UPDATE requests SET status=?,assignee=? WHERE id=?")){statement.setString(1,status);statement.setString(2,assignee);statement.setInt(3,id);if(statement.executeUpdate()==0){writeJson(response,404,jsonMap("message","Không tìm thấy yêu cầu"));return;}writeJson(response,jsonMap("success",true,"message","Cập nhật trạng thái yêu cầu thành công"));}return;}String type=text(body,"request_type"),content=text(body,"content"),priority=text(body,"priority");Date dueDate=date(body,"due_date");if(type==null||content==null||!PRIORITIES.contains(priority)||!STATUSES.contains(status)){writeJson(response,400,jsonMap("message","Loại yêu cầu, nội dung, ưu tiên và trạng thái không hợp lệ"));return;}try(Connection connection=Database.getConnection();PreparedStatement statement=connection.prepareStatement("UPDATE requests SET request_type=?,content=?,priority=?,status=?,due_date=?,assignee=? WHERE id=?")){statement.setString(1,type);statement.setString(2,content);statement.setString(3,priority);statement.setString(4,status);statement.setDate(5,dueDate);statement.setString(6,assignee);statement.setInt(7,id);if(statement.executeUpdate()==0){writeJson(response,404,jsonMap("message","Không tìm thấy yêu cầu"));return;}writeJson(response,jsonMap("success",true,"message","Cập nhật yêu cầu thành công"));}}
        catch(IllegalArgumentException e){writeJson(response,400,jsonMap("message",e.getMessage()));}catch(SQLException e){serverError("Không thể cập nhật yêu cầu",e,response);}
    }

    private void addResponse(int id,HttpServletRequest request,HttpServletResponse response)throws IOException{
        try{Map<String,Object> body=readJson(request);String content=text(body,"response_content"),responder=text(body,"responder");if(content==null||responder==null){writeJson(response,400,jsonMap("message","Vui lòng nhập nội dung phản hồi và người phản hồi"));return;}try(Connection connection=Database.getConnection();PreparedStatement check=connection.prepareStatement("SELECT id FROM requests WHERE id=?");PreparedStatement insert=connection.prepareStatement("INSERT INTO request_responses(request_id,response_content,responder) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){check.setInt(1,id);try(ResultSet rs=check.executeQuery()){if(!rs.next()){writeJson(response,404,jsonMap("message","Không tìm thấy yêu cầu"));return;}}insert.setInt(1,id);insert.setString(2,content);insert.setString(3,responder);insert.executeUpdate();try(ResultSet keys=insert.getGeneratedKeys()){keys.next();writeJson(response,201,jsonMap("success",true,"id",keys.getInt(1),"message","Thêm phản hồi thành công"));}}}
        catch(SQLException e){serverError("Không thể lưu phản hồi",e,response);}
    }

    private List<Map<String,Object>> groupCounts(Connection connection,String column)throws SQLException{String safe=List.of("request_type","priority","status").contains(column)?column:"status";try(PreparedStatement statement=connection.prepareStatement("SELECT "+safe+" label,COUNT(*) total FROM requests GROUP BY "+safe+" ORDER BY total DESC");ResultSet rs=statement.executeQuery()){return rows(rs);}}
    private Date date(Map<String,Object> body,String key){String value=text(body,key);if(value==null)return null;try{return Date.valueOf(value);}catch(Exception e){throw new IllegalArgumentException("Ngày không hợp lệ");}}
    private void addFilter(List<String>w,List<Object>p,String clause,String value){String v=trim(value);if(v!=null){w.add(clause);p.add(v);}}
    private String trim(String value){return value==null||value.trim().isEmpty()?null:value.trim();}
    private int parseInt(String value,int fallback){try{return value==null?fallback:Integer.parseInt(value);}catch(Exception e){return fallback;}}
    private void serverError(String message,Exception error,HttpServletResponse response)throws IOException{getServletContext().log(message,error);writeJson(response,500,jsonMap("message",message));}
    private Path parsePath(HttpServletRequest request){String path=request.getPathInfo();if(path==null||path.equals("/")||path.isBlank())return new Path(null,null);String[]parts=path.substring(1).split("/");try{return new Path(Integer.valueOf(parts[0]),parts.length>1?parts[1]:null);}catch(Exception e){return new Path(null,"invalid");}}
    private record Path(Integer id,String action){}
}
