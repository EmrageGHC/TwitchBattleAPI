package org.emrage.twitchbattleapi.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.emrage.twitchbattleapi.TwitchBattleAPI;

/**
 * Manages database connections and operations
 */
public class DatabaseManager {
    private String connectionString;
    private Connection connection;
    private final Logger logger = Logger.getLogger("TwitchBattleAPI");

    /**
     * Create a new database manager
     * @param connectionString JDBC connection string
     */
    public DatabaseManager(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Connect to the database
     */
    public void connect() {
        try {
            this.connection = DriverManager.getConnection(connectionString);
            logger.info("Successfully connected to database");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
        }
    }

    /**
     * Disconnect from the database
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Successfully disconnected from database");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to disconnect from database", e);
        }
    }

    /**
     * Create tables needed for this API
     */
    public void createTables() {
        String teamTable = "CREATE TABLE IF NOT EXISTS teams (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL, " +
                "display_name VARCHAR(255), " +
                "color VARCHAR(50), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String playerTable = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(16) NOT NULL, " +
                "team_id INTEGER, " +
                "FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL)";

        String pointsTable = "CREATE TABLE IF NOT EXISTS points (" +
                "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "team_id INTEGER, " +
                "player_uuid VARCHAR(36), " +
                "points INTEGER DEFAULT 0, " +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE)";

        executeUpdate(teamTable);
        executeUpdate(playerTable);
        executeUpdate(pointsTable);
    }

    /**
     * Execute a SQL update statement
     * @param sql The SQL statement
     * @return True if successful, false otherwise
     */
    public boolean executeUpdate(String sql) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute update: " + sql, e);
            return false;
        }
    }

    /**
     * Execute a SQL query statement
     * @param sql The SQL statement
     * @return ResultSet with the query results
     */
    public ResultSet executeQuery(String sql) {
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute query: " + sql, e);
            return null;
        }
    }

    /**
     * Execute a prepared statement with parameters
     * @param sql The SQL statement with placeholders
     * @param params The parameters to replace the placeholders
     * @return True if successful, false otherwise
     */
    public boolean executeUpdate(String sql, Object... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute update with params", e);
            return false;
        }
    }

    /**
     * Execute a prepared query with parameters
     * @param sql The SQL statement with placeholders
     * @param params The parameters to replace the placeholders
     * @return ResultSet with the query results
     */
    public ResultSet executeQuery(String sql, Object... params) {
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute query with params", e);
            return null;
        }
    }

    /**
     * Get the connection object
     * @return The database connection
     */
    public Connection getConnection() {
        return connection;
    }
}