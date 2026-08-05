/**
 * Database utility class for establishing connections and loading environment configurations.
 */
package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBUtils {

    private static Map<String, String> env;

    static {
        env = loadEnv(".env");
    }

    private static Map<String, String> loadEnv(String filename) {
        Map<String, String> envMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    envMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load " + filename + " file.");
        }
        return envMap;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
        String url = env.getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/cinesphere");
        String user = env.getOrDefault("DB_USER", "root");
        String password = env.getOrDefault("DB_PASSWORD", "groot");
        return DriverManager.getConnection(url, user, password);
    }
}

