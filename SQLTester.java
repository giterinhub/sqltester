import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class SQLTester {
    public static void main(String[] args) {
        Properties props = new Properties();
        String propFileName = "datasource.properties";

        try {
            // Load the properties file
            FileInputStream inputStream = new FileInputStream(propFileName);
            props.load(inputStream);
            System.out.println("Loaded " + propFileName);

            // -----------------------------------------------------------
            // EDIT THESE KEYS TO MATCH YOUR FILE
            // Common frameworks use: spring.datasource.url, spring.datasource.username, etc.
            // Or simple keys like: url, user, password
            // -----------------------------------------------------------
            
            // Try to find the URL (checking common variations)
            String dbUrl = props.getProperty("spring.datasource.url");
            if (dbUrl == null) dbUrl = props.getProperty("url");
            if (dbUrl == null) dbUrl = props.getProperty("jdbc.url");
            
            // Try to find the User
            String dbUser = props.getProperty("spring.datasource.username");
            if (dbUser == null) dbUser = props.getProperty("username");
            if (dbUser == null) dbUser = props.getProperty("user");

            // Try to find the Password
            String dbPassword = props.getProperty("spring.datasource.password");
            if (dbPassword == null) dbPassword = props.getProperty("password");

            // -----------------------------------------------------------

            if (dbUrl == null || dbUser == null || dbPassword == null) {
                System.out.println("ERROR: Could not find one of the keys in datasource.properties.");
                System.out.println("Found URL: " + dbUrl);
                System.out.println("Found User: " + dbUser);
                System.out.println("Found Pass: " + (dbPassword != null ? "****" : "null"));
                System.out.println("Please edit SQLTester.java to match your property keys.");
                return;
            }

            System.out.println("Loading driver...");
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                 System.out.println("Driver not found! checking legacy driver...");
                 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            }

            System.out.println("Attempting connection to: " + dbUrl);
            try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                System.out.println("SUCCESS! Connection established with database.");
            }

        } catch (IOException e) {
            System.out.println("Error reading datasource.properties. Is it in the current folder?");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error: Connection failed.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Error: JDBC Driver class not found in classpath.");
        }
    }
}
