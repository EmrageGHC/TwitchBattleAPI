package org.emrage.twitchbattleapi.config;

/**
 * Datenbankverbindungskonfiguration
 * WICHTIG: Diese Datei sollte in .gitignore aufgenommen werden!
 */
public class DatabaseConfig {
    // MongoDB Verbindungsdaten
    private static final String DB_HOST = "deine-server-ip";
    private static final int DB_PORT = 30065;
    private static final String DB_NAME = "TwitchBattle";
    private static final String DB_USER = "twitchbattle_dev";
    private static final String DB_PASSWORD = "Cg2W2LciGEGkh26U";

    /**
     * Gibt den MongoDB-Verbindungsstring zurück
     */
    public static String getConnectionString() {
        return "mongodb://" + DB_USER + ":" + DB_PASSWORD + "@" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?authSource=" + DB_NAME;
    }

    /**
     * Gibt den Datenbank-Host zurück
     */
    public static String getHost() {
        return DB_HOST;
    }

    /**
     * Gibt den Datenbank-Port zurück
     */
    public static int getPort() {
        return DB_PORT;
    }

    /**
     * Gibt den Datenbanknamen zurück
     */
    public static String getDatabaseName() {
        return DB_NAME;
    }

    /**
     * Gibt den Datenbankbenutzer zurück
     */
    public static String getUsername() {
        return DB_USER;
    }

    /**
     * Gibt das Datenbankpasswort zurück
     */
    public static String getPassword() {
        return DB_PASSWORD;
    }
}