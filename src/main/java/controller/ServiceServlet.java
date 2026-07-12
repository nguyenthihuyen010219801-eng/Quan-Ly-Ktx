package com.dormitory.controller;

import com.dormitory.dao.ServiceDAO;
import com.dormitory.model.Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/services")
public class ServiceServlet extends BaseServlet {
    private final ServiceDAO dao=new ServiceDAO();
    @Override protected void service(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{req.setCharacterEncoding("UTF-8");resp.setCharacterEncoding("UTF-8");super.service(req,resp);}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{String action=req.getParameter("action");if(action==null||action.isBlank()){req.getRequestDispatcher("/WEB-INF/views/services.jsp").forward(req,resp);return;}try{if("detail".equals(action)){Map<String,Object>d=dao.detail(id(req));if(d==null){writeJson(resp,404,jsonMap("message","Không tìm thấy dịch vụ"));return;}writeJson(resp,d);return;}writeJson(resp,dao.search(keyword(req)));}catch(Exception e){writeJson(resp,500,jsonMap("message",msg(e,"Không tải được dữ liệu dịch vụ")));}}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{save(req,resp,false);}@Override protected void doPut(HttpServletRequest req,HttpServletResponse resp)throws IOException{save(req,resp,true);}
    @Override protected void doDelete(HttpServletRequest req,HttpServletResponse resp)throws IOException{try{dao.delete(id(req));writeJson(resp,jsonMap("success",true,"message","Xóa dịch vụ thành công"));}catch(Exception e){writeJson(resp,500,jsonMap("message",msg(e,"Không thể xóa dịch vụ")));}}
    private void save(HttpServletRequest req,HttpServletResponse resp,boolean edit)throws IOException{try{Map<String,Object>d=readJson(req);Service s=new Service();s.id=edit?id(req):null;s.serviceCode=text(d,"service_code");s.serviceName=text(d,"service_name");s.serviceType=text(d,"service_type");if(s.serviceType==null)s.serviceType="Khác";s.unit=text(d,"unit");s.unitPrice=decimal(d.get("unit_price"));s.status=text(d,"status");if(s.status==null)s.status="Đang hoạt động";s.description=text(d,"note");s.icon=text(d,"icon");if(s.icon==null)s.icon="fa-box";s.color=text(d,"color");if(s.color==null)s.color="blue";s.usageCount=integer(d.get("usage_count"));if(s.serviceCode==null||s.serviceName==null||s.unit==null||s.unitPrice==null||s.unitPrice.signum()<0)throw new IllegalArgumentException("Vui lòng nhập đủ mã, tên, đơn giá và đơn vị tính hợp lệ");int id=edit?(dao.update(s)?s.id:0):dao.create(s);if(id==0){writeJson(resp,404,jsonMap("message","Không tìm thấy dịch vụ"));return;}writeJson(resp,edit?200:201,jsonMap("success",true,"id",id,"message",edit?"Cập nhật dịch vụ thành công":"Thêm dịch vụ thành công"));}catch(IllegalArgumentException e){writeJson(resp,400,jsonMap("message",e.getMessage()));}catch(SQLException e){writeJson(resp,400,jsonMap("message",e.getErrorCode()==1062?"Mã dịch vụ đã tồn tại":msg(e,"Không thể lưu dịch vụ")));}}
    private int id(HttpServletRequest r){return Integer.parseInt(r.getParameter("id"));}private String keyword(HttpServletRequest r){String k=r.getParameter("keyword");return k==null?r.getParameter("search"):k;}private BigDecimal decimal(Object o){try{return o==null?null:new BigDecimal(String.valueOf(o));}catch(Exception e){return null;}}private Integer integer(Object o){try{return o==null?0:Integer.valueOf(String.valueOf(o));}catch(Exception e){return 0;}}private String msg(Exception e,String f){return e.getMessage()==null?f:e.getMessage();}
}
