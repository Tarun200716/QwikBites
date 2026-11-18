package com.qwikbite.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/qwikbite?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    private Database() { }

    public static Connection getConnection() throws SQLException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver is missing. Run setup.bat first.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean isAvailable() {
        try (Connection ignored = getConnection()) { return true; }
        catch (SQLException e) { return false; }
    }
}
