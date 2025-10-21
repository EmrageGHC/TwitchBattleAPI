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
    }

    /**
     * Initialize the API
     * @param plugin The plugin that is using this API
     * @return The API instance
     */
    public static TwitchBattleAPI init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new TwitchBattleAPI(plugin);

            // Lade Konfiguration aus config.yml des Plugins
            if (plugin.getConfig().contains("database")) {
                String host = plugin.getConfig().getString("database.host", "localhost");
                int port = plugin.getConfig().getInt("database.port", 27017);
                String name = plugin.getConfig().getString("database.name", "TwitchBattle");
                String username = plugin.getConfig().getString("database.username", "");
                String password = plugin.getConfig().getString("database.password", "");

                // Setze die Datenbankkonfiguration
                DatabaseConfig.setCustomConfig(host, port, name, username, password);
            }

            // Initialisiere Datenbankverbindung und Komponenten
            instance.initializeComponents();
        }
        return instance;
    }

    /**
     * Initialize the database and components
     */
    private void initializeComponents() {
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