# TwitchBattleAPI

Eine professionelle API für Minecraft-Server zur Verwaltung von Teams und Punktesystemen für TwitchBattle-Events.

<p align="center">
  <img src="https://via.placeholder.com/200x200.png?text=TwitchBattleAPI" alt="TwitchBattleAPI Logo" width="200"/>
</p>

<p align="center">
  <a href="https://jitpack.io/#EmrageGHC/TwitchBattleAPI"><img src="https://jitpack.io/v/EmrageGHC/TwitchBattleAPI.svg" alt="Release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/EmrageGHC/TwitchBattleAPI" alt="License"></a>
  <a href="https://github.com/EmrageGHC/TwitchBattleAPI/issues"><img src="https://img.shields.io/github/issues/EmrageGHC/TwitchBattleAPI" alt="Issues"></a>
</p>

## Übersicht

Die TwitchBattleAPI bietet eine robuste Infrastruktur für Minecraft-Server, um Team-basierte Events zu verwalten. Mit einer nahtlosen MongoDB-Integration ermöglicht die API das Erstellen und Verwalten von Teams, das Tracken von Punkten und die visuelle Integration in die Spieloberfläche über Tab-Liste und Chat.

## Funktionen

- **Team-Management**: Erstellen, bearbeiten und löschen von Teams mit individuellen Farben und Namen
- **Punktesystem**: Verwalten von Punkten für Teams und individuelle Spieler
- **Spielerintegrationen**: Automatische Aktualisierung der Tab-Liste und Chat-Formatierung
- **Persistenz**: Zuverlässige Datenspeicherung durch MongoDB-Integration
- **Erweiterbarkeit**: Einfache API für die Integration in bestehende Plugins

## Dokumentation

### Installation

#### Mit Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.EmrageGHC:TwitchBattleAPI:v1.1.0'
}
```

#### Mit Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.EmrageGHC</groupId>
        <artifactId>TwitchBattleAPI</artifactId>
        <version>v1.1.0</version>
    </dependency>
</dependencies>
```

### Konfiguration

Erstelle eine `config.yml` im Ressourcenverzeichnis deines Plugins:

```yaml
database:
  host: "mongodb-server-adresse"
  port: 27017
  name: "TwitchBattle"
  username: "datenbank-benutzer"
  password: "datenbank-passwort"
```

### Initialisierung

```java
import org.bukkit.plugin.java.JavaPlugin;
import org.emrage.twitchbattleapi.TwitchBattleAPI;

public class DeinPlugin extends JavaPlugin {
    private TwitchBattleAPI api;
    
    @Override
    public void onEnable() {
        // Konfigurationsdatei speichern
        saveDefaultConfig();
        
        // API initialisieren
        api = TwitchBattleAPI.init(this);
        
        if (api != null) {
            getLogger().info("TwitchBattleAPI erfolgreich initialisiert!");
        } else {
            getLogger().severe("TwitchBattleAPI konnte nicht initialisiert werden!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        if (api != null) {
            api.shutdown();
        }
    }
    
    public TwitchBattleAPI getApi() {
        return api;
    }
}
```

## API-Referenz

### Team-Management

```java
// Team erstellen
Team team = api.getTeamManager().createTeam("team1", "Rotes Team", "#FF0000");

// Spieler einem Team hinzufügen
boolean success = api.getTeamManager().addPlayerToTeam(playerUUID, teamId);

// Teams abrufen
Team specificTeam = api.getTeamManager().getTeam(teamId);
Team teamByName = api.getTeamManager().getTeamByName("team1");
List<Team> allTeams = api.getTeamManager().getAllTeams();

// Team eines Spielers abrufen
Team playerTeam = api.getTeamManager().getPlayerTeam(playerUUID);

// Team aktualisieren
team.setDisplayName("Neuer Name");
team.setColor("#00FF00");
api.getTeamManager().updateTeam(team);

// Spieler aus Team entfernen
api.getTeamManager().removePlayerFromTeam(playerUUID);

// Team löschen
api.getTeamManager().deleteTeam(teamId);
```

### Punktesystem

```java
// Teampunkte verwalten
api.getPointSystem().addTeamPoints(teamId, 10);
api.getPointSystem().setTeamPoints(teamId, 100);
int teamPoints = api.getPointSystem().getTeamPoints(teamId);

// Spielerpunkte verwalten
api.getPointSystem().addPlayerPoints(playerUUID, 5);
api.getPointSystem().setPlayerPoints(playerUUID, 50);
int playerPoints = api.getPointSystem().getPlayerPoints(playerUUID);

// Top Teams abrufen
Map<Integer, Integer> topTeams = api.getPointSystem().getTopTeams(5);

// Top Spieler abrufen
Map<UUID, Integer> topPlayers = api.getPointSystem().getTopPlayers(5);
```

### Anzeigeoptionen

