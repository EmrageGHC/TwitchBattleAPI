package org.emrage.twitchbattleapi.teams;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.emrage.twitchbattleapi.TwitchBattleAPI;

/**
 * Manages team operations
 */
public class TeamManager {
    private final TwitchBattleAPI api;
    private final Map<Integer, Team> teams;
    private final Map<UUID, Integer> playerTeams;
    private final Logger logger = Logger.getLogger("TwitchBattleAPI");

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
        ResultSet teamResult = api.getDatabaseManager().executeQuery("SELECT * FROM teams");
        try {
            while (teamResult != null && teamResult.next()) {
                int id = teamResult.getInt("id");
                String name = teamResult.getString("name");
                String displayName = teamResult.getString("display_name");
                String color = teamResult.getString("color");
                
                Team team = new Team(id, name, displayName, color);
                teams.put(id, team);
            }
            
            // Load team members
            ResultSet playerResult = api.getDatabaseManager().executeQuery("SELECT * FROM players WHERE team_id IS NOT NULL");
            while (playerResult != null && playerResult.next()) {
                UUID playerUUID = UUID.fromString(playerResult.getString("uuid"));
                int teamId = playerResult.getInt("team_id");
                
                if (teams.containsKey(teamId)) {
                    teams.get(teamId).addMember(playerUUID);
                    playerTeams.put(playerUUID, teamId);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load teams from database", e);
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
        String sql = "INSERT INTO teams (name, display_name, color) VALUES (?, ?, ?)";
        boolean success = api.getDatabaseManager().executeUpdate(sql, name, displayName, color);
        
        if (success) {
            ResultSet rs = api.getDatabaseManager().executeQuery("SELECT LAST_INSERT_ID() as id");
            try {
                if (rs != null && rs.next()) {
                    int id = rs.getInt("id");
                    Team team = new Team(id, name, displayName, color);
                    teams.put(id, team);
                    return team;
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to retrieve team ID after creation", e);
            }
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
        
        String sql = "DELETE FROM teams WHERE id = ?";
        boolean success = api.getDatabaseManager().executeUpdate(sql, id);
        
        if (success) {
            // Remove players from the team
            for (UUID playerUUID : new ArrayList<>(teams.get(id).getMembers())) {
                playerTeams.remove(playerUUID);
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
        String sql = "UPDATE teams SET name = ?, display_name = ?, color = ? WHERE id = ?";
        return api.getDatabaseManager().executeUpdate(sql, team.getName(), team.getDisplayName(), team.getColor(), team.getId());
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
        
        String sql = "INSERT INTO players (uuid, username, team_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE team_id = ?";
        
        Player player = Bukkit.getPlayer(playerUUID);
        String username = player != null ? player.getName() : playerUUID.toString();
        
        boolean success = api.getDatabaseManager().executeUpdate(sql, playerUUID.toString(), username, teamId, teamId);
        
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
        String sql = "UPDATE players SET team_id = NULL WHERE uuid = ?";
        boolean success = api.getDatabaseManager().executeUpdate(sql, playerUUID.toString());
        
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