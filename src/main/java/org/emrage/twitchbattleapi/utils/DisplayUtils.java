package org.emrage.twitchbattleapi.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.emrage.twitchbattleapi.TwitchBattleAPI;

/**
 * Utility class for displaying information to players
 */
public class DisplayUtils {
    private final TwitchBattleAPI api;

    /**
     * Create a new display utils instance
     * @param api The API instance
     */
    public DisplayUtils(TwitchBattleAPI api) {
        this.api = api;
    }

    /**
     * Display team points on a scoreboard
     * @param player The player to display the scoreboard to
     * @param title The scoreboard title
     */
    public void displayTeamPointsScoreboard(Player player, String title) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("teamPoints", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Map<Integer, Integer> teamPoints = api.getPointSystem().getTeamPointsMap();
        List<Map.Entry<Integer, Integer>> sortedPoints = new ArrayList<>(teamPoints.entrySet());
        
        // Sort by points (descending)
        Collections.sort(sortedPoints, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> e1, Map.Entry<Integer, Integer> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });
        
        int lineNumber = sortedPoints.size();
        for (Map.Entry<Integer, Integer> entry : sortedPoints) {
            int teamId = entry.getKey();
            int points = entry.getValue();
            org.emrage.twitchbattleapi.teams.Team apiTeam = api.getTeamManager().getTeam(teamId);
            
            if (apiTeam != null) {
                String displayName = formatText(apiTeam.getDisplayName(), apiTeam.getColor());
                String line = displayName + ": " + points;
                objective.getScore(line).setScore(lineNumber--);
            }
        }
        
        player.setScoreboard(scoreboard);
    }

    /**
     * Display player points on a scoreboard
     * @param player The player to display the scoreboard to
     * @param title The scoreboard title
     */
    public void displayPlayerPointsScoreboard(Player player, String title) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("playerPoints", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Map<UUID, Integer> playerPoints = api.getPointSystem().getPlayerPointsMap();
        List<Map.Entry<UUID, Integer>> sortedPoints = new ArrayList<>(playerPoints.entrySet());
        
        // Sort by points (descending)
        Collections.sort(sortedPoints, new Comparator<Map.Entry<UUID, Integer>>() {
            @Override
            public int compare(Map.Entry<UUID, Integer> e1, Map.Entry<UUID, Integer> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });
        
        int lineNumber = sortedPoints.size();
        for (Map.Entry<UUID, Integer> entry : sortedPoints) {
            UUID playerUUID = entry.getKey();
            int points = entry.getValue();
            
            String playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
            if (playerName != null) {
                objective.getScore(playerName + ": " + points).setScore(lineNumber--);
            }
        }
        
        player.setScoreboard(scoreboard);
    }

    /**
     * Format text with color
     * @param text The text to format
     * @param hexColor The color in hex format
     * @return The formatted text
     */
    public String formatText(String text, String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            return text;
        }
        
        // Simple hex color conversion for compatibility
        try {
            if (hexColor.startsWith("#")) {
                hexColor = hexColor.substring(1);
            }
            
            // Convert hex to bukkit color
            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
            
            ChatColor nearestColor = getNearestChatColor(r, g, b);
            return nearestColor + text;
        } catch (Exception e) {
            return text;
        }
    }

    /**
     * Get the nearest ChatColor to an RGB color
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @return The nearest ChatColor
     */
    private ChatColor getNearestChatColor(int r, int g, int b) {
        ChatColor nearestColor = ChatColor.WHITE;
        double minDistance = Double.MAX_VALUE;
        
        Map<ChatColor, int[]> colorMap = new HashMap<>();
        colorMap.put(ChatColor.BLACK, new int[]{0, 0, 0});
        colorMap.put(ChatColor.DARK_BLUE, new int[]{0, 0, 170});
        colorMap.put(ChatColor.DARK_GREEN, new int[]{0, 170, 0});
        colorMap.put(ChatColor.DARK_AQUA, new int[]{0, 170, 170});
        colorMap.put(ChatColor.DARK_RED, new int[]{170, 0, 0});
        colorMap.put(ChatColor.DARK_PURPLE, new int[]{170, 0, 170});
        colorMap.put(ChatColor.GOLD, new int[]{255, 170, 0});
        colorMap.put(ChatColor.GRAY, new int[]{170, 170, 170});
        colorMap.put(ChatColor.DARK_GRAY, new int[]{85, 85, 85});
        colorMap.put(ChatColor.BLUE, new int[]{85, 85, 255});
        colorMap.put(ChatColor.GREEN, new int[]{85, 255, 85});
        colorMap.put(ChatColor.AQUA, new int[]{85, 255, 255});
        colorMap.put(ChatColor.RED, new int[]{255, 85, 85});
        colorMap.put(ChatColor.LIGHT_PURPLE, new int[]{255, 85, 255});
        colorMap.put(ChatColor.YELLOW, new int[]{255, 255, 85});
        colorMap.put(ChatColor.WHITE, new int[]{255, 255, 255});
        
        for (Map.Entry<ChatColor, int[]> entry : colorMap.entrySet()) {
            int[] colorValues = entry.getValue();
            double distance = Math.sqrt(
                Math.pow(colorValues[0] - r, 2) +
                Math.pow(colorValues[1] - g, 2) +
                Math.pow(colorValues[2] - b, 2)
            );
            
            if (distance < minDistance) {
                minDistance = distance;
                nearestColor = entry.getKey();
            }
        }
        
        return nearestColor;
    }

    /**
     * Set up team prefixes in the tab list
     */
    public void setupTeamPrefixes() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        
        // Clear existing teams
        for (org.emrage.twitchbattleapi.teams.Team apiTeam : api.getTeamManager().getAllTeams()) {
            Team bukkitTeam = scoreboard.getTeam("tb_" + apiTeam.getId());
            if (bukkitTeam != null) {
                bukkitTeam.unregister();
            }
        }
        
        // Create new teams
        for (org.emrage.twitchbattleapi.teams.Team apiTeam : api.getTeamManager().getAllTeams()) {
            Team bukkitTeam = scoreboard.registerNewTeam("tb_" + apiTeam.getId());
            bukkitTeam.setPrefix(formatText(apiTeam.getDisplayName() + " ", apiTeam.getColor()));
            
            // Add players to team
            for (UUID playerUUID : apiTeam.getMembers()) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null) {
                    bukkitTeam.addEntry(player.getName());
                }
            }
        }
    }
}