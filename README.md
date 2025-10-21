# TwitchBattle API

Eine Minecraft-API für die Verwaltung von Teams, Spielern und Punkten für TwitchBattle-Events.

## Installation

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.YourUsername</groupId>
        <artifactId>TwitchBattleAPI</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.YourUsername:TwitchBattleAPI:v1.0.0'
}
```

## Verwendung

### Initialisierung

```java
// Im onEnable() deines Plugins
TwitchBattleAPI api = TwitchBattleAPI.init(this);
```

### Teams verwalten

```java
// Team erstellen
Team team = api.getTeamManager().createTeam("RedTeam", "Red Team", "#FF0000");

// Spieler zum Team hinzufügen
UUID playerId = player.getUniqueId();
api.getTeamManager().addPlayerToTeam(playerId, team.getId());

// Team des Spielers abfragen
Team playerTeam = api.getTeamManager().getPlayerTeam(player.getUniqueId());
```

### Punkte verwalten

```java
// Team-Punkte hinzufügen
api.getPointSystem().addTeamPoints(teamId, 100);

// Spieler-Punkte hinzufügen
api.getPointSystem().addPlayerPoints(player.getUniqueId(), 50);

// Punkte anzeigen
api.getDisplayUtils().displayTeamPointsScoreboard(player, "Team Standings");
```

## Wichtig: API herunterfahren

```java
// Im onDisable() deines Plugins
api.shutdown();
```