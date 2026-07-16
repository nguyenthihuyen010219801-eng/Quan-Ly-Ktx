package dao;

import config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsDAO {
    public Map<String,String> getAll()throws SQLException{Map<String,String> result=new LinkedHashMap<>();try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("SELECT setting_key,setting_value FROM system_settings ORDER BY setting_key");ResultSet rs=ps.executeQuery()){while(rs.next())result.put(rs.getString(1),rs.getString(2));}return result;}
    public void save(Map<String,String> values)throws SQLException{try(Connection c=Database.getConnection()){c.setAutoCommit(false);try(PreparedStatement ps=c.prepareStatement("INSERT INTO system_settings(setting_key,setting_value) VALUES(?,?) ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)")){for(Map.Entry<String,String> e:values.entrySet()){ps.setString(1,e.getKey());ps.setString(2,e.getValue());ps.addBatch();}ps.executeBatch();c.commit();}catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}}}
    public String passwordHash(int userId)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("SELECT password FROM users WHERE id=? AND status='active'")){ps.setInt(1,userId);try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getString(1):null;}}}
    public boolean changePassword(int userId,String hash)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE users SET password=? WHERE id=? AND status='active'")){ps.setString(1,hash);ps.setInt(2,userId);return ps.executeUpdate()>0;}}
}
