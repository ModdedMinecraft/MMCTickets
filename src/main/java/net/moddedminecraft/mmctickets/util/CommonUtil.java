package net.moddedminecraft.mmctickets.util;


import net.moddedminecraft.mmctickets.config.Permissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;

public class CommonUtil {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) return null;

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "&ajust now&6";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "&a1 minute ago&6"; // a minute ago
        } else if (diff < 50 * MINUTE_MILLIS) {
            return "&a" + diff / MINUTE_MILLIS + " min ago&6";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "&a1 hour ago&6";
        } else if (diff < 24 * HOUR_MILLIS) {
            return "&e" + diff / HOUR_MILLIS + " hours ago&6";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "&cyesterday&6";
        } else {
            return "&c" + diff / DAY_MILLIS + " days ago&6";
        }
    }

    public static String shortenMessage(String message){
        if (message.length() >= 20) {
            message = message.substring(0, 20) + "...";
        }
        return message;
    }

    public static String isUserOnline(String name){
        for(Player player : Sponge.getServer().getOnlinePlayers()){
            if(name.equals(player.getName())) {
                return "&a";
            }
        }
        return "&c";
    }

    public static boolean checkUserOnline(String name){
        for(Player player : Sponge.getServer().getOnlinePlayers()){
            if(name.equals(player.getName())) {
                return true;
            }
        }
        return false;
    }

    public static String getTicketStatusColour(int ticketIDStatus){
        String ticketStatus = "";
        if (ticketIDStatus == 0) ticketStatus = "&eOpen";
        if (ticketIDStatus == 1) ticketStatus = "&eClaimed";
        if (ticketIDStatus == 2) ticketStatus = "&eHeld";
        if (ticketIDStatus == 3) ticketStatus = "&cClosed";

        return ticketStatus;
    }

    public static void notifyOnlineStaff(Text message) {
        for(Player player : Sponge.getServer().getOnlinePlayers()){
            if(player.hasPermission(Permissions.STAFF)) {
                player.sendMessage(message);
            }
        }
    }

    public static void notifyOnlineStaffTitle(Text message) {
        for(Player player : Sponge.getServer().getOnlinePlayers()){
            if(player.hasPermission(Permissions.STAFF)) {
                player.sendTitle(Title.builder().subtitle(message).fadeIn(20).fadeOut(20).stay(40).build());
            }
        }
    }

    public static void notifyOnlineStaffSound() {
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player.hasPermission(Permissions.STAFF)) {
                player.playSound(SoundTypes.BLOCK_NOTE_PLING, player.getLocation().getPosition(), 2);
            }
        }
    }
}