```java
// Team-Präfixe einrichten
api.getDisplayUtils().setupTeamPrefixes();

// Scoreboard mit Punkten anzeigen
api.getDisplayUtils().displayTeamPointsScoreboard(player, "Teampunkte");
api.getDisplayUtils().displayPlayerPointsScoreboard(player, "Spielerpunkte");

// Scoreboard aktualisieren
api.getDisplayUtils().updateScoreboards();
```

## Integration mit anderen Systemen

### TabList-Manager

```java
public class TabListManager {
    private final TwitchBattleAPI api;
    private final JavaPlugin plugin;
    private final Scoreboard scoreboard;
    
    public TabListManager(JavaPlugin plugin, TwitchBattleAPI api) {
        this.plugin = plugin;
        this.api = api;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        setupTeams();
        startUpdateTask();
    }
    
    private void setupTeams() {
        // Teams für Tab-Liste einrichten
        // ...
    }
    
    public void updatePlayerTeam(Player player) {
        // Team eines Spielers in der Tab-Liste aktualisieren
        // ...
    }
    
    public void startUpdateTask() {
        // Regelmäßige Aktualisierung der Tab-Liste starten
        // ...
    }
}
```

### Chat-Formatter

```java
public class ChatFormatter implements Listener {
    private final TwitchBattleAPI api;
    
    public ChatFormatter(JavaPlugin plugin, TwitchBattleAPI api) {
        this.api = api;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Chat-Nachricht mit Team-Präfix formatieren
        // ...
    }
}
```

## Beispielimplementierungen

### Punkte für Aktionen vergeben

```java
@EventHandler
public void onPlayerKill(PlayerDeathEvent event) {
    Player killed = event.getEntity();
    Player killer = killed.getKiller();
    
    if (killer != null) {
        // Punkte für einen Kill vergeben
        api.getPointSystem().addPlayerPoints(killer.getUniqueId(), 5);
        
        // Optional: Auch Teampunkte vergeben
        Team killerTeam = api.getTeamManager().getPlayerTeam(killer.getUniqueId());
        if (killerTeam != null) {
            api.getPointSystem().addTeamPoints(killerTeam.getId(), 3);
            killer.sendMessage("§aDu hast 5 Punkte für einen Kill erhalten! Dein Team erhält 3 Punkte.");
        }
    }
}
```

### Team-basierte Effekte

```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    Team team = api.getTeamManager().getPlayerTeam(player.getUniqueId());
    
    if (team != null) {
        // Spezielle Effekte für Teammitglieder
        player.sendMessage("§6Willkommen zurück zum Event! Du bist im Team §r" + 
                           getColorFromHex(team.getColor()) + team.getDisplayName());
    } else {
        // Standard-Nachricht für Spieler ohne Team
        player.sendMessage("§6Willkommen zum Event! Du bist noch keinem Team zugewiesen.");
    }
}
```

## Systemanforderungen

- **Java**: Version 17 oder höher
- **Minecraft**: 1.20.x oder höher (getestet bis 1.21.8)
- **Server**: Paper, Spigot oder Bukkit
- **Datenbank**: MongoDB 4.4 oder höher

## Fehlerbehebung

### Verbindungsprobleme mit der Datenbank

Wenn du Probleme bei der Verbindung zur Datenbank hast:
1. Überprüfe die Verbindungsdaten in der `config.yml`
2. Stelle sicher, dass dein Server Zugriff auf die MongoDB-Instanz hat
3. Prüfe, ob Firewall-Regeln den Zugriff blockieren

### API wird nicht initialisiert

Falls die API nicht korrekt initialisiert wird:
1. Überprüfe, ob `saveDefaultConfig()` vor der Initialisierung aufgerufen wird
2. Stelle sicher, dass die `config.yml` im richtigen Format ist
3. Aktiviere das Debug-Logging in der `log4j.properties` oder `bukkit.yml`

## Mitwirken

Beiträge zum Projekt sind willkommen! Wenn du einen Fehler findest oder eine Verbesserung vorschlagen möchtest:

1. Erstelle ein Issue mit einer detaillierten Beschreibung
2. Fork das Repository und erstelle einen Branch für deine Änderung
3. Implementiere deine Änderungen und teste sie gründlich
4. Erstelle einen Pull Request mit einer ausführlichen Beschreibung deiner Änderungen

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert - siehe [LICENSE](LICENSE) für Details.

## Kontakt und Support

Bei Fragen, Problemen oder Anregungen kannst du:
- Ein [GitHub Issue](https://github.com/EmrageGHC/TwitchBattleAPI/issues) erstellen
- Mich auf Discord kontaktieren: `EmrageGHC`
- Eine E-Mail senden an: `kontakt@emrage.org`

---

<p align="center">
  Entwickelt von <a href="https://github.com/EmrageGHC">EmrageGHC</a>
  <br>
  © 2025 • Alle Rechte vorbehalten
</p>
