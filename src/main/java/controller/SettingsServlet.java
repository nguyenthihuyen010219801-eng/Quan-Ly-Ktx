package com.dormitory.controller;

import com.dormitory.dao.SettingsDAO;
import com.dormitory.util.PasswordUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@WebServlet(urlPatterns={"/api/settings","/api/settings/*"})
public class SettingsServlet extends BaseServlet {
    private static final Set<String> KEYS=Set.of("system_name","dormitory_name","address","email","phone","footer","announcement");
    private final SettingsDAO dao=new SettingsDAO();
    @Override protected void service(HttpServletRequest req,HttpServletResponse resp)throws jakarta.servlet.ServletException,IOException{req.setCharacterEncoding("UTF-8");resp.setCharacterEncoding("UTF-8");boolean ownPassword="POST".equals(req.getMethod())&&"/password".equals(req.getPathInfo());if(!ownPassword&&!authorized(req,resp))return;super.service(req,resp);}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{try{writeJson(resp,dao.getAll());}catch(Exception e){fail(resp,e);}}
    @Override protected void doPut(HttpServletRequest req,HttpServletResponse resp)throws IOException{try{Map<String,Object> body=readJson(req);Map<String,String> values=new LinkedHashMap<>();for(String key:KEYS){if(body.containsKey(key)){String value=text(body,key);values.put(key,value==null?"":value);}}if(values.isEmpty())throw new IllegalArgumentException("Không có cấu hình hợp lệ");validate(values);dao.save(values);writeJson(resp,jsonMap("success",true,"message","Lưu cài đặt thành công"));}catch(Exception e){fail(resp,e);}}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{if(!"/password".equals(req.getPathInfo())){writeJson(resp,404,jsonMap("message","Đường dẫn không hợp lệ"));return;}try{Map<String,Object> body=readJson(req);String oldPassword=text(body,"old_password"),newPassword=text(body,"new_password"),confirm=text(body,"confirm_password");if(oldPassword==null||newPassword==null||confirm==null)throw new IllegalArgumentException("Vui lòng nhập đầy đủ mật khẩu");if(newPassword.length()<6)throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");if(!newPassword.equals(confirm))throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");int userId=((Number)req.getSession(false).getAttribute("accountUserId")).intValue();String hash=dao.passwordHash(userId);if(!PasswordUtil.verify(oldPassword,hash)){writeJson(resp,400,jsonMap("message","Mật khẩu hiện tại không đúng"));return;}dao.changePassword(userId,PasswordUtil.hash(newPassword));writeJson(resp,jsonMap("success",true,"message","Đổi mật khẩu thành công"));}catch(Exception e){fail(resp,e);}}
    private void validate(Map<String,String> v){if(v.containsKey("system_name")&&v.get("system_name").isBlank())throw new IllegalArgumentException("Tên hệ thống không được để trống");if(v.containsKey("dormitory_name")&&v.get("dormitory_name").isBlank())throw new IllegalArgumentException("Tên ký túc xá không được để trống");String email=v.get("email");if(email!=null&&!email.isBlank()&&!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))throw new IllegalArgumentException("Email không hợp lệ");for(Map.Entry<String,String> e:v.entrySet())if(e.getValue().length()>2000)throw new IllegalArgumentException("Nội dung cấu hình quá dài");}
    private boolean authorized(HttpServletRequest req,HttpServletResponse resp)throws IOException{HttpSession s=req.getSession(false);if(s==null||s.getAttribute("accountUserId")==null){writeJson(resp,401,jsonMap("message","Vui lòng đăng nhập"));return false;}if(!"quanly".equals(s.getAttribute("accountRole"))){writeJson(resp,403,jsonMap("message","Bạn không có quyền thay đổi cài đặt"));return false;}return true;}
    private void fail(HttpServletResponse resp,Exception e)throws IOException{if(e instanceof IllegalArgumentException){writeJson(resp,400,jsonMap("message",e.getMessage()));return;}getServletContext().log("Lỗi cài đặt hệ thống",e);writeJson(resp,500,jsonMap("message","Không thể xử lý cài đặt hệ thống"));}
}
