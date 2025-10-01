package org.emrage.twitchbattleapi.teams;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a team in the TwitchBattle
 */
public class Team {
    private int id;
    private String name;
    private String displayName;
    private String color;
    private List<UUID> members;

    /**
     * Create a new team
     * @param id The team ID
     * @param name The team name
     * @param displayName The team display name
     * @param color The team color in hex format
     */
    public Team(int id, String name, String displayName, String color) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.members = new ArrayList<>();
    }

    /**
     * Get the team ID
     * @return The team ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the team name
     * @return The team name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the team name
     * @param name The new team name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the team display name
     * @return The team display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the team display name
     * @param displayName The new team display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the team color
     * @return The team color in hex format
     */
    public String getColor() {
        return color;
    }

    /**
     * Set the team color
     * @param color The new team color in hex format
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Get the team members
     * @return List of player UUIDs in the team
     */
    public List<UUID> getMembers() {
        return members;
    }

    /**
     * Add a member to the team
     * @param playerUUID The player UUID
     */
    public void addMember(UUID playerUUID) {
        if (!members.contains(playerUUID)) {
            members.add(playerUUID);
        }
    }

    /**
     * Remove a member from the team
     * @param playerUUID The player UUID
     * @return True if the player was removed, false otherwise
     */
    public boolean removeMember(UUID playerUUID) {
        return members.remove(playerUUID);
    }

    /**
     * Check if a player is a member of the team
     * @param playerUUID The player UUID
     * @return True if the player is a member, false otherwise
     */
    public boolean isMember(UUID playerUUID) {
        return members.contains(playerUUID);
    }
}