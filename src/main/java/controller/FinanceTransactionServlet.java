package controller;

import config.Database;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/finance-transactions", "/api/finance-transactions/*"})
public class FinanceTransactionServlet extends BaseServlet {
    @Override protected void service(HttpServletRequest request, HttpServletResponse response) throws jakarta.servlet.ServletException, IOException {
        request.setCharacterEncoding("UTF-8"); response.setCharacterEncoding("UTF-8"); super.service(request, response);
    }
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = pathId(request); if (id == null) list(request, response); else detail(id, response);
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException { save(null, request, response); }
    @Override protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = pathId(request); if (id == null) writeJson(response, 400, jsonMap("message", "Thiếu mã giao dịch")); else save(id, request, response);
    }
    @Override protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer id = pathId(request); if (id == null) { writeJson(response, 400, jsonMap("message", "Thiếu mã giao dịch")); return; }
        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM finance_transactions WHERE id=?")) {
            statement.setInt(1, id); if (statement.executeUpdate() == 0) { writeJson(response, 404, jsonMap("message", "Không tìm thấy giao dịch")); return; }
            writeJson(response, jsonMap("success", true, "message", "Xóa giao dịch thành công"));
        } catch (SQLException e) { fail("Không thể xóa giao dịch", e, response); }
    }

    private void list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = Math.max(number(request.getParameter("page"), 1), 1), limit = Math.min(Math.max(number(request.getParameter("limit"), 10), 1), 100), offset = (page - 1) * limit;
        List<String> where = new ArrayList<>(); List<Object> params = new ArrayList<>();
        String search = clean(request.getParameter("search"));
        if (search != null) { String keyword = "%" + search + "%"; where.add("(f.transaction_code LIKE ? OR f.content LIKE ? OR s.full_name LIKE ? OR r.room_code LIKE ?)"); for (int i=0;i<4;i++) params.add(keyword); }
        filter(where, params, "f.transaction_type=?", request.getParameter("type")); filter(where, params, "f.category=?", request.getParameter("category"));
        filter(where, params, "f.payment_method=?", request.getParameter("payment_method")); filter(where, params, "DATE(f.transaction_date)>=?", request.getParameter("from")); filter(where, params, "DATE(f.transaction_date)<=?", request.getParameter("to"));
        String clause = where.isEmpty() ? "" : " WHERE " + String.join(" AND ", where);
        String joins = " FROM finance_transactions f LEFT JOIN students s ON s.id=f.student_id LEFT JOIN rooms r ON r.id=s.room_id LEFT JOIN buildings b ON b.id=r.building_id ";
        try (Connection connection = Database.getConnection()) {
            int total; try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*)" + joins + clause)) { setParams(statement, params); try (ResultSet result = statement.executeQuery()) { result.next(); total = result.getInt(1); } }
            List<Object> listParams = new ArrayList<>(params); listParams.add(limit); listParams.add(offset);
            List<Map<String,Object>> items; String select = "SELECT f.*,s.student_code,s.full_name,r.room_code,b.building_name" + joins + clause + " ORDER BY f.transaction_date DESC,f.id DESC LIMIT ? OFFSET ?";
            try (PreparedStatement statement = connection.prepareStatement(select)) { setParams(statement, listParams); try (ResultSet result = statement.executeQuery()) { items = rows(result); } }
            Map<String,Object> stats; try (PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(SUM(CASE WHEN transaction_type='Thu' THEN amount ELSE 0 END),0) total_income,COALESCE(SUM(CASE WHEN transaction_type='Chi' THEN amount ELSE 0 END),0) total_expense,COALESCE(SUM(CASE WHEN transaction_type='Thu' THEN amount ELSE -amount END),0) balance,COUNT(*) transaction_count FROM finance_transactions"); ResultSet result = statement.executeQuery()) { result.next(); stats = row(result); }
            List<Map<String,Object>> categories; try (PreparedStatement statement = connection.prepareStatement("SELECT category,transaction_type,COALESCE(SUM(amount),0) total FROM finance_transactions GROUP BY category,transaction_type ORDER BY total DESC"); ResultSet result = statement.executeQuery()) { categories = rows(result); }
            writeJson(response, jsonMap("transactions", items, "total", total, "page", page, "limit", limit, "stats", stats, "categories", categories));
        } catch (SQLException e) { fail("Không tải được danh sách giao dịch", e, response); }
    }
    private void detail(int id, HttpServletResponse response) throws IOException {
        String sql = "SELECT f.*,s.student_code,s.full_name,r.room_code,b.building_name FROM finance_transactions f LEFT JOIN students s ON s.id=f.student_id LEFT JOIN rooms r ON r.id=s.room_id LEFT JOIN buildings b ON b.id=r.building_id WHERE f.id=?";
        try (Connection connection=Database.getConnection(); PreparedStatement statement=connection.prepareStatement(sql)) { statement.setInt(1,id); try(ResultSet result=statement.executeQuery()){ if(!result.next()){writeJson(response,404,jsonMap("message","Không tìm thấy giao dịch"));return;} writeJson(response,row(result)); } }
        catch(SQLException e){fail("Không tải được chi tiết giao dịch",e,response);}
    }
    private void save(Integer id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try { Map<String,Object> body=readJson(request); Integer student=integer(body,"student_id"); String content=text(body,"content"),type=text(body,"transaction_type"),category=text(body,"category"),method=text(body,"payment_method"),performer=text(body,"performed_by"),note=text(body,"note"); BigDecimal amount=decimal(body.get("amount")); Timestamp date=timestamp(text(body,"transaction_date"));
            if(content==null||content.length()>500||!("Thu".equals(type)||"Chi".equals(type))||category==null||method==null||performer==null||amount==null||amount.signum()<=0||date==null){writeJson(response,400,jsonMap("message","Vui lòng nhập đầy đủ ngày, nội dung, loại, danh mục, số tiền, phương thức và người thực hiện"));return;}
            if(id==null) create(student,date,content,type,category,amount,method,performer,note,response); else update(id,student,date,content,type,category,amount,method,performer,note,response);
        } catch(IllegalArgumentException e){writeJson(response,400,jsonMap("message",e.getMessage()));} catch(SQLException e){fail("Không thể lưu giao dịch",e,response);}
    }
    private void create(Integer student,Timestamp date,String content,String type,String category,BigDecimal amount,String method,String performer,String note,HttpServletResponse response)throws SQLException,IOException{
        try(Connection connection=Database.getConnection()){connection.setAutoCommit(false);try(PreparedStatement statement=connection.prepareStatement("INSERT INTO finance_transactions(transaction_code,student_id,transaction_date,content,transaction_type,category,amount,payment_method,performed_by,note) VALUES(?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){statement.setString(1,"TMP"+System.nanoTime());bind(statement,2,student,date,content,type,category,amount,method,performer,note);statement.executeUpdate();try(ResultSet keys=statement.getGeneratedKeys()){keys.next();int id=keys.getInt(1);String code="GD"+String.format("%07d",id);try(PreparedStatement update=connection.prepareStatement("UPDATE finance_transactions SET transaction_code=? WHERE id=?")){update.setString(1,code);update.setInt(2,id);update.executeUpdate();}connection.commit();writeJson(response,201,jsonMap("success",true,"id",id,"transaction_code",code,"message","Thêm giao dịch thành công"));}}catch(SQLException|IOException e){connection.rollback();throw e;}finally{connection.setAutoCommit(true);}}
    }
    private void update(int id,Integer student,Timestamp date,String content,String type,String category,BigDecimal amount,String method,String performer,String note,HttpServletResponse response)throws SQLException,IOException{
        try(Connection connection=Database.getConnection();PreparedStatement statement=connection.prepareStatement("UPDATE finance_transactions SET student_id=?,transaction_date=?,content=?,transaction_type=?,category=?,amount=?,payment_method=?,performed_by=?,note=? WHERE id=?")){bind(statement,1,student,date,content,type,category,amount,method,performer,note);statement.setInt(10,id);if(statement.executeUpdate()==0){writeJson(response,404,jsonMap("message","Không tìm thấy giao dịch"));return;}writeJson(response,jsonMap("success",true,"message","Cập nhật giao dịch thành công"));}
    }
    private void bind(PreparedStatement s,int n,Integer student,Timestamp date,String content,String type,String category,BigDecimal amount,String method,String performer,String note)throws SQLException{if(student==null)s.setNull(n,Types.INTEGER);else s.setInt(n,student);s.setTimestamp(n+1,date);s.setString(n+2,content);s.setString(n+3,type);s.setString(n+4,category);s.setBigDecimal(n+5,amount);s.setString(n+6,method);s.setString(n+7,performer);s.setString(n+8,note);}
    private BigDecimal decimal(Object value){try{return value==null?null:new BigDecimal(String.valueOf(value));}catch(Exception e){return null;}}
    private Timestamp timestamp(String value){if(value==null)return null;try{return Timestamp.valueOf(LocalDateTime.parse(value));}catch(Exception e){try{return Timestamp.valueOf(value.replace('T',' ')+":00");}catch(Exception ignored){throw new IllegalArgumentException("Ngày giao dịch không hợp lệ");}}}
    private Integer pathId(HttpServletRequest request){String value=request.getPathInfo();if(value==null||value.equals("/")||value.isBlank())return null;try{return Integer.valueOf(value.substring(1).split("/")[0]);}catch(Exception e){return null;}}
    private void filter(List<String>w,List<Object>p,String sql,String value){String v=clean(value);if(v!=null){w.add(sql);p.add(v);}} private String clean(String v){return v==null||v.trim().isEmpty()?null:v.trim();} private int number(String v,int fallback){try{return v==null?fallback:Integer.parseInt(v);}catch(Exception e){return fallback;}}
    private void fail(String message,Exception e,HttpServletResponse response)throws IOException{getServletContext().log(message,e);writeJson(response,500,jsonMap("message",message));}
}
