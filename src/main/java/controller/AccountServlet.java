package com.dormitory.controller;

import com.dormitory.dao.UserDAO;
import com.dormitory.util.PasswordUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

@WebServlet(urlPatterns={"/api/accounts","/api/accounts/*"})
public class AccountServlet extends BaseServlet {
    private static final Set<String> ROLES=Set.of("quanly","nhanvien");
    private final UserDAO dao=new UserDAO();

    @Override protected void service(HttpServletRequest req,HttpServletResponse resp)throws jakarta.servlet.ServletException,IOException{
        req.setCharacterEncoding("UTF-8");resp.setCharacterEncoding("UTF-8");
        if(!authorized(req,resp))return;super.service(req,resp);
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        try{Path p=path(req);if(p.id()==null){int page=Math.max(parse(req.getParameter("page"),1),1),limit=Math.min(Math.max(parse(req.getParameter("limit"),10),1),50);writeJson(resp,dao.search(req.getParameter("search"),req.getParameter("role"),req.getParameter("status"),page,limit));return;}Map<String,Object> user=dao.find(p.id());if(user==null){writeJson(resp,404,jsonMap("message","Không tìm thấy tài khoản"));return;}writeJson(resp,user);}catch(Exception e){error(resp,e);}}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        try{Path p=path(req);if(p.id()==null){create(req,resp);return;}if("lock".equals(p.action())){changeStatus(req,resp,p.id(),"locked");return;}if("unlock".equals(p.action())){changeStatus(req,resp,p.id(),"active");return;}if("reset-password".equals(p.action())){reset(req,resp,p.id());return;}writeJson(resp,404,jsonMap("message","Đường dẫn không hợp lệ"));}catch(Exception e){error(resp,e);}}
    @Override protected void doPut(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        try{Path p=path(req);if(p.id()==null){writeJson(resp,400,jsonMap("message","Thiếu ID tài khoản"));return;}Map<String,Object> body=readJson(req),existing=dao.find(p.id());if(existing==null){writeJson(resp,404,jsonMap("message","Không tìm thấy tài khoản"));return;}String full=text(body,"full_name"),username=text(body,"username"),role=text(body,"role"),email=text(body,"email"),phone=text(body,"phone");validate(full,username,role);if("quanly".equals(existing.get("role"))&&!"quanly".equals(role)&&dao.countActiveManagers()<=1){writeJson(resp,409,jsonMap("message","Không thể thay đổi quản lý cuối cùng"));return;}dao.update(p.id(),full,username,role,email,phone);writeJson(resp,jsonMap("success",true,"message","Cập nhật tài khoản thành công"));}catch(Exception e){error(resp,e);}}
    @Override protected void doDelete(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        try{Path p=path(req);if(p.id()==null){writeJson(resp,400,jsonMap("message","Thiếu ID tài khoản"));return;}if(currentId(req)==p.id()){writeJson(resp,409,jsonMap("message","Không thể xóa tài khoản đang đăng nhập"));return;}Map<String,Object> user=dao.find(p.id());if(user==null){writeJson(resp,404,jsonMap("message","Không tìm thấy tài khoản"));return;}if("quanly".equals(user.get("role"))&&dao.countActiveManagers()<=1){writeJson(resp,409,jsonMap("message","Không thể xóa quản lý cuối cùng"));return;}dao.delete(p.id());writeJson(resp,jsonMap("success",true,"message","Xóa tài khoản thành công"));}catch(Exception e){error(resp,e);}}
    private void create(HttpServletRequest req,HttpServletResponse resp)throws Exception{Map<String,Object>b=readJson(req);String full=text(b,"full_name"),username=text(b,"username"),password=text(b,"password"),role=text(b,"role"),email=text(b,"email"),phone=text(b,"phone");validate(full,username,role);if(password==null||password.length()<6)throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");int id=dao.create(full,username,PasswordUtil.hash(password),role,email,phone,"active");writeJson(resp,201,jsonMap("success",true,"id",id,"message","Thêm tài khoản thành công"));}
    private void changeStatus(HttpServletRequest req,HttpServletResponse resp,int id,String status)throws Exception{if("locked".equals(status)&&currentId(req)==id){writeJson(resp,409,jsonMap("message","Không thể khóa tài khoản đang đăng nhập"));return;}Map<String,Object>user=dao.find(id);if(user==null){writeJson(resp,404,jsonMap("message","Không tìm thấy tài khoản"));return;}if("locked".equals(status)&&"quanly".equals(user.get("role"))&&dao.countActiveManagers()<=1){writeJson(resp,409,jsonMap("message","Không thể khóa quản lý cuối cùng"));return;}dao.setStatus(id,status);writeJson(resp,jsonMap("success",true,"message","active".equals(status)?"Mở khóa tài khoản thành công":"Khóa tài khoản thành công"));}
    private void reset(HttpServletRequest req,HttpServletResponse resp,int id)throws Exception{Map<String,Object>b=readJson(req);String password=text(b,"password");if(password==null||password.length()<6)throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");if(!dao.resetPassword(id,PasswordUtil.hash(password))){writeJson(resp,404,jsonMap("message","Không tìm thấy tài khoản"));return;}writeJson(resp,jsonMap("success",true,"message","Đặt lại mật khẩu thành công"));}
    private void validate(String full,String username,String role){if(full==null||username==null||role==null)throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin bắt buộc");if(!username.matches("[A-Za-z0-9._-]{3,50}"))throw new IllegalArgumentException("Username phải có 3-50 ký tự hợp lệ");if(!ROLES.contains(role))throw new IllegalArgumentException("Vai trò không hợp lệ");}
    private boolean authorized(HttpServletRequest req,HttpServletResponse resp)throws IOException{HttpSession s=req.getSession(false);if(s==null||s.getAttribute("accountUserId")==null){writeJson(resp,401,jsonMap("message","Vui lòng đăng nhập"));return false;}if(!"quanly".equals(s.getAttribute("accountRole"))){writeJson(resp,403,jsonMap("message","Bạn không có quyền quản lý tài khoản"));return false;}return true;}
    private int currentId(HttpServletRequest req){return ((Number)req.getSession(false).getAttribute("accountUserId")).intValue();}
    private int parse(String value,int fallback){try{return Integer.parseInt(value);}catch(Exception e){return fallback;}}
    private Path path(HttpServletRequest req){String info=req.getPathInfo();if(info==null||info.equals("/"))return new Path(null,null);String[]parts=info.substring(1).split("/");try{return new Path(Integer.valueOf(parts[0]),parts.length>1?parts[1]:null);}catch(Exception e){return new Path(null,"invalid");}}
    private void error(HttpServletResponse resp,Exception e)throws IOException{if(e instanceof IllegalArgumentException){writeJson(resp,400,jsonMap("message",e.getMessage()));return;}if(e instanceof SQLException sql&&sql.getErrorCode()==1062){writeJson(resp,409,jsonMap("message","Username đã tồn tại"));return;}getServletContext().log("Lỗi quản lý tài khoản",e);writeJson(resp,500,jsonMap("message","Không thể xử lý tài khoản"));}
    private record Path(Integer id,String action){}
}
