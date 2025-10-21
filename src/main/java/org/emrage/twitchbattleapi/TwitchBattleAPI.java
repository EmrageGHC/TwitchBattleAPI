package org.emrage.twitchbattleapi;

import org.bukkit.plugin.java.JavaPlugin;
import org.emrage.twitchbattleapi.config.DatabaseConfig;
import org.emrage.twitchbattleapi.database.DatabaseManager;
import org.emrage.twitchbattleapi.points.PointSystem;
import org.emrage.twitchbattleapi.teams.TeamManager;
import org.emrage.twitchbattleapi.utils.DisplayUtils;

/**
 * Main API class for TwitchBattle
 */
public class TwitchBattleAPI {
    private static TwitchBattleAPI instance;
    private final JavaPlugin plugin;
    private DatabaseManager databaseManager;
    private PointSystem pointSystem;
    private TeamManager teamManager;
    private DisplayUtils displayUtils;

    /**
     * Private constructor for singleton pattern
     * @param plugin The plugin that is using this API
     */
    private TwitchBattleAPI(JavaPlugin plugin) {
        this.plugin = plugin;
        try {
            this.databaseManager = new DatabaseManager();
            this.databaseManager.connect();
            this.databaseManager.createTables();

            this.teamManager = new TeamManager(this);
            this.pointSystem = new PointSystem(this);
            this.displayUtils = new DisplayUtils(this);

            plugin.getLogger().info("[TwitchBattleAPI] Successfully initialized API with MongoDB");
        } catch (Exception e) {
            plugin.getLogger().severe("[TwitchBattleAPI] Failed to initialize API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize the API with default database configuration
     * @param plugin The plugin that is using this API
     * @return The API instance
     */
    public static TwitchBattleAPI init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new TwitchBattleAPI(plugin);
        }
        return instance;
    }

    /**
     * Initialize the API with custom database configuration
     * @param plugin The plugin that is using this API
     * @param databaseConfig Custom database configuration string
     * @return The API instance
     */
    public static TwitchBattleAPI initWithCustomConfig(JavaPlugin plugin, String databaseConfig) {
        if (instance == null) {
            instance = new TwitchBattleAPI(plugin);
            instance.databaseManager.setConnectionString(databaseConfig);
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
     * Get the display utils
     * @return The display utils
     */
    public DisplayUtils getDisplayUtils() {
        return displayUtils;
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