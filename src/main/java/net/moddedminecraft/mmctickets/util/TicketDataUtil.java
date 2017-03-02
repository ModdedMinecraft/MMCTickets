package net.moddedminecraft.mmctickets.util;

public class TicketDataUtil {

    protected String name, world, staffname, comment, message;
    protected int ticketID, x, y, z, status, notified;
    protected Double yaw, pitch;
    protected long timestamp;

    public TicketDataUtil(int ticketID, String name, String staffname, String comment, long timestamp, String world, int x, int y, int z, Double yaw, Double pitch, String message, int status, int notified) {
        this.ticketID = ticketID;
        this.name = name;
        this.staffname = staffname;
        this.comment = comment;
        this.timestamp = timestamp;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.message = message;
        this.status = status;
        this.notified = notified;
    }

    public int getTicketID() {
        return ticketID;
    }

    public String getName() {
        return name;
    }

    public String getStaffName() {
        return staffname;
    }

    public String getComment() {
        return comment.replaceAll("(\\[)(.*)(\\])", "$2");
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getWorld() {
        return world;
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

    public int getStatus() {
        return status;
    }

    public int getNotified() {
        return notified;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setNotified(int notified) {
        this.notified = notified;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setStaffName(String staffname) {
        this.staffname = staffname;
    }

}
