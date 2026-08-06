/**
 * Database Utility Class (DBUtils)
 * 
 * Responsibility:
 * 1. Reads database configuration settings (DB_URL, DB_USER, DB_PASSWORD) from the `.env` file.
 * 2. Creates and returns active MySQL Database Connections (`java.sql.Connection`) for all DAOs.
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

    // Holds key-value configuration pairs loaded from the .env configuration file
    private static Map<String, String> env;

    // Static initializer block: runs once automatically when DBUtils class is first loaded
    static {
        env = loadEnv(".env");
    }

    /**
     * Reads a key-value properties file (like .env) line by line.
     * Parses lines formatted as KEY=VALUE into a Map.
     */
    private static Map<String, String> loadEnv(String filename) {
        Map<String, String> envMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comment lines starting with '#'
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                // Split line at the first '=' character
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

    /**
     * Establishes and returns a new MySQL JDBC connection using configuration parameters.
     * Uses fallback default values if .env properties are missing.
     * 
     * @return Connection active database connection instance
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        // Ensure MySQL JDBC Driver class is registered
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }

        // Fetch settings from environment map, providing fallback defaults if omitted
        String url = env.getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/cinesphere");
        String user = env.getOrDefault("DB_USER", "root");
        String password = env.getOrDefault("DB_PASSWORD", "groot");

        // Connect to MySQL server via JDBC DriverManager
        return DriverManager.getConnection(url, user, password);
    }
}

