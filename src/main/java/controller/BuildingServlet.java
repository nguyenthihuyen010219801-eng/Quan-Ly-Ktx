package com.dormitory.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import dao.BuildingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Building;

@WebServlet("/buildings")
public class BuildingServlet extends BaseServlet {
    private final BuildingDAO dao=new BuildingDAO();
    @Override protected void service(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{req.setCharacterEncoding("UTF-8");resp.setCharacterEncoding("UTF-8");super.service(req,resp);}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{String action=req.getParameter("action");if(action==null||action.isBlank()){req.getRequestDispatcher("/WEB-INF/views/buildings.jsp").forward(req,resp);return;}try{if("detail".equals(action)){Map<String,Object>d=dao.detail(id(req));if(d==null){writeJson(resp,404,jsonMap("message","Không tìm thấy tòa nhà"));return;}writeJson(resp,d);return;}writeJson(resp,dao.search(keyword(req)));}catch(Exception e){writeJson(resp,500,jsonMap("message",msg(e,"Không tải được dữ liệu tòa nhà")));}}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{save(req,resp,false);}@Override protected void doPut(HttpServletRequest req,HttpServletResponse resp)throws IOException{save(req,resp,true);}
    @Override protected void doDelete(HttpServletRequest req,HttpServletResponse resp)throws IOException{try{dao.delete(id(req));writeJson(resp,jsonMap("success",true,"message","Xóa tòa nhà thành công"));}catch(Exception e){writeJson(resp,409,jsonMap("message",msg(e,"Không thể xóa tòa nhà")));}}
    private void save(HttpServletRequest req,HttpServletResponse resp,boolean edit)throws IOException{try{Map<String,Object>d=readJson(req);Building b=new Building();b.id=edit?id(req):null;b.buildingCode=text(d,"building_code");b.buildingName=text(d,"building_name");b.floors=integer(d,"floors");b.status=text(d,"status");if(b.status==null)b.status="Đang hoạt động";b.note=text(d,"note");if(b.buildingCode==null||b.buildingName==null||b.floors==null||b.floors<1)throw new IllegalArgumentException("Vui lòng nhập mã, tên và số tầng hợp lệ");int id=edit?(dao.update(b)?b.id:0):dao.create(b);if(id==0){writeJson(resp,404,jsonMap("message","Không tìm thấy tòa nhà"));return;}writeJson(resp,edit?200:201,jsonMap("success",true,"id",id,"message",edit?"Cập nhật tòa nhà thành công":"Thêm tòa nhà thành công"));}catch(IllegalArgumentException e){writeJson(resp,400,jsonMap("message",e.getMessage()));}catch(SQLException e){writeJson(resp,400,jsonMap("message",e.getErrorCode()==1062?"Mã tòa nhà đã tồn tại":msg(e,"Không thể lưu tòa nhà")));}}
    private int id(HttpServletRequest r){return Integer.parseInt(r.getParameter("id"));}private String keyword(HttpServletRequest r){String k=r.getParameter("keyword");return k==null?r.getParameter("search"):k;}private String msg(Exception e,String f){return e.getMessage()==null?f:e.getMessage();}
}
