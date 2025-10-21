package org.emrage.twitchbattleapi.config;

/**
 * Datenbankverbindungskonfiguration
 * Diese Klasse verwaltet die Datenbankverbindungsdetails.
 */
public class DatabaseConfig {
    // Standard-Entwicklungswerte (Dummy-Werte)
    private static String DB_HOST = "localhost";
    private static int DB_PORT = 27017;
    private static String DB_NAME = "TwitchBattle";
    private static String DB_USER = "";
    private static String DB_PASSWORD = "";

    /**
     * Setzt benutzerdefinierte Konfigurationswerte
     * @param host Datenbank-Host
     * @param port Datenbank-Port
     * @param name Datenbankname
     * @param user Benutzername
     * @param password Passwort
     */
    public static void setCustomConfig(String host, int port, String name, String user, String password) {
        DB_HOST = host;
        DB_PORT = port;
        DB_NAME = name;
        DB_USER = user;
        DB_PASSWORD = password;
    }

    /**
     * Gibt den MongoDB-Verbindungsstring zurück
     * @return MongoDB connection string
     */
    public static String getConnectionString() {
        return "mongodb://" + DB_USER + ":" + DB_PASSWORD + "@" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?authSource=" + DB_NAME;
    }

    /**
     * Gibt den Datenbank-Host zurück
     * @return Database host
     */
    public static String getHost() {
        return DB_HOST;
    }

    /**
     * Gibt den Datenbank-Port zurück
     * @return Database port
     */
    public static int getPort() {
        return DB_PORT;
    }

    /**
     * Gibt den Datenbanknamen zurück
     * @return Database name
     */
    public static String getDatabaseName() {
        return DB_NAME;
    }

    /**
     * Gibt den Datenbankbenutzer zurück
     * @return Database username
     */
    public static String getUsername() {
        return DB_USER;
    }

    /**
     * Gibt das Datenbankpasswort zurück
     * @return Database password
     */
    public static String getPassword() {
        return DB_PASSWORD;
    }
}