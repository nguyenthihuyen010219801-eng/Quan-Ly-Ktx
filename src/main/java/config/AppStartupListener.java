package com.dormito.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        DatabaseInitializer.run(event.getServletContext());
        AccountSchemaMigration.run(event.getServletContext());
        SettingsSchemaMigration.run(event.getServletContext());
        PasswordHashMigration.run(event.getServletContext());
        ManagementSchemaMigration.run(event.getServletContext());
        ServiceSchemaMigration.run(event.getServletContext());
        RequestFeedbackSchemaMigration.run(event.getServletContext());
        InvoiceSchemaMigration.run(event.getServletContext());
        FinanceSchemaMigration.run(event.getServletContext());
    }
}
