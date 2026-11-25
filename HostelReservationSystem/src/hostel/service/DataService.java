package hostel.service;
import hostel.model.*;
import java.sql.*; import java.time.LocalDate; import java.util.*;

/** JDBC/MySQL persistence layer used by the original Swing screens. */
public final class DataService {
 private static final DataService INSTANCE=new DataService(); public static DataService get(){return INSTANCE;}
 private DataService(){initialize();}
 private Connection conn(){try{return DatabaseConnection.getConnection();}catch(SQLException e){throw error(e);}}
 private RuntimeException error(Exception e){return new IllegalStateException("Database error: "+e.getMessage()+". Check MySQL is running and src/db.properties / environment settings.",e);}
 private void initialize(){try(Connection c=conn();Statement s=c.createStatement()){
  s.executeUpdate("CREATE TABLE IF NOT EXISTS admins(username VARCHAR(80) PRIMARY KEY,password_hash VARCHAR(255) NOT NULL)");
  s.executeUpdate("CREATE TABLE IF NOT EXISTS students(id VARCHAR(20) PRIMARY KEY,name VARCHAR(120) NOT NULL,email VARCHAR(150),phone VARCHAR(30),course VARCHAR(100),gender VARCHAR(30),address VARCHAR(255))");
  s.executeUpdate("CREATE TABLE IF NOT EXISTS rooms(room_no VARCHAR(20) PRIMARY KEY,type VARCHAR(50) NOT NULL,capacity INT NOT NULL,price DECIMAL(10,2) NOT NULL,status VARCHAR(30) NOT NULL)");
  s.executeUpdate("CREATE TABLE IF NOT EXISTS reservations(id VARCHAR(20) PRIMARY KEY,student_id VARCHAR(20) NOT NULL,room_no VARCHAR(20) NOT NULL,check_in DATE NOT NULL,check_out DATE NOT NULL,total DECIMAL(10,2) NOT NULL,status VARCHAR(30) NOT NULL,FOREIGN KEY(student_id) REFERENCES students(id),FOREIGN KEY(room_no) REFERENCES rooms(room_no),INDEX(room_no,check_in,check_out))");
  s.executeUpdate("CREATE TABLE IF NOT EXISTS payments(id VARCHAR(20) PRIMARY KEY,reservation_id VARCHAR(20) NOT NULL,amount DECIMAL(10,2) NOT NULL,method VARCHAR(50),payment_date DATE,status VARCHAR(30) NOT NULL,FOREIGN KEY(reservation_id) REFERENCES reservations(id))");
  try(PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO admins VALUES('admin',?)")){p.setString(1,hash("admin123"));p.executeUpdate();}
  seed(c);
 }catch(SQLException e){throw error(e);}}
 private void seed(Connection c)throws SQLException{
  try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM rooms")){r.next();if(r.getInt(1)>0)return;}
  String[][] st={{"STU001","Arun Kumar","arun.kumar@mail.com","9840012345","B.Tech CSE","Male","12 Anna Nagar, Chennai"},{"STU002","Priya Sharma","priya.s@mail.com","9876543210","MBA","Female","45 T Nagar, Chennai"},{"STU003","Rahul Verma","rahul.v@mail.com","9123456780","B.Com","Male","8 Gandhi Road, Madurai"},{"STU004","Divya Lakshmi","divya.l@mail.com","9444012345","M.Sc Physics","Female","22 Lake View, Coimbatore"},{"STU005","Mohammed Faizal","faizal.m@mail.com","9790011223","B.Tech ECE","Male","5 Mount Road, Chennai"}};
  try(PreparedStatement p=c.prepareStatement("INSERT INTO students VALUES(?,?,?,?,?,?,?)")){for(String[] a:st){for(int i=0;i<a.length;i++)p.setString(i+1,a[i]);p.addBatch();}p.executeBatch();}
  Object[][] rr={{"101","Single",1,1200,"Occupied"},{"102","Single",1,1200,"Available"},{"103","Single",1,1200,"Available"},{"104","Single",1,1200,"Maintenance"},{"201","Double",2,2000,"Available"},{"202","Double",2,2000,"Available"},{"301","Triple",3,2700,"Available"},{"D01","Dormitory",6,3600,"Available"}};
  try(PreparedStatement p=c.prepareStatement("INSERT INTO rooms VALUES(?,?,?,?,?)")){for(Object[] a:rr){for(int i=0;i<a.length;i++)p.setObject(i+1,a[i]);p.addBatch();}p.executeBatch();}
 }
 private String hash(String raw){try{byte[] salt="HostelReservationSystemSalt".getBytes(java.nio.charset.StandardCharsets.UTF_8);var spec=new javax.crypto.spec.PBEKeySpec(raw.toCharArray(),salt,120000,256);return Base64.getEncoder().encodeToString(javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded());}catch(Exception e){throw error(e);}}
 public boolean authenticate(String u,String p){try(Connection c=conn();PreparedStatement q=c.prepareStatement("SELECT password_hash FROM admins WHERE username=?")){q.setString(1,u);try(ResultSet r=q.executeQuery()){return r.next()&&java.security.MessageDigest.isEqual(hash(p).getBytes(),r.getString(1).getBytes());}}catch(SQLException e){throw error(e);}}
 private int count(String table,String where){try(Connection c=conn();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM "+table+" WHERE "+where)){r.next();return r.getInt(1);}catch(SQLException e){throw error(e);}}
 public int totalRooms(){return count("rooms","1=1");} public int availableRoomCount(){return count("rooms","status='Available'");} public int occupiedRoomCount(){return count("rooms","status='Occupied'");} public int totalStudents(){return count("students","1=1");} public int totalReservations(){return count("reservations","1=1");}
 private void exec(String sql,Object...a){try(Connection c=conn();PreparedStatement p=c.prepareStatement(sql)){bind(p,a);p.executeUpdate();}catch(SQLException e){throw error(e);}}
 private void bind(PreparedStatement p,Object...a)throws SQLException{for(int i=0;i<a.length;i++)p.setObject(i+1,a[i]);}
 private String next(String table,String col,String prefix){try(Connection c=conn();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COALESCE(MAX(CAST(SUBSTRING("+col+",4) AS UNSIGNED)),0)+1 FROM "+table)){r.next();return String.format(Locale.ROOT,prefix+"%03d",r.getLong(1));}catch(SQLException e){throw error(e);}}
 public String nextStudentId(){return next("students","id","STU");} public String nextReservationId(){return next("reservations","id","RES");} public String nextPaymentId(){return next("payments","id","PAY");}
 public List<Student> students(){return studentQuery("SELECT * FROM students ORDER BY id",null);}
 public List<Student> searchStudents(String q){return studentQuery("SELECT * FROM students WHERE id LIKE ? OR name LIKE ? OR course LIKE ? OR phone LIKE ? ORDER BY id",q);}
 private List<Student> studentQuery(String sql,String q){List<Student> a=new ArrayList<>();try(Connection c=conn();PreparedStatement p=c.prepareStatement(sql)){if(q!=null){String k="%"+q.trim()+"%";for(int i=1;i<=4;i++)p.setString(i,k);}try(ResultSet r=p.executeQuery()){while(r.next())a.add(new Student(r.getString("id"),r.getString("name"),r.getString("email"),r.getString("phone"),r.getString("course"),r.getString("gender"),r.getString("address")));}return a;}catch(SQLException e){throw error(e);}}
 public void addStudent(Student x){exec("INSERT INTO students VALUES(?,?,?,?,?,?,?)",x.id(),x.name(),x.email(),x.phone(),x.course(),x.gender(),x.address());}
 public void updateStudent(Student x){exec("UPDATE students SET name=?,email=?,phone=?,course=?,gender=?,address=? WHERE id=?",x.name(),x.email(),x.phone(),x.course(),x.gender(),x.address(),x.id());}
 public boolean deleteStudent(String id){if(count("reservations","student_id='"+id.replace("'","''")+"' AND status IN ('Confirmed','Checked-In')")>0)return false;exec("DELETE FROM students WHERE id=?",id);return true;}
 public List<Room> rooms(){return roomQuery("SELECT * FROM rooms ORDER BY room_no",null,null);}
 public List<Room> searchRooms(String q,String status){return roomQuery("SELECT * FROM rooms WHERE (? IS NULL OR status=?) AND (room_no LIKE ? OR type LIKE ?) ORDER BY room_no",q,status);}
 private List<Room> roomQuery(String sql,String q,String status){List<Room>a=new ArrayList<>();try(Connection c=conn();PreparedStatement p=c.prepareStatement(sql)){if(sql.contains("? IS NULL")){String f=status==null||status.startsWith("All")?null:status;p.setString(1,f);p.setString(2,f);String k="%"+(q==null?"":q.trim())+"%";p.setString(3,k);p.setString(4,k);}try(ResultSet r=p.executeQuery()){while(r.next())a.add(new Room(r.getString("room_no"),r.getString("type"),r.getInt("capacity"),r.getDouble("price"),r.getString("status")));}return a;}catch(SQLException e){throw error(e);}}
 public List<Room> availableRooms(String type){return rooms().stream().filter(x->x.status().equals("Available")&&(type==null||x.type().equals(type))).toList();}
 public Room findRoom(String no){return rooms().stream().filter(x->x.roomNo().equals(no)).findFirst().orElse(null);} public boolean roomExists(String no){return findRoom(no)!=null;}
 public void addRoom(Room x){exec("INSERT INTO rooms VALUES(?,?,?,?,?)",x.roomNo(),x.type(),x.capacity(),x.price(),x.status());} public void updateRoom(Room x){exec("UPDATE rooms SET type=?,capacity=?,price=?,status=? WHERE room_no=?",x.type(),x.capacity(),x.price(),x.status(),x.roomNo());}
 public boolean deleteRoom(String no){if(count("reservations","room_no='"+no.replace("'","''")+"' AND status IN ('Confirmed','Checked-In')")>0)return false;exec("DELETE FROM rooms WHERE room_no=?",no);return true;}
 public List<Reservation> reservations(){return reservationQuery("SELECT r.*,s.name student_name FROM reservations r JOIN students s ON s.id=r.student_id ORDER BY r.id",null,null);}
 public List<Reservation> searchReservations(String q,String status){return reservationQuery("SELECT r.*,s.name student_name FROM reservations r JOIN students s ON s.id=r.student_id WHERE (? IS NULL OR r.status=?) AND (r.id LIKE ? OR s.name LIKE ? OR r.student_id LIKE ? OR r.room_no LIKE ?) ORDER BY r.id",q,status);}
 private List<Reservation> reservationQuery(String sql,String q,String status){List<Reservation>a=new ArrayList<>();try(Connection c=conn();PreparedStatement p=c.prepareStatement(sql)){if(sql.contains("? IS NULL")){String f=status==null||status.startsWith("All")?null:status;p.setString(1,f);p.setString(2,f);String k="%"+(q==null?"":q.trim())+"%";for(int i=3;i<=6;i++)p.setString(i,k);}try(ResultSet r=p.executeQuery()){while(r.next())a.add(new Reservation(r.getString("id"),r.getString("student_id"),r.getString("student_name"),r.getString("room_no"),r.getDate("check_in").toLocalDate(),r.getDate("check_out").toLocalDate(),r.getDouble("total"),r.getString("status")));}return a;}catch(SQLException e){throw error(e);}}
 public Reservation findReservation(String id){return reservations().stream().filter(x->x.id().equals(id)).findFirst().orElse(null);}
 public void addReservation(Reservation x){
  if(!x.checkOut().isAfter(x.checkIn())) throw new IllegalArgumentException("Check-out must be after check-in.");
  try(Connection c=conn()){
   c.setAutoCommit(false);
   try {
    try(PreparedStatement lock=c.prepareStatement("SELECT status FROM rooms WHERE room_no=? FOR UPDATE")){
     lock.setString(1,x.roomNo()); try(ResultSet r=lock.executeQuery()){if(!r.next()||!"Available".equals(r.getString(1))) throw new SQLException("Room is not available.");}
    }
    try(PreparedStatement p=c.prepareStatement("INSERT INTO reservations VALUES(?,?,?,?,?,?,?)")){
     bind(p,x.id(),x.studentId(),x.roomNo(),java.sql.Date.valueOf(x.checkIn()),java.sql.Date.valueOf(x.checkOut()),x.total(),x.status());p.executeUpdate();
    }
    try(PreparedStatement p=c.prepareStatement("UPDATE rooms SET status='Occupied' WHERE room_no=?")){p.setString(1,x.roomNo());p.executeUpdate();}
    c.commit();
   } catch(Exception e){c.rollback();throw error(e);}
  } catch(SQLException e){throw error(e);}
 }

 public void updateReservation(Reservation x){exec("UPDATE reservations SET student_id=?,room_no=?,check_in=?,check_out=?,total=?,status=? WHERE id=?",x.studentId(),x.roomNo(),java.sql.Date.valueOf(x.checkIn()),java.sql.Date.valueOf(x.checkOut()),x.total(),x.status(),x.id());refreshRoom(x.roomNo());}
 public void cancelReservation(String id){Reservation x=findReservation(id);if(x!=null){exec("UPDATE reservations SET status='Cancelled' WHERE id=?",id);refreshRoom(x.roomNo());}}
 private void refreshRoom(String room){if(count("reservations","room_no='"+room.replace("'","''")+"' AND status IN ('Confirmed','Checked-In')")==0)exec("UPDATE rooms SET status='Available' WHERE room_no=? AND status<>'Maintenance'",room);}
 public List<Payment> payments(){return paymentQuery("SELECT p.*,s.name student_name FROM payments p JOIN reservations r ON r.id=p.reservation_id JOIN students s ON s.id=r.student_id ORDER BY p.id",null,null);}
 public List<Payment> searchPayments(String q,String status){return paymentQuery("SELECT p.*,s.name student_name FROM payments p JOIN reservations r ON r.id=p.reservation_id JOIN students s ON s.id=r.student_id WHERE (? IS NULL OR p.status=?) AND (p.id LIKE ? OR p.reservation_id LIKE ? OR s.name LIKE ?) ORDER BY p.id",q,status);}
 private List<Payment> paymentQuery(String sql,String q,String status){List<Payment>a=new ArrayList<>();try(Connection c=conn();PreparedStatement p=c.prepareStatement(sql)){if(sql.contains("? IS NULL")){String f=status==null||status.startsWith("All")?null:status;p.setString(1,f);p.setString(2,f);String k="%"+(q==null?"":q.trim())+"%";for(int i=3;i<=5;i++)p.setString(i,k);}try(ResultSet r=p.executeQuery()){while(r.next())a.add(new Payment(r.getString("id"),r.getString("reservation_id"),r.getString("student_name"),r.getDouble("amount"),r.getString("method"),r.getDate("payment_date")==null?null:r.getDate("payment_date").toLocalDate(),r.getString("status")));}return a;}catch(SQLException e){throw error(e);}}
 public void addPayment(Payment x){exec("INSERT INTO payments VALUES(?,?,?,?,?,?)",x.id(),x.reservationId(),x.amount(),x.method(),x.date()==null?null:java.sql.Date.valueOf(x.date()),x.status());} public void updatePayment(Payment x){exec("UPDATE payments SET reservation_id=?,amount=?,method=?,payment_date=?,status=? WHERE id=?",x.reservationId(),x.amount(),x.method(),x.date()==null?null:java.sql.Date.valueOf(x.date()),x.status(),x.id());}
}
