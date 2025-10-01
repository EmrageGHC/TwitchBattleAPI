package org.emrage.twitchbattleapi.points;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.emrage.twitchbattleapi.TwitchBattleAPI;
import org.emrage.twitchbattleapi.teams.Team;

/**
 * Manages the point system
 */
public class PointSystem {
    private final TwitchBattleAPI api;
    private final Map<Integer, Integer> teamPoints;
    private final Map<UUID, Integer> playerPoints;
    private final Logger logger = Logger.getLogger("TwitchBattleAPI");

    /**
     * Create a new point system
     * @param api The API instance
     */
    public PointSystem(TwitchBattleAPI api) {
        this.api = api;
        this.teamPoints = new HashMap<>();
        this.playerPoints = new HashMap<>();
        loadPoints();
    }

    /**
     * Load points from the database
     */
    private void loadPoints() {
        // Load team points
        ResultSet teamResult = api.getDatabaseManager().executeQuery(
                "SELECT team_id, points FROM points WHERE team_id IS NOT NULL");
        try {
            while (teamResult != null && teamResult.next()) {
                int teamId = teamResult.getInt("team_id");
                int points = teamResult.getInt("points");
                teamPoints.put(teamId, points);
            }
            
            // Load player points
            ResultSet playerResult = api.getDatabaseManager().executeQuery(
                    "SELECT player_uuid, points FROM points WHERE player_uuid IS NOT NULL");
            while (playerResult != null && playerResult.next()) {
                UUID playerUUID = UUID.fromString(playerResult.getString("player_uuid"));
                int points = playerResult.getInt("points");
                playerPoints.put(playerUUID, points);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load points from database", e);
        }
    }

    /**
     * Add points to a team
     * @param teamId The team ID
     * @param points The points to add
     * @return The new total points
     */
    public int addTeamPoints(int teamId, int points) {
        int currentPoints = getTeamPoints(teamId);
        int newPoints = currentPoints + points;
        
        String sql = "INSERT INTO points (team_id, points) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE points = ?";
        boolean success = api.getDatabaseManager().executeUpdate(sql, teamId, newPoints, newPoints);
        
        if (success) {
            teamPoints.put(teamId, newPoints);
            return newPoints;
        }
        
        return currentPoints;
    }

    /**
     * Remove points from a team
     * @param teamId The team ID
     * @param points The points to remove
     * @return The new total points
     */
    public int removeTeamPoints(int teamId, int points) {
        return addTeamPoints(teamId, -points);
    }

    /**
     * Set team points
     * @param teamId The team ID
     * @param points The points to set
     * @return True if successful, false otherwise
     */
    public boolean setTeamPoints(int teamId, int points) {
        String sql = "INSERT INTO points (team_id, points) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE points = ?";
        boolean success = api.getDatabaseManager().executeUpdate(sql, teamId, points, points);
        
        if (success) {
            teamPoints.put(teamId, points);
            return true;
        }
        
        return false;
    }

    /**
     * Get team points
     * @param teamId The team ID
     * @return The team points
     */
    public int getTeamPoints(int teamId) {
        return teamPoints.getOrDefault(teamId, 0);
    }

    /**
     * Add points to a player
     * @param playerUUID The player UUID
     * @param points The points to add
     * @return The new total points
     */
    public int addPlayerPoints(UUID playerUUID, int points) {
        int currentPoints = getPlayerPoints(playerUUID);
        int newPoints = currentPoints + points;
        
        // Ensure player exists in the database
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            String checkSql = "INSERT IGNORE INTO players (uuid, username) VALUES (?, ?)";
            api.getDatabaseManager().executeUpdate(checkSql, playerUUID.toString(), player.getName());
        }
        
        String sql = "INSERT INTO points (player_uuid, points) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE points = ?";
        boolean success = api.getDatabaseManager().executeUpdate(sql, playerUUID.toString(), newPoints, newPoints);
        
        if (success) {
            playerPoints.put(playerUUID, newPoints);
            return newPoints;
        }
        
        return currentPoints;
    }

    /**
     * Remove points from a player
     * @param playerUUID The player UUID
     * @param points The points to remove
     * @return The new total points
     */
    public int removePlayerPoints(UUID playerUUID, int points) {
        return addPlayerPoints(playerUUID, -points);
    }

    /**
     * Set player points
     * @param playerUUID The player UUID
     * @param points The points to set
     * @return True if successful, false otherwise
     */
    public boolean setPlayerPoints(UUID playerUUID, int points) {
        // Ensure player exists in the database
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            String checkSql = "INSERT IGNORE INTO players (uuid, username) VALUES (?, ?)";
            api.getDatabaseManager().executeUpdate(checkSql, playerUUID.toString(), player.getName());
        }
        
        String sql = "INSERT INTO points (player_uuid, points) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE points = ?";
        boolean success = api.getDatabaseManager().executeUpdate(sql, playerUUID.toString(), points, points);
        
        if (success) {
            playerPoints.put(playerUUID, points);
            return true;
        }
        
        return false;
    }

    /**
     * Get player points
     * @param playerUUID The player UUID
     * @return The player points
     */
    public int getPlayerPoints(UUID playerUUID) {
        return playerPoints.getOrDefault(playerUUID, 0);
    }

    /**
     * Get team points map
     * @return Map of team ID to points
     */
    public Map<Integer, Integer> getTeamPointsMap() {
        return new HashMap<>(teamPoints);
    }

    /**
     * Get player points map
     * @return Map of player UUID to points
     */
    public Map<UUID, Integer> getPlayerPointsMap() {
        return new HashMap<>(playerPoints);
    }

    /**
     * Reset all team points
     * @return True if successful, false otherwise
     */
    public boolean resetTeamPoints() {
        String sql = "DELETE FROM points WHERE team_id IS NOT NULL";
        boolean success = api.getDatabaseManager().executeUpdate(sql);
        
        if (success) {
            teamPoints.clear();
            return true;
        }
        
        return false;
    }

    /**
     * Reset all player points
     * @return True if successful, false otherwise
     */
    public boolean resetPlayerPoints() {
        String sql = "DELETE FROM points WHERE player_uuid IS NOT NULL";
        boolean success = api.getDatabaseManager().executeUpdate(sql);
        
        if (success) {
            playerPoints.clear();
            return true;
        }
        
        return false;
    }
}