package hostel.service;
import java.sql.*;
public final class DatabaseConnection {
 private DatabaseConnection(){}
 public static Connection getConnection() throws SQLException {
  String url=System.getenv().getOrDefault("HOSTEL_DB_URL","jdbc:mysql://localhost:3306/hostel_reservation?useSSL=false&serverTimezone=Asia/Kolkata");
  String user=System.getenv().getOrDefault("HOSTEL_DB_USER","root");
  String pass=System.getenv().getOrDefault("HOSTEL_DB_PASSWORD","root");
  return DriverManager.getConnection(url,user,pass);
 }
}
