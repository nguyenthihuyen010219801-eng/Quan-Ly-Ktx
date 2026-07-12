package com.dormitory.dao;

import com.dormitory.model.Service;
import com.dormitory.util.DBConnection;
import java.sql.*;
import java.util.*;

public class ServiceDAO extends DaoSupport {
    public List<Map<String,Object>> search(String keyword) throws SQLException {
        String sql="SELECT id,service_code,service_name,service_type,unit,unit_price,note,status,icon,color,usage_count FROM services WHERE service_code LIKE ? OR service_name LIKE ? OR COALESCE(note,'') LIKE ? ORDER BY service_code";
        String term="%"+(keyword==null?"":keyword.trim())+"%";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setString(1,term);ps.setString(2,term);ps.setString(3,term);try(ResultSet rs=ps.executeQuery()){return rows(rs);}}
    }
    public Map<String,Object> detail(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT id,service_code,service_name,service_type,unit,unit_price,note,status,icon,color,usage_count FROM services WHERE id=?")){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){if(!rs.next())return null;Map<String,Object> result=new LinkedHashMap<>();result.put("service",row(rs));result.put("usage",List.of());return result;}}
    }
    public int create(Service s)throws SQLException{String sql="INSERT INTO services(service_code,service_name,service_type,unit,unit_price,status,note,icon,color,usage_count) VALUES(?,?,?,?,?,?,?,?,?,?)";try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){bind(ps,s);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){return rs.next()?rs.getInt(1):0;}}}
    public boolean update(Service s)throws SQLException{String sql="UPDATE services SET service_code=?,service_name=?,service_type=?,unit=?,unit_price=?,status=?,note=?,icon=?,color=?,usage_count=? WHERE id=?";try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){bind(ps,s);ps.setInt(11,s.id);return ps.executeUpdate()>0;}}
    private void bind(PreparedStatement ps,Service s)throws SQLException{ps.setString(1,s.serviceCode);ps.setString(2,s.serviceName);ps.setString(3,s.serviceType);ps.setString(4,s.unit);ps.setBigDecimal(5,s.unitPrice);ps.setString(6,s.status);ps.setString(7,s.description);ps.setString(8,s.icon);ps.setString(9,s.color);ps.setInt(10,s.usageCount==null?0:s.usageCount);}
    public void delete(int id)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM services WHERE id=?")){ps.setInt(1,id);ps.executeUpdate();}}
}
