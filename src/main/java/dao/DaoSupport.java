package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

abstract class DaoSupport {
    protected Map<String, Object> row(ResultSet rs) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            Object value = rs.getObject(i);
            if (value instanceof BigDecimal) value = ((BigDecimal) value).doubleValue();
            if (value instanceof java.sql.Date) value = value.toString();
            if (value instanceof Timestamp) value = ((Timestamp) value).toLocalDateTime().toString();
            if (value instanceof LocalDate || value instanceof LocalDateTime || value instanceof LocalTime) value = value.toString();
            result.put(meta.getColumnLabel(i), value);
        }
        return result;
    }

    protected List<Map<String, Object>> rows(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        while (rs.next()) result.add(row(rs));
        return result;
    }
}
