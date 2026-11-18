package com.qwikbite.dao;

import com.qwikbite.config.Database;
import java.sql.*;
import java.util.*;

public class OrderDAO {
    public record Shop(int id,String name) { @Override public String toString(){ return name; } }
    public record MenuItem(int id,String name,String description,double price) { }
    public record CartItem(int menuItemId,int quantity) { }
    public record InventoryItem(int id,String name,String description,double price,int stock,boolean available) { }
    public List<Shop> shops(String court) throws SQLException {
        List<Shop> out=new ArrayList<>(); String sql="SELECT s.id,s.shop_name FROM shops s JOIN food_courts f ON f.id=s.court_id WHERE f.court_name=? AND s.is_open=1 ORDER BY s.shop_name";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,court);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new Shop(r.getInt(1),r.getString(2)));}} return out;
    }
    public List<MenuItem> menuForShop(int shopId) throws SQLException {
        List<MenuItem> out=new ArrayList<>();String sql="SELECT id,item_name,COALESCE(description,'') AS description,price FROM menu_items WHERE shop_id=? AND available=1 AND stock_qty>0 ORDER BY item_name";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,shopId);try(ResultSet r=p.executeQuery()){while(r.next())out.add(new MenuItem(r.getInt(1),r.getString(2),r.getString(3),r.getDouble(4)));}}return out;
    }
    public int placeOrder(int studentId,List<CartItem> cart,String building,int floor,String room,String paymentMethod) throws SQLException {
        if(cart==null||cart.isEmpty())throw new IllegalArgumentException("Your cart is empty.");
        if(!"CASH_ON_DELIVERY".equals(paymentMethod)&&!"UPI_ON_DELIVERY".equals(paymentMethod))throw new IllegalArgumentException("Choose cash or UPI payment at delivery.");
        if(floor<0||room==null||room.isBlank())throw new IllegalArgumentException("Enter a valid floor and room number.");
        try(Connection c=Database.getConnection()){
            c.setAutoCommit(false);
            try {
                Map<Integer,Integer> quantities=new LinkedHashMap<>();
                for(CartItem item:cart){if(item.quantity()<1)throw new IllegalArgumentException("Invalid cart quantity.");quantities.merge(item.menuItemId(),item.quantity(),Integer::sum);}
                int shopId=-1;double subtotal=0;Map<Integer,MenuItem> selected=new LinkedHashMap<>();
                String lookup="SELECT id,item_name,COALESCE(description,'') AS description,price,shop_id,stock_qty FROM menu_items WHERE id=? AND available=1";
                try(PreparedStatement p=c.prepareStatement(lookup)){
                    for(int itemId:quantities.keySet()){
                        p.setInt(1,itemId);try(ResultSet r=p.executeQuery()){
                            if(!r.next())throw new SQLException("A cart item is no longer available. Refresh the menu.");
                            int currentShop=r.getInt("shop_id");if(shopId==-1)shopId=currentShop;else if(shopId!=currentShop)throw new SQLException("A single order can contain items from only one outlet. Clear the cart before switching outlets.");
                            int stock=r.getInt("stock_qty");int requested=quantities.get(itemId);if(requested>stock)throw new SQLException("Insufficient stock for "+r.getString("item_name")+". Available: "+stock);MenuItem item=new MenuItem(r.getInt("id"),r.getString("item_name"),r.getString("description"),r.getDouble("price"));selected.put(itemId,item);subtotal+=item.price()*requested;
                        }
                    }
                }
                double fee=floor<=4?10:10+(floor-4);double total=subtotal+fee;int orderId;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO orders(student_id,shop_id,building,floor_number,room_number,subtotal,delivery_fee,total_amount,status) VALUES(?,?,?,?,?,?,?,?,'PLACED')",Statement.RETURN_GENERATED_KEYS)){
                    p.setInt(1,studentId);p.setInt(2,shopId);p.setString(3,building);p.setInt(4,floor);p.setString(5,room.trim());p.setDouble(6,subtotal);p.setDouble(7,fee);p.setDouble(8,total);p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Could not create order.");orderId=r.getInt(1);}
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO order_items(order_id,menu_item_id,item_name,unit_price,quantity) VALUES(?,?,?,?,?)")){
                    for(var entry:quantities.entrySet()){MenuItem item=selected.get(entry.getKey());p.setInt(1,orderId);p.setInt(2,item.id());p.setString(3,item.name());p.setDouble(4,item.price());p.setInt(5,entry.getValue());p.addBatch();}p.executeBatch();
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE menu_items SET stock_qty=stock_qty-? WHERE id=? AND stock_qty>=?")){for(var entry:quantities.entrySet()){p.setInt(1,entry.getValue());p.setInt(2,entry.getKey());p.setInt(3,entry.getValue());p.addBatch();}int[] changed=p.executeBatch();for(int n:changed)if(n==0)throw new SQLException("Stock changed while placing the order. Please retry.");}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO payments(order_id,method,status,amount) VALUES(?,?,'PENDING',?)")){p.setInt(1,orderId);p.setString(2,paymentMethod);p.setDouble(3,total);p.executeUpdate();}
                c.commit();return orderId;
            }catch(Exception e){try{c.rollback();}catch(SQLException x){e.addSuppressed(x);}if(e instanceof SQLException x)throw x;if(e instanceof RuntimeException x)throw x;throw new SQLException(e);}
            finally{try{c.setAutoCommit(true);}catch(SQLException ignored){}}
        }
    }
    public List<Object[]> history(int studentId) throws SQLException {
        List<Object[]> rows=new ArrayList<>();String sql="SELECT o.id,s.shop_name,GROUP_CONCAT(CONCAT(oi.item_name,' x',oi.quantity) SEPARATOR ', '),SUM(oi.quantity),o.building,o.floor_number,o.room_number,o.total_amount,o.status,o.created_at FROM orders o JOIN shops s ON s.id=o.shop_id JOIN order_items oi ON oi.order_id=o.id WHERE o.student_id=? GROUP BY o.id,s.shop_name ORDER BY o.created_at DESC";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,studentId);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getInt(4),r.getString(5),r.getInt(6),r.getString(7),r.getDouble(8),r.getString(9),r.getTimestamp(10)});}}return rows;
    }
    public List<Object[]> ordersForShop(int shopId) throws SQLException {List<Object[]> rows=new ArrayList<>();String sql="SELECT id,building,floor_number,room_number,total_amount,status,created_at FROM orders WHERE shop_id=? ORDER BY created_at DESC";try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,shopId);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new Object[]{r.getInt(1),r.getString(2),r.getInt(3),r.getString(4),r.getDouble(5),r.getString(6),r.getTimestamp(7)});}}return rows;}
    public void updateOrderStatus(int id,String status)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("UPDATE orders SET status=? WHERE id=?")){p.setString(1,status);p.setInt(2,id);if(p.executeUpdate()==0)throw new SQLException("Order not found.");}}
    public List<Object[]> deliveryPool(int mateId)throws SQLException{
        List<Object[]> rows=new ArrayList<>();
        String sql="SELECT o.id,s.shop_name,o.building,o.floor_number,o.room_number,o.total_amount,o.status,o.created_at FROM orders o JOIN shops s ON s.id=o.shop_id WHERE o.delivery_id IS NULL AND o.status IN ('PLACED','ACCEPTED','READY') AND NOT EXISTS(SELECT 1 FROM delivery_declines d WHERE d.order_id=o.id AND d.mate_id=?) ORDER BY o.created_at";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,mateId);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getInt(4),r.getString(5),r.getDouble(6),r.getString(7),r.getTimestamp(8)});}}return rows;
    }
    public String deliveryDetails(int orderId)throws SQLException{
        StringBuilder b=new StringBuilder();
        String head="SELECT o.id,o.building,o.floor_number,o.room_number,o.subtotal,o.delivery_fee,o.total_amount,o.status,o.created_at,s.shop_name,st.full_name,st.phone,pay.method FROM orders o JOIN shops s ON s.id=o.shop_id JOIN students st ON st.id=o.student_id LEFT JOIN payments pay ON pay.order_id=o.id WHERE o.id=?";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(head)){p.setInt(1,orderId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Order not found.");b.append("ORDER #").append(r.getInt(1)).append("\\nOutlet: ").append(r.getString(10)).append("\\nCustomer: ").append(r.getString(11)).append("\\nPhone: ").append(r.getString(12)).append("\\nPayment: ").append(r.getString(13)).append("\\nDelivery: ").append(r.getString(2)).append(" · Floor ").append(r.getInt(3)).append(" · Room ").append(r.getString(4)).append("\\nShop status: ").append(r.getString(8)).append("\\n\\nITEMS\\n");b.append(String.format("Food subtotal: ₹%.2f\\nDelivery fee: ₹%.2f\\nTOTAL: ₹%.2f",r.getDouble(5),r.getDouble(6),r.getDouble(7)));}}
        String items="SELECT item_name,quantity,unit_price FROM order_items WHERE order_id=? ORDER BY id";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(items)){p.setInt(1,orderId);try(ResultSet r=p.executeQuery()){while(r.next())b.insert(b.indexOf("\\n\\nITEMS")+8,r.getString(1)+" x"+r.getInt(2)+"   ₹"+String.format("%.2f",r.getDouble(3)*r.getInt(2))+"\\n");}}
        return b.toString().replace("\\n","\n");
    }
    public void declineDelivery(int orderId,int mateId)throws SQLException{
        String sql="INSERT IGNORE INTO delivery_declines(order_id,mate_id) SELECT id,? FROM orders WHERE id=? AND delivery_id IS NULL AND status IN ('PLACED','ACCEPTED','READY')";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,mateId);p.setInt(2,orderId);if(p.executeUpdate()!=1)throw new SQLException("This order is no longer available.");}
    }
    public void claimDelivery(int orderId,int mateId)throws SQLException{
        String sql="UPDATE orders SET delivery_id=?,delivery_status='ACCEPTED' WHERE id=? AND status IN ('PLACED','ACCEPTED','READY') AND delivery_id IS NULL";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,mateId);p.setInt(2,orderId);if(p.executeUpdate()!=1)throw new SQLException("This order was already accepted by another BiteMate. Refresh the list.");}
    }

    public List<InventoryItem> inventoryForShop(int shopId)throws SQLException{
        List<InventoryItem> rows=new ArrayList<>();String sql="SELECT id,item_name,COALESCE(description,''),price,stock_qty,available FROM menu_items WHERE shop_id=? ORDER BY item_name";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,shopId);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new InventoryItem(r.getInt(1),r.getString(2),r.getString(3),r.getDouble(4),r.getInt(5),r.getBoolean(6)));}}return rows;
    }
    public void saveInventoryItem(int shopId,int id,String name,String description,double price,int stock,boolean available)throws SQLException{
        if(name==null||name.isBlank()||price<0||stock<0)throw new IllegalArgumentException("Enter a name, non-negative price and stock quantity.");
        if(id<=0){String sql="INSERT INTO menu_items(shop_id,item_name,description,price,stock_qty,available) VALUES(?,?,?,?,?,?)";try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,shopId);p.setString(2,name.trim());p.setString(3,description==null?"":description.trim());p.setDouble(4,price);p.setInt(5,stock);p.setBoolean(6,available);p.executeUpdate();}}
        else{String sql="UPDATE menu_items SET item_name=?,description=?,price=?,stock_qty=?,available=? WHERE id=? AND shop_id=?";try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,name.trim());p.setString(2,description==null?"":description.trim());p.setDouble(3,price);p.setInt(4,stock);p.setBoolean(5,available);p.setInt(6,id);p.setInt(7,shopId);if(p.executeUpdate()!=1)throw new SQLException("Menu item not found for this shop.");}}
    }
    public List<Object[]> assignedDeliveries(int mateId)throws SQLException{
        List<Object[]> rows=new ArrayList<>();String sql="SELECT o.id,s.shop_name,o.building,o.floor_number,o.room_number,o.total_amount,o.delivery_status,o.created_at,o.status FROM orders o JOIN shops s ON s.id=o.shop_id WHERE o.delivery_id=? ORDER BY o.created_at DESC";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,mateId);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new Object[]{r.getInt(1),r.getString(2),r.getString(3),r.getInt(4),r.getString(5),r.getDouble(6),r.getString(7),r.getTimestamp(8),r.getString(9)});}}return rows;
    }
    public void updateDeliveryStatus(int orderId,int mateId,String status)throws SQLException{
        Map<String,String> transitions=Map.of("ON_WAY_TO_PICKUP","ACCEPTED","PICKED_UP","ON_WAY_TO_PICKUP","OUT_FOR_DELIVERY","PICKED_UP");
        String expected=transitions.get(status);if(expected==null)throw new IllegalArgumentException("Invalid delivery status.");
        String sql="UPDATE orders SET delivery_status=? WHERE id=? AND delivery_id=? AND delivery_status=?";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,status);p.setInt(2,orderId);p.setInt(3,mateId);p.setString(4,expected);if(p.executeUpdate()!=1)throw new SQLException("Complete the previous delivery step first, then refresh your history.");}
    }
    public void markDeliveredWithPhoto(int orderId,int mateId,byte[] photo,String fileName)throws SQLException{
        if(photo==null||photo.length==0)throw new IllegalArgumentException("Upload a delivery photo before marking this order delivered.");
        String sql="UPDATE orders SET delivery_status='DELIVERED',delivery_photo=?,delivery_photo_name=? WHERE id=? AND delivery_id=? AND delivery_status='OUT_FOR_DELIVERY'";
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setBytes(1,photo);p.setString(2,fileName);p.setInt(3,orderId);p.setInt(4,mateId);if(p.executeUpdate()!=1)throw new SQLException("Order must be marked OUT_FOR_DELIVERY before delivery proof can be submitted.");}
    }
    public void setShopOpen(int shopId,boolean open)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("UPDATE shops SET is_open=? WHERE id=?")){p.setBoolean(1,open);p.setInt(2,shopId);p.executeUpdate();}}
}
