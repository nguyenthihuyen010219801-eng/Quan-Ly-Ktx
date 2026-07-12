package com.dormitory.controller;

import com.dormitory.config.Database;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@WebServlet("/api/manage/meta")
public class ManageMetaServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String buildingsSql = "SELECT * FROM buildings ORDER BY building_code, id";
        String roomsSql = """
                SELECT r.id, r.room_code, r.room_name, r.capacity, r.current_quantity, r.status,
                       b.id AS building_id, b.building_code, b.building_name
                FROM rooms r
                LEFT JOIN buildings b ON r.building_id = b.id
                ORDER BY b.building_code, r.room_code
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement buildingsStatement = connection.prepareStatement(buildingsSql);
             PreparedStatement roomsStatement = connection.prepareStatement(roomsSql);
             ResultSet buildings = buildingsStatement.executeQuery();
             ResultSet rooms = roomsStatement.executeQuery()) {

            Map<String, Object> payload = jsonMap(
                    "buildings", rows(buildings),
                    "rooms", rows(rooms)
            );
            writeJson(response, payload);
        } catch (Exception e) {
            writeJson(response, 500, jsonMap("message", "Không tải được dữ liệu tòa nhà/phòng"));
        }
    }
}
