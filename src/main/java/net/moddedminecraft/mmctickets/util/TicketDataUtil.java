package net.moddedminecraft.mmctickets.util;

import net.moddedminecraft.mmctickets.data.ticketStatus;

import java.util.UUID;

public class TicketDataUtil {

    protected String playerUUID, worldnamespace, worldvalue, staffUUID, comment, message, server;
    protected int ticketID, x, y, z, notified;
    protected Double yaw, pitch;
    protected long timestamp;
    protected ticketStatus status;


    public TicketDataUtil(int ticketID, String playerUUID, String staffUUID, String comment, long timestamp, String worldnamespace, String worldvalue, int x, int y, int z, Double yaw, Double pitch, String message, ticketStatus status, int notified, String server) {
        this.ticketID = ticketID;
        this.playerUUID = playerUUID;
        this.staffUUID = staffUUID;
        this.comment = comment;
        this.timestamp = timestamp;
        this.worldnamespace = worldnamespace;
        this.worldvalue = worldvalue;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.message = message;
        this.status = status;
        this.notified = notified;
        this.server = server;
    }

    public int getTicketID() {
        return ticketID;
    }

    public UUID getPlayerUUID() {
        return UUID.fromString(playerUUID);
    }

    public UUID getStaffUUID() {
        return UUID.fromString(staffUUID);
    }

    public String getOldPlayer() {
        return playerUUID;
    }

    public String getOldStaffname() {
        return staffUUID;
    }

    public String getComment() {
        return comment.replaceAll("(\\[)(.*)(\\])", "$2");
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getWorldNamespace() {
        return worldnamespace;
    }

    public String getWorldValue() {
        return worldvalue;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Double getYaw() {
        return yaw;
    }

    public Double getPitch() {
        return pitch;
    }

    public String getMessage() {
        return message.replaceAll("(\\[)(.*)(\\])", "$2");
    }

    public ticketStatus getStatus() {
        return status;
    }

    public int getNotified() {
        return notified;
    }

    public void setStatus(ticketStatus status) {
        this.status = status;
    }

    public void setNotified(int notified) {
        this.notified = notified;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setStaffUUID(String uuid) {
        this.staffUUID = uuid;
    }

    public void setPlayerUUID(UUID uuid) {
        this.playerUUID = String.valueOf(uuid);
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
