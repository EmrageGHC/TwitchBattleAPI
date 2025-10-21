package org.emrage.twitchbattleapi.teams;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.emrage.twitchbattleapi.TwitchBattleAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages team operations
 */
public class TeamManager {
    private final TwitchBattleAPI api;
    private final Map<Integer, Team> teams;
    private final Map<UUID, Integer> playerTeams;
    private final Logger logger = Logger.getLogger("TwitchBattleAPI");
    private int nextTeamId = 1;

    /**
     * Create a new team manager
     * @param api The API instance
     */
    public TeamManager(TwitchBattleAPI api) {
        this.api = api;
        this.teams = new HashMap<>();
        this.playerTeams = new HashMap<>();
        loadTeams();
    }

    /**
     * Load teams from the database
     */
    private void loadTeams() {
        // Load teams
        List<Document> teamDocs = api.getDatabaseManager().find("teams", new Document());
        for (Document doc : teamDocs) {
            int id = doc.getInteger("id", 0);
            String name = doc.getString("name");
            String displayName = doc.getString("display_name");
            String color = doc.getString("color");

            Team team = new Team(id, name, displayName, color);
            teams.put(id, team);

            // Keep track of the highest team ID
            if (id >= nextTeamId) {
                nextTeamId = id + 1;
            }
        }

        // Load team members
        Document teamFilter = new Document("team_id", new Document("$ne", null));
        List<Document> playerDocs = api.getDatabaseManager().find("players", teamFilter);
        for (Document doc : playerDocs) {
            UUID playerUUID = UUID.fromString(doc.getString("uuid"));
            int teamId = doc.getInteger("team_id");

            if (teams.containsKey(teamId)) {
                teams.get(teamId).addMember(playerUUID);
                playerTeams.put(playerUUID, teamId);
            }
        }
    }

    /**
     * Create a new team
     * @param name The team name
     * @param displayName The team display name
     * @param color The team color in hex format
     * @return The created team, or null if creation failed
     */
    public Team createTeam(String name, String displayName, String color) {
        // Check if a team with this name already exists
        for (Team team : teams.values()) {
            if (team.getName().equalsIgnoreCase(name)) {
                return null;
            }
        }

        // Create new team document
        Document teamDoc = new Document()
                .append("id", nextTeamId)
                .append("name", name)
                .append("display_name", displayName)
                .append("color", color)
                .append("created_at", new java.util.Date());

        boolean success = api.getDatabaseManager().insertOne("teams", teamDoc);

        if (success) {
            Team team = new Team(nextTeamId, name, displayName, color);
            teams.put(nextTeamId, team);
            nextTeamId++;
            return team;
        }

        return null;
    }

    /**
     * Get a team by ID
     * @param id The team ID
     * @return The team, or null if not found
     */
    public Team getTeam(int id) {
        return teams.get(id);
    }

    /**
     * Get a team by name
     * @param name The team name
     * @return The team, or null if not found
     */
    public Team getTeamByName(String name) {
        for (Team team : teams.values()) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }

    /**
     * Get all teams
     * @return List of all teams
     */
    public List<Team> getAllTeams() {
        return new ArrayList<>(teams.values());
    }

    /**
     * Delete a team
     * @param id The team ID
     * @return True if the team was deleted, false otherwise
     */
    public boolean deleteTeam(int id) {
        if (!teams.containsKey(id)) {
            return false;
        }

        boolean success = api.getDatabaseManager().deleteOne("teams", new Document("id", id));

        if (success) {
            // Remove players from the team
            for (UUID playerUUID : new ArrayList<>(teams.get(id).getMembers())) {
                playerTeams.remove(playerUUID);

                // Update player document to remove team association
                Document playerFilter = new Document("uuid", playerUUID.toString());
                Document update = new Document("team_id", null);
                api.getDatabaseManager().updateOne("players", playerFilter, update);
            }

            teams.remove(id);
            return true;
        }

        return false;
    }

    /**
     * Update a team
     * @param team The team to update
     * @return True if the team was updated, false otherwise
     */
    public boolean updateTeam(Team team) {
        Document filter = new Document("id", team.getId());
        Document update = new Document()
                .append("name", team.getName())
                .append("display_name", team.getDisplayName())
                .append("color", team.getColor());

        return api.getDatabaseManager().updateOne("teams", filter, update);
    }

    /**
     * Add a player to a team
     * @param playerUUID The player UUID
     * @param teamId The team ID
     * @return True if the player was added, false otherwise
     */
    public boolean addPlayerToTeam(UUID playerUUID, int teamId) {
        if (!teams.containsKey(teamId)) {
            return false;
        }

        // Remove from current team if any
        if (playerTeams.containsKey(playerUUID)) {
            removePlayerFromTeam(playerUUID);
        }

        // Get player username
        Player player = Bukkit.getPlayer(playerUUID);
        String username = player != null ? player.getName() : playerUUID.toString();

        // Check if player exists in database
        Document playerFilter = new Document("uuid", playerUUID.toString());
        Document playerDoc = api.getDatabaseManager().findOne("players", playerFilter);

        boolean success;
        if (playerDoc == null) {
            // Insert new player document
            Document newPlayerDoc = new Document()
                    .append("uuid", playerUUID.toString())
                    .append("username", username)
                    .append("team_id", teamId);
            success = api.getDatabaseManager().insertOne("players", newPlayerDoc);
        } else {
            // Update existing player document
            Document update = new Document("team_id", teamId);
            if (!playerDoc.getString("username").equals(username)) {
                update.append("username", username);
            }
            success = api.getDatabaseManager().updateOne("players", playerFilter, update);
        }

        if (success) {
            teams.get(teamId).addMember(playerUUID);
            playerTeams.put(playerUUID, teamId);
            return true;
        }

        return false;
    }

    /**
     * Remove a player from their team
     * @param playerUUID The player UUID
     * @return True if the player was removed, false otherwise
     */
    public boolean removePlayerFromTeam(UUID playerUUID) {
        if (!playerTeams.containsKey(playerUUID)) {
            return false;
        }

        int teamId = playerTeams.get(playerUUID);
        Document filter = new Document("uuid", playerUUID.toString());
        Document update = new Document("team_id", null);
        boolean success = api.getDatabaseManager().updateOne("players", filter, update);

        if (success) {
            teams.get(teamId).removeMember(playerUUID);
            playerTeams.remove(playerUUID);
            return true;
        }

        return false;
    }

    /**
     * Get a player's team
     * @param playerUUID The player UUID
     * @return The team, or null if the player is not in a team
     */
    public Team getPlayerTeam(UUID playerUUID) {
        if (!playerTeams.containsKey(playerUUID)) {
            return null;
        }

        return teams.get(playerTeams.get(playerUUID));
    }
}