package net.moddedminecraft.mmctickets.util;

import java.util.UUID;

public class PlayerDataUtil {
    protected UUID playerUUID;
    protected String playerName;
    protected int bannedStatus;

    public PlayerDataUtil(UUID playerUUID, String playerName, int bannedStatus) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.bannedStatus = bannedStatus;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getBannedStatus() {
        return bannedStatus;
    }

    public void setBannedStatus(int bannedStatus) {
        this.bannedStatus = bannedStatus;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
