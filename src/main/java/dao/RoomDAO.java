package dao;

import model.Room;
import util.DBConnection;
import java.sql.*;
import java.util.*;

public class RoomDAO extends DaoSupport {
    public List<Map<String, Object>> search(String keyword, Integer buildingId) throws SQLException {
        String sql = "SELECT r.*, b.building_name, b.building_code FROM rooms r LEFT JOIN buildings b ON b.id=r.building_id " +
                "WHERE (r.room_code LIKE ? OR COALESCE(r.room_name,'') LIKE ? OR COALESCE(r.room_type,'') LIKE ? OR COALESCE(b.building_name,'') LIKE ?)" +
                (buildingId == null ? "" : " AND r.building_id=?") + " ORDER BY b.building_code,r.room_code";
        String term = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, term);
            if (buildingId != null) ps.setInt(5, buildingId);
            try (ResultSet rs = ps.executeQuery()) { return rows(rs); }
        }
    }

    public Map<String, Object> detail(int id) throws SQLException {
        String roomSql = "SELECT r.*,b.building_name,b.building_code FROM rooms r LEFT JOIN buildings b ON b.id=r.building_id WHERE r.id=?";
        String tenantSql = "SELECT id,student_code,full_name,phone,email FROM students WHERE room_id=? AND status='Đang ở' ORDER BY full_name";
        try (Connection c = DBConnection.getConnection(); PreparedStatement roomPs = c.prepareStatement(roomSql); PreparedStatement tenantPs = c.prepareStatement(tenantSql)) {
            roomPs.setInt(1, id); tenantPs.setInt(1, id);
            Map<String, Object> room;
            try (ResultSet rs = roomPs.executeQuery()) { if (!rs.next()) return null; room = row(rs); }
            List<Map<String, Object>> tenants;
            try (ResultSet rs = tenantPs.executeQuery()) { tenants = rows(rs); }
            room.put("current_quantity", tenants.size());
            return new LinkedHashMap<>(Map.of("room", room, "tenants", tenants));
        }
    }

    public int create(Room r) throws SQLException {
        String sql = "INSERT INTO rooms(room_code,room_name,building_id,room_type,floor,capacity,current_quantity,price,status) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, r); ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : 0; }
        }
    }

    public boolean update(Room r) throws SQLException {
        String sql = "UPDATE rooms SET room_code=?,room_name=?,building_id=?,room_type=?,floor=?,capacity=?,current_quantity=?,price=?,status=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, r); ps.setInt(10, r.id); return ps.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement ps, Room r) throws SQLException {
        ps.setString(1, r.roomCode); ps.setString(2, r.roomName); ps.setInt(3, r.buildingId); ps.setString(4, r.roomType);
        ps.setInt(5, r.floor); ps.setInt(6, r.capacity); ps.setInt(7, r.currentQuantity); ps.setBigDecimal(8, r.price); ps.setString(9, r.status);
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement check = c.prepareStatement("SELECT COUNT(*) FROM students WHERE room_id=?"); PreparedStatement delete = c.prepareStatement("DELETE FROM rooms WHERE id=?")) {
            check.setInt(1, id); try (ResultSet rs = check.executeQuery()) { rs.next(); if (rs.getInt(1) > 0) throw new SQLException("Không thể xóa phòng đang có sinh viên", "23000"); }
            delete.setInt(1, id); delete.executeUpdate();
        }
    }
}
