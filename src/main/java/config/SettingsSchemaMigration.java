package java.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;

public final class SettingsSchemaMigration {
    private SettingsSchemaMigration() {}

    public static void run(ServletContext context) {
        String create="""
                CREATE TABLE IF NOT EXISTS system_settings (
                  setting_key VARCHAR(100) PRIMARY KEY,
                  setting_value TEXT NULL,
                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
                """;
        Map<String,String> defaults=new LinkedHashMap<>();
        defaults.put("system_name","Hệ thống quản lý ký túc xá");defaults.put("dormitory_name","Ký túc xá");
        defaults.put("address","");defaults.put("email","");defaults.put("phone","");
        defaults.put("footer","Hệ thống quản lý ký túc xá");defaults.put("announcement","");
        try(Connection c=Database.getConnection();PreparedStatement createStatement=c.prepareStatement(create)){
            createStatement.executeUpdate();
            try(PreparedStatement seed=c.prepareStatement("INSERT IGNORE INTO system_settings(setting_key,setting_value) VALUES(?,?)")){
                for(Map.Entry<String,String> entry:defaults.entrySet()){seed.setString(1,entry.getKey());seed.setString(2,entry.getValue());seed.addBatch();}seed.executeBatch();
            }
            context.log("Đã chuẩn bị schema cài đặt hệ thống");
        }catch(Exception e){context.log("Không thể chuẩn bị schema cài đặt hệ thống",e);}
    }
}
