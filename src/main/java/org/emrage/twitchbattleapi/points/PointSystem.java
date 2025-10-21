package org.emrage.twitchbattleapi.points;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.emrage.twitchbattleapi.TwitchBattleAPI;
import org.emrage.twitchbattleapi.teams.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        Document teamFilter = new Document();
        teamFilter.append("team_id", new Document("$ne", null));

        List<Document> teamDocs = api.getDatabaseManager().find("points", teamFilter);
        for (Document doc : teamDocs) {
            int teamId = doc.getInteger("team_id");
            int points = doc.getInteger("points", 0);
            teamPoints.put(teamId, points);
        }

        // Load player points
        Document playerFilter = new Document();
        playerFilter.append("player_uuid", new Document("$ne", null));

        List<Document> playerDocs = api.getDatabaseManager().find("points", playerFilter);
        for (Document doc : playerDocs) {
            UUID playerUUID = UUID.fromString(doc.getString("player_uuid"));
            int points = doc.getInteger("points", 0);
            playerPoints.put(playerUUID, points);
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

        Document filter = new Document("team_id", teamId);
        Document existingDoc = api.getDatabaseManager().findOne("points", filter);

        boolean success;
        if (existingDoc == null) {
            // Insert new document
            Document newDoc = new Document()
                    .append("team_id", teamId)
                    .append("points", newPoints)
                    .append("last_updated", new java.util.Date());
            success = api.getDatabaseManager().insertOne("points", newDoc);
        } else {
            // Update existing document
            Document update = new Document()
                    .append("points", newPoints)
                    .append("last_updated", new java.util.Date());
            success = api.getDatabaseManager().updateOne("points", filter, update);
        }

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
        Document filter = new Document("team_id", teamId);
        Document existingDoc = api.getDatabaseManager().findOne("points", filter);

        boolean success;
        if (existingDoc == null) {
            // Insert new document
            Document newDoc = new Document()
                    .append("team_id", teamId)
                    .append("points", points)
                    .append("last_updated", new java.util.Date());
            success = api.getDatabaseManager().insertOne("points", newDoc);
        } else {
            // Update existing document
            Document update = new Document()
                    .append("points", points)
                    .append("last_updated", new java.util.Date());
            success = api.getDatabaseManager().updateOne("points", filter, update);
        }

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
        ensurePlayerExists(playerUUID);

        // Update points
        Document filter = new Document("player_uuid", playerUUID.toString());
        Document existingDoc = api.getDatabaseManager().findOne("points", filter);

        boolean success;
        if (existingDoc == null) {
            // Insert new document
            Document newDoc = new Document()
                    .append("player_uuid", playerUUID.toString())
                    .append("points", newPoints)
                    .append("last_updated", new java.util.Date());
            success = api.getDatabaseManager().insertOne("points", newDoc);
        } else {
            // Update existing document
            Document update = new Document()
                    .append("points", newPoints)
                    .append("last_updated", new java.util.Date());
            success = api.getDatabaseManager().updateOne("points", filter, update);
        }

        if (success) {
            playerPoints.put(playerUUID, newPoints);
            return newPoints;
        }

        return currentPoints;
    }

    /**
     * Ensure player exists in the database
     * @param playerUUID The player UUID
     */
    private void ensurePlayerExists(UUID playerUUID) {
        Document filter = new Document("uuid", playerUUID.toString());
        Document playerDoc = api.getDatabaseManager().findOne("players", filter);

        if (playerDoc == null) {
            Player player = Bukkit.getPlayer(playerUUID);
            String username = player != null ? player.getName() : playerUUID.toString();

            Document newPlayerDoc = new Document()
                    .append("uuid", playerUUID.toString())
                    .append("username", username);

            api.getDatabaseManager().insertOne("players", newPlayerDoc);
        }
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
        ensurePlayerExists(playerUUID);

        // Update points
        Document filter = new Document("player_uuid", playerUUID.toString());
        Document existingDoc = api.getDatabaseManager().findOne("points", filter);

        boolean success;
        if (existingDoc == null) {
            // Insert new document
            Document newDoc = new Document()
                    .append("player_uuid", playerUUID.toString())
                    .append("points", points)
                    .append("last_updated", new java.util.Date());
            success = api.getDatabaseManager().insertOne("points", newDoc);
        } else {
            // Update existing document
            Document update = new Document()
                    .append("points", points)
                    .append("last_updated", new java.util.Date());
            success = api.getDatabaseManager().updateOne("points", filter, update);
        }

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
        Document filter = new Document();
        filter.append("team_id", new Document("$ne", null));

        boolean success = false;
        try {
            api.getDatabaseManager().getDatabase().getCollection("points").deleteMany(filter);
            success = true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reset team points", e);
        }

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
        Document filter = new Document();
        filter.append("player_uuid", new Document("$ne", null));

        boolean success = false;
        try {
            api.getDatabaseManager().getDatabase().getCollection("points").deleteMany(filter);
            success = true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reset player points", e);
        }

        if (success) {
            playerPoints.clear();
            return true;
        }

        return false;
    }
}