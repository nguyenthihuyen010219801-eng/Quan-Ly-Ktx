package controller;

import dao.RoomDAO;
import model.Room;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/rooms")
public class RoomServlet extends BaseServlet {
    private final RoomDAO dao = new RoomDAO();
    @Override protected void service(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{req.setCharacterEncoding("UTF-8");resp.setCharacterEncoding("UTF-8");super.service(req,resp);}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String action=req.getParameter("action");
        if(action==null||action.isBlank()){req.getRequestDispatcher("/WEB-INF/views/rooms.jsp").forward(req,resp);return;}
        try{
            if("detail".equals(action)){Map<String,Object> data=dao.detail(id(req));if(data==null){writeJson(resp,404,jsonMap("message","Không tìm thấy phòng"));return;}writeJson(resp,data);return;}
            Integer buildingId=optionalInt(req.getParameter("building_id"));
            writeJson(resp,dao.search(keyword(req),buildingId));
        }catch(Exception e){writeJson(resp,500,jsonMap("message",message(e,"Không tải được dữ liệu phòng")));}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{save(req,resp,false);}
    @Override protected void doPut(HttpServletRequest req,HttpServletResponse resp)throws IOException{save(req,resp,true);}
    @Override protected void doDelete(HttpServletRequest req,HttpServletResponse resp)throws IOException{try{dao.delete(id(req));writeJson(resp,jsonMap("success",true,"message","Xóa phòng thành công"));}catch(Exception e){writeJson(resp,409,jsonMap("message",message(e,"Không thể xóa phòng")));}}
    private void save(HttpServletRequest req,HttpServletResponse resp,boolean edit)throws IOException{
        try{Map<String,Object>d=readJson(req);Room r=new Room();r.id=edit?id(req):null;r.roomCode=text(d,"room_code");r.roomName=text(d,"room_name");r.buildingId=integer(d,"building_id");r.roomType=value(text(d,"room_type"),"Tiêu chuẩn");r.floor=integer(d,"floor");r.capacity=integer(d,"capacity");r.currentQuantity=integer(d,"current_quantity");r.price=decimal(d.get("price"));r.status=value(text(d,"status"),"Còn trống");validate(r);int id=edit?(dao.update(r)?r.id:0):dao.create(r);if(id==0){writeJson(resp,404,jsonMap("message","Không tìm thấy phòng"));return;}writeJson(resp,edit?200:201,jsonMap("success",true,"id",id,"message",edit?"Cập nhật phòng thành công":"Thêm phòng thành công"));}
        catch(IllegalArgumentException e){writeJson(resp,400,jsonMap("message",e.getMessage()));}catch(SQLException e){writeJson(resp,400,jsonMap("message",e.getErrorCode()==1062?"Mã phòng đã tồn tại":message(e,"Không thể lưu phòng")));}
    }
    private void validate(Room r){if(r.roomCode==null||r.roomName==null||r.buildingId==null||r.floor==null||r.floor<1||r.capacity==null||r.capacity<1||r.currentQuantity==null||r.currentQuantity<0||r.currentQuantity>r.capacity||r.price==null||r.price.signum()<0)throw new IllegalArgumentException("Vui lòng kiểm tra mã, tên, tòa nhà, loại phòng, sức chứa, số người và giá phòng");}
    private int id(HttpServletRequest r){return Integer.parseInt(r.getParameter("id"));}private Integer optionalInt(String s){return s==null||s.isBlank()?null:Integer.valueOf(s);}private String keyword(HttpServletRequest r){String k=r.getParameter("keyword");return k==null?r.getParameter("search"):k;}private BigDecimal decimal(Object o){try{return o==null?null:new BigDecimal(String.valueOf(o));}catch(Exception e){return null;}}private String value(String s,String f){return s==null?f:s;}private String message(Exception e,String f){return e.getMessage()==null?f:e.getMessage();}
}
