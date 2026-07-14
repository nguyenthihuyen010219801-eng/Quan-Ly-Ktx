package com.dormitory.dao;

import com.dormitory.model.Building;
import com.dormitory.util.DBConnection;
import java.sql.*;
import java.util.*;

public class BuildingDAO extends DaoSupport {
    public List<Map<String, Object>> search(String keyword) throws SQLException {
        String sql = "SELECT b.*,COUNT(r.id) room_count FROM buildings b LEFT JOIN rooms r ON r.building_id=b.id " +
                "WHERE b.building_code LIKE ? OR b.building_name LIKE ? OR COALESCE(b.note,'') LIKE ? GROUP BY b.id ORDER BY b.building_code";
        String term = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, term); ps.setString(2, term); ps.setString(3, term);
            try (ResultSet rs = ps.executeQuery()) { return rows(rs); }
        }
    }

    public Map<String, Object> detail(int id) throws SQLException {
        String buildingSql = "SELECT b.*,COUNT(r.id) room_count FROM buildings b LEFT JOIN rooms r ON r.building_id=b.id WHERE b.id=? GROUP BY b.id";
        String roomsSql = "SELECT r.*,b.building_name,b.building_code FROM rooms r JOIN buildings b ON b.id=r.building_id WHERE r.building_id=? ORDER BY r.floor,r.room_code";
        String tenantsSql = "SELECT id,student_code,full_name,phone,email,room_id FROM students WHERE room_id=? AND status='Đang ở' ORDER BY full_name";
        try (Connection c = DBConnection.getConnection(); PreparedStatement bps = c.prepareStatement(buildingSql); PreparedStatement rps = c.prepareStatement(roomsSql); PreparedStatement tps = c.prepareStatement(tenantsSql)) {
            bps.setInt(1, id); Map<String, Object> building;
            try (ResultSet rs = bps.executeQuery()) { if (!rs.next()) return null; building = row(rs); }
            rps.setInt(1, id); List<Map<String, Object>> rooms;
            try (ResultSet rs = rps.executeQuery()) { rooms = rows(rs); }
            int available = 0, full = 0, maintenance = 0;
            for (Map<String, Object> room : rooms) {
                int roomId = ((Number) room.get("id")).intValue(); tps.setInt(1, roomId);
                List<Map<String, Object>> tenants; try (ResultSet rs = tps.executeQuery()) { tenants = rows(rs); }
                room.put("tenants", tenants); room.put("current_quantity", tenants.size());
                String status = String.valueOf(room.get("status"));
                int capacity = ((Number) room.get("capacity")).intValue();
                if (status.contains("Bảo trì")) maintenance++; else if (tenants.size() >= capacity) full++; else available++;
            }
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("total_rooms", rooms.size()); stats.put("available_rooms", available); stats.put("full_rooms", full); stats.put("maintenance_rooms", maintenance);
            Map<String, Object> result = new LinkedHashMap<>(); result.put("building", building); result.put("stats", stats); result.put("rooms", rooms); return result;
        }
    }

    public int create(Building b) throws SQLException {
        String sql = "INSERT INTO buildings(building_code,building_name,floors,status,note) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, b); ps.executeUpdate(); try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : 0; }
        }
    }
    public boolean update(Building b) throws SQLException {
        String sql = "UPDATE buildings SET building_code=?,building_name=?,floors=?,status=?,note=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { bind(ps,b); ps.setInt(6,b.id); return ps.executeUpdate()>0; }
    }
    private void bind(PreparedStatement ps, Building b) throws SQLException { ps.setString(1,b.buildingCode); ps.setString(2,b.buildingName); ps.setInt(3,b.floors); ps.setString(4,b.status); ps.setString(5,b.note); }
    public void delete(int id) throws SQLException {
        try (Connection c=DBConnection.getConnection(); PreparedStatement check=c.prepareStatement("SELECT COUNT(*) FROM rooms WHERE building_id=?"); PreparedStatement delete=c.prepareStatement("DELETE FROM buildings WHERE id=?")) {
            check.setInt(1,id); try(ResultSet rs=check.executeQuery()){rs.next();if(rs.getInt(1)>0)throw new SQLException("Không thể xóa tòa nhà đang có phòng","23000");}
            delete.setInt(1,id); delete.executeUpdate();
        }
    }
}
