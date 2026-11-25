HOSTEL RESERVATION SYSTEM - INTEGRATED FRONTEND + MYSQL BACKEND

Requirements: JDK 17+, MySQL 8+, IntelliJ IDEA, internet access for Maven dependency download.

SETUP
1. Start MySQL Server.
2. Run database.sql in MySQL Workbench (creates hostel_reservation database).
3. Configure MySQL credentials using environment variables:
   HOSTEL_DB_URL=jdbc:mysql://localhost:3306/hostel_reservation?useSSL=false&serverTimezone=Asia/Kolkata
   HOSTEL_DB_USER=root
   HOSTEL_DB_PASSWORD=your_mysql_password
   Or set these in IntelliJ Run > Edit Configurations > Environment variables.
4. Open this folder (the one containing pom.xml) in IntelliJ IDEA.
5. Set Project SDK to JDK 17 or newer; reload Maven dependencies.
6. Run src/hostel/Main.java.

LOGIN: username admin / password admin123
The application creates its tables and initial sample records on first launch.

IMPORTANT: Change the default admin password before real deployment. This is an educational starter project; test all flows with your own MySQL instance before submission.
