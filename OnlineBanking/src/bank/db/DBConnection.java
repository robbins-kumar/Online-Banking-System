package bank.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static String url = "jdbc:mysql://localhost:3306/online_banking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static String user = "root";
    private static String password = "";

    static {
        try (FileInputStream in = new FileInputStream("db.properties")) {
            Properties p = new Properties();
            p.load(in);
            url = p.getProperty("url", url);
            user = p.getProperty("user", user);
            password = p.getProperty("password", password);
        } catch (IOException ignored) {
            // fall back to defaults
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
