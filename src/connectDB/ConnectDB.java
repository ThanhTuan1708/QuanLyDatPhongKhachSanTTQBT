package connectDB;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectDB {
    // private static Connection con = null; // <-- BỎ DÒNG NÀY
    // private static final ConnectDB instance = new ConnectDB(); // <-- BỎ DÒNG NÀY
    private static final Properties props = new Properties();

    static {
        // (Giữ nguyên khối static block để đọc properties)
        props.setProperty("db.url", "jdbc:sqlserver://localhost:1433;databaseName=QuanLyKhachSan;encrypt=true;trustServerCertificate=true");
        props.setProperty("db.user", "sa");
        props.setProperty("db.password", "sapassword");
        try {
            File propFile = new File("src/database.properties");
            if (propFile.exists()) {
                try (FileInputStream input = new FileInputStream(propFile)) {
                    Properties fileProps = new Properties();
                    fileProps.load(input);
                    props.putAll(fileProps);
                }
            }
        } catch (IOException e) {
            System.err.println("Cảnh báo: Không thể đọc file cấu hình: " + e.getMessage());
        }

        // Load driver một lần
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Không thể tải JDBC driver: " + e.getMessage());
        }
    }

    // public static ConnectDB getInstance() { // <-- BỎ HÀM NÀY
    //     return instance;
    // }

    public static Connection getConnection() throws SQLException {
        // Trả về một kết nối MỚI mỗi lần
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }

    public static void disconnect() throws SQLException {
        // (Bỏ hàm này - vì DAO sẽ tự đóng kết nối)
    }

    public static void main(String[] args) {
        try {
            // Test connection
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Kết nối thành công!");
                System.out.println("Database: " + conn.getCatalog());
                System.out.println("Server version: " + conn.getMetaData().getDatabaseProductVersion());
                disconnect();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
