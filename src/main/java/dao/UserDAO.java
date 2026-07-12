package com.dormitory.dao;

import com.dormitory.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserDAO extends DaoSupport {
    public Map<String,Object> search(String search,String role,String status,int page,int limit)throws SQLException{
        StringBuilder where=new StringBuilder(" WHERE (username LIKE ? OR full_name LIKE ? OR COALESCE(email,'') LIKE ? OR COALESCE(phone,'') LIKE ?)");
        List<Object> params=new ArrayList<>();String term="%"+(search==null?"":search.trim())+"%";for(int i=0;i<4;i++)params.add(term);
        if(role!=null&&!role.isBlank()){where.append(" AND role=?");params.add(role);}if(status!=null&&!status.isBlank()){where.append(" AND status=?");params.add(status);}
        try(Connection c=Database.getConnection()){
            int total;try(PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM users"+where)){bind(ps,params);try(ResultSet rs=ps.executeQuery()){rs.next();total=rs.getInt(1);}}
            List<Object> listParams=new ArrayList<>(params);listParams.add(limit);listParams.add((page-1)*limit);
            List<Map<String,Object>> users;try(PreparedStatement ps=c.prepareStatement("SELECT id,full_name,username,role,email,phone,status,created_at FROM users"+where+" ORDER BY id LIMIT ? OFFSET ?")){bind(ps,listParams);try(ResultSet rs=ps.executeQuery()){users=rows(rs);}}
            Map<String,Object> result=new LinkedHashMap<>();result.put("users",users);result.put("total",total);result.put("page",page);result.put("limit",limit);return result;
        }
    }
    public Map<String,Object> find(int id)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("SELECT id,full_name,username,role,email,phone,status,created_at FROM users WHERE id=?")){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?row(rs):null;}}}
    public int create(String fullName,String username,String hash,String role,String email,String phone,String status)throws SQLException{String sql="INSERT INTO users(full_name,username,password,role,email,phone,status) VALUES(?,?,?,?,?,?,?)";try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){ps.setString(1,fullName);ps.setString(2,username);ps.setString(3,hash);ps.setString(4,role);ps.setString(5,email);ps.setString(6,phone);ps.setString(7,status);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){return rs.next()?rs.getInt(1):0;}}}
    public boolean update(int id,String fullName,String username,String role,String email,String phone)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE users SET full_name=?,username=?,role=?,email=?,phone=? WHERE id=?")){ps.setString(1,fullName);ps.setString(2,username);ps.setString(3,role);ps.setString(4,email);ps.setString(5,phone);ps.setInt(6,id);return ps.executeUpdate()>0;}}
    public boolean setStatus(int id,String status)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE users SET status=? WHERE id=?")){ps.setString(1,status);ps.setInt(2,id);return ps.executeUpdate()>0;}}
    public boolean resetPassword(int id,String hash)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE users SET password=? WHERE id=?")){ps.setString(1,hash);ps.setInt(2,id);return ps.executeUpdate()>0;}}
    public boolean delete(int id)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM users WHERE id=?")){ps.setInt(1,id);return ps.executeUpdate()>0;}}
    public int countActiveManagers()throws SQLException{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM users WHERE role='quanly' AND status='active'");ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}
    private void bind(PreparedStatement ps,List<Object> values)throws SQLException{for(int i=0;i<values.size();i++)ps.setObject(i+1,values.get(i));}
}
