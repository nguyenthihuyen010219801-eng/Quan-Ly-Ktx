package com.dormitory.util;

import com.dormitory.config.Database;
import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnection {
    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return Database.getConnection();
    }
}
