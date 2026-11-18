package com.qwikbite.dao;

import com.qwikbite.config.Database;
import com.qwikbite.model.Session;
import java.sql.*;
import java.util.*;

public class AuthDAO {
    public List<String> foodCourts() throws SQLException {
        List<String> rows=new ArrayList<>();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT court_name FROM food_courts ORDER BY court_name");ResultSet r=p.executeQuery()) { while(r.next()) rows.add(r.getString(1)); }
        return rows;
    }
    public List<OrderDAO.Shop> shopsForCourt(String court) throws SQLException {
        List<OrderDAO.Shop> rows=new ArrayList<>();
        String sql="SELECT s.id,s.shop_name FROM shops s JOIN food_courts f ON f.id=s.court_id WHERE f.court_name=? ORDER BY s.shop_name";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,court);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new OrderDAO.Shop(r.getInt(1),r.getString(2)));}}
        return rows;
    }
    public void register(String regNo,String name,String phone,String password,String accountType)throws SQLException{
        String sql="INSERT INTO students(register_number,full_name,phone,password_hash,account_type) VALUES(?,?,?,?,?)";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,regNo.trim());p.setString(2,name.trim());p.setString(3,phone.trim());p.setString(4,password);p.setString(5,accountType);p.executeUpdate();}
    }
    public void registerBiteMate(String name,String username,String password)throws SQLException{
        String sql="INSERT INTO bitemates(full_name,username,password_hash) VALUES(?,?,?)";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,name.trim());p.setString(2,username.trim());p.setString(3,password);p.executeUpdate();}
    }
    // The selected seeded outlet is the shop-counter identity; no separate username is needed.
    public void registerShop(int shopId,String password)throws SQLException{
        String sql="UPDATE shops SET username=shop_name,password_hash=? WHERE id=?";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,password);p.setInt(2,shopId);if(p.executeUpdate()!=1)throw new SQLException("Selected outlet was not found.");}
    }
    public Session login(String role,String username,String password)throws SQLException{return login(role,username,password,-1);}
    public Session login(String role,String username,String password,int shopId)throws SQLException{
        if("STUDENT".equals(role)||"FACULTY".equals(role)||"STAFF".equals(role)){
            String sql="SELECT id,full_name,register_number,account_type FROM students WHERE register_number=? AND password_hash=? AND account_type=? AND active=1";
            try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,username.trim());p.setString(2,password);p.setString(3,role);try(ResultSet r=p.executeQuery()){if(r.next())return new Session(r.getInt("id"),r.getString("full_name"),role,r.getString("register_number"));}}
        }else if("SHOP".equals(role)){
            String sql="SELECT id,shop_name FROM shops WHERE id=? AND username=shop_name AND password_hash=?";
            try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,shopId);p.setString(2,password);try(ResultSet r=p.executeQuery()){if(r.next())return new Session(r.getInt("id"),r.getString("shop_name"),role,Integer.toString(shopId));}}
        }else if("BITEMATE".equals(role)){
            String sql="SELECT id,full_name FROM bitemates WHERE username=? AND password_hash=? AND active=1";
            try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,username.trim());p.setString(2,password);try(ResultSet r=p.executeQuery()){if(r.next())return new Session(r.getInt("id"),r.getString("full_name"),role,username);}}
        }
        return null;
    }
}
