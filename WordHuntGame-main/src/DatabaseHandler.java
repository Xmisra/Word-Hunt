import java.sql.*;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/wordgame?createDatabaseIfNotExist=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "524110064";

    // Static initializer to ensure tables are created when the class is loaded.
    static {
        createTablesIfNotExists();
    }

    // Returns a new connection using the DriverManager.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Creates required tables if they do not already exist.
    public static void createTablesIfNotExists() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create players table
            String sqlPlayers = "CREATE TABLE IF NOT EXISTS players (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) UNIQUE NOT NULL" +
                    ")";
            stmt.executeUpdate(sqlPlayers);

            // Create game_results table
            String sqlGameResults = "CREATE TABLE IF NOT EXISTS game_results (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "player_name VARCHAR(255) NOT NULL, " +
                    "score INT NOT NULL, " +
                    "words TEXT, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            stmt.executeUpdate(sqlGameResults);

            // Create leaderboard table
            String sqlLeaderboard = "CREATE TABLE IF NOT EXISTS leaderboard (" +
                    "player_name VARCHAR(255) PRIMARY KEY, " +
                    "score INT NOT NULL" +
                    ")";
            stmt.executeUpdate(sqlLeaderboard);

        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    // Creates a new player record if one does not already exist.
    public static void createPlayerIfNotExists(String playerName) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT IGNORE INTO players (name) VALUES (?)")) {
            ps.setString(1, playerName);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating player: " + e.getMessage());
        }
    }

    // Saves the game result for a player.
    public static void saveGameResult(String playerName, int score, Set<String> usedWords) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO game_results (player_name, score, words) VALUES (?, ?, ?)")) {
            ps.setString(1, playerName);
            ps.setInt(2, score);
            ps.setString(3, String.join(", ", usedWords));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving game result: " + e.getMessage());
        }
    }

    // Updates the leaderboard for a player.
    public static void updateLeaderboard(String playerName, int score) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO leaderboard (player_name, score) VALUES (?, ?) " +
                             "ON DUPLICATE KEY UPDATE score = score + ?")) {
            ps.setString(1, playerName);
            ps.setInt(2, score);
            ps.setInt(3, score);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating leaderboard: " + e.getMessage());
        }
    }
}
