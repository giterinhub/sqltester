# Java 8 App Service - SQL Server Connectivity

This project is a Java 8 application hosted on Azure App Service, connecting to an Azure SQL Server database.

## Prerequisites

* **Java Development Kit (JDK):** Version 8
* **Azure App Service:** Linux environment
* **Database:** Microsoft SQL Server (Azure SQL Database)
* **JDBC Driver:** `sqljdbc42.jar` (or compatible version for Java 8)

## Configuration

The application relies on an external configuration file for database credentials. This file must be present in the application's classpath or working directory.

### `datasource.properties`

**Security Warning:** Never commit real passwords to version control. Use environment variables or Azure Key Vault for production secrets.

The file `datasource.properties` should be structured as follows:

```properties
# Database Connection Settings
jdbc.url=jdbc:sqlserver://<SERVER_NAME>.database.windows.net:1433;database=<DB_NAME>;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
jdbc.username=<YOUR_USERNAME>
jdbc.password=<YOUR_PASSWORD>

# (Or if using Spring Boot style keys)
# spring.datasource.url=...
# spring.datasource.username=...
# spring.datasource.password=...

```

## Deployment

1. Build the application JAR/WAR.
2. Deploy the artifact to Azure App Service (via Maven, Gradle, GitHub Actions, or Azure CLI).
3. Ensure `datasource.properties` and the JDBC Driver JAR are deployed to the appropriate directory (e.g., `/home/site/wwwroot`).

## Troubleshooting Database Connectivity

If the application fails to connect to the database, use the following manual verification steps directly inside the Azure App Service SSH terminal.

### 1. Network Reachability Test

Verify the App Service can reach the SQL Server TCP port (checking for Firewall blocks).

```bash
# Expectation: Connection hangs or says "Connected".
# If it fails immediately, check Azure SQL Firewall settings to "Allow Azure Services".
curl -v telnet://<your-server>.database.windows.net:1433

```

### 2. Application Connectivity Test (Java)

To verify that Java, the Driver, and the Credentials are all correct, create and run the `SQLTester` utility on the server.

#### Step A: Create the Tester

Run this command in the SSH terminal to generate `SQLTester.java`. This script reads your `datasource.properties` file directly.

```java
cat <<EOF > SQLTester.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;

public class SQLTester {
    public static void main(String[] args) {
        String propFile = "datasource.properties";
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propFile));
            
            // ADJUST KEYS BELOW TO MATCH YOUR PROPERTIES FILE
            String url = props.getProperty("jdbc.url"); 
            String user = props.getProperty("jdbc.username");
            String pass = props.getProperty("jdbc.password");

            if (url == null) url = props.getProperty("spring.datasource.url");
            if (user == null) user = props.getProperty("spring.datasource.username");
            if (pass == null) pass = props.getProperty("spring.datasource.password");

            System.out.println("Testing connection to: " + url);
            
            // Load Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            // Attempt Connection
            try (Connection con = DriverManager.getConnection(url, user, pass)) {
                System.out.println("SUCCESS: Connection established!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
EOF

```

#### Step B: Compile and Run

Ensure `sqljdbc42.jar` is in the current directory.

```bash
# Compile
javac SQLTester.java

# Run (Note the colon ':' separator for Linux classpath)
java -cp .:./sqljdbc42.jar SQLTester

```

---

## Common Errors

* **Login Failed:** Check `datasource.properties` for typos in username/password.
* **TCP/IP Connection to Host Failed:** The server name is wrong, or the Azure SQL Firewall is blocking the connection.
* **Class Not Found:** The classpath (`-cp`) in the java command is incorrect, or the JAR file is missing.

