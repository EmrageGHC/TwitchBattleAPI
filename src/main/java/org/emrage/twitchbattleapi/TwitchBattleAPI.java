package org.emrage.twitchbattleapi;

import org.bukkit.plugin.java.JavaPlugin;
import org.emrage.twitchbattleapi.database.DatabaseManager;
import org.emrage.twitchbattleapi.points.PointSystem;
import org.emrage.twitchbattleapi.teams.TeamManager;

/**
 * Main API class for TwitchBattle
 */
public class TwitchBattleAPI {
    private static TwitchBattleAPI instance;
    private final JavaPlugin plugin;
    private DatabaseManager databaseManager;
    private PointSystem pointSystem;
    private TeamManager teamManager;

    /**
     * Private constructor for singleton pattern
     * @param plugin The plugin that is using this API
     * @param databaseConfig Database configuration string
     */
    private TwitchBattleAPI(JavaPlugin plugin, String databaseConfig) {
        this.plugin = plugin;
        this.databaseManager = new DatabaseManager(databaseConfig);
        this.pointSystem = new PointSystem(this);
        this.teamManager = new TeamManager(this);
    }

    /**
     * Initialize the API
     * @param plugin The plugin that is using this API
     * @param databaseConfig Database configuration string
     * @return The API instance
     */
    public static TwitchBattleAPI init(JavaPlugin plugin, String databaseConfig) {
        if (instance == null) {
            instance = new TwitchBattleAPI(plugin, databaseConfig);
            instance.databaseManager.connect();
            instance.databaseManager.createTables();
        }
        return instance;
    }

    /**
     * Get the API instance
     * @return The API instance
     */
    public static TwitchBattleAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TwitchBattleAPI is not initialized. Call init() first.");
        }
        return instance;
    }

    /**
     * Get the plugin that is using this API
     * @return The plugin
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Get the database manager
     * @return The database manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Get the point system
     * @return The point system
     */
    public PointSystem getPointSystem() {
        return pointSystem;
    }

    /**
     * Get the team manager
     * @return The team manager
     */
    public TeamManager getTeamManager() {
        return teamManager;
    }

    /**
     * Shutdown the API properly
     */
    public void shutdown() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }
}