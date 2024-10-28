package net.moddedminecraft.mmctickets.util;


import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.title.Title;
import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Config;
import net.moddedminecraft.mmctickets.config.Permissions;
import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.ticketStatus;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.GameProfileManager;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static net.moddedminecraft.mmctickets.data.ticketStatus.*;

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

    public static String isUserOnline(UUID uuid){
        for(ServerPlayer player : Sponge.server().onlinePlayers()){
            if(uuid.equals(player.uniqueId())) {
                return "&a";
            }
        }
        return "&c";
    }

    public static boolean checkUserOnline(String name){
        for(ServerPlayer player : Sponge.server().onlinePlayers()){
            if(name.equals(player.name())) {
                return true;
            }
        }
        return false;
    }

    public static String checkTicketServer(String server) {
        if (server.equalsIgnoreCase(Config.server)) {
            return "&a" + server;
        }
        return "&c" + server;
    }

    public static String getTicketStatusColour(ticketStatus ticketIDStatus){
        String ticketStatus = "";
        if (ticketIDStatus == Open) ticketStatus = "&eOpen";
        if (ticketIDStatus == Claimed) ticketStatus = "&eClaimed";
        if (ticketIDStatus == Held) ticketStatus = "&eHeld";
        if (ticketIDStatus == Closed) ticketStatus = "&cClosed";

        return ticketStatus;
    }

    public static void notifyOnlineStaff(Component message) {
        for(ServerPlayer player : Sponge.server().onlinePlayers()){
            if(player.hasPermission(Permissions.STAFF)) {
                Component clickableText = message
                        .clickEvent(ClickEvent.runCommand("/ticket check"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click here to get a list of all open tickets")));
                player.sendMessage(clickableText);
            }
        }
    }

    public static void notifyOnlineStaffOpen(Component message, int ticketID) {
        for(ServerPlayer player : Sponge.server().onlinePlayers()){
            if(player.hasPermission(Permissions.STAFF)) {
                Component clickableText = message
                        .clickEvent(ClickEvent.runCommand("/ticket check " + ticketID))
                        .hoverEvent(HoverEvent.showText(Component.text("Click here to get more details for ticket #" + ticketID)));
                player.sendMessage(clickableText);
            }
        }
    }

    public static void notifyOnlineStaffTitle(Component message) {
        for(ServerPlayer player : Sponge.server().onlinePlayers()){
            if(player.hasPermission(Permissions.STAFF)) {
                Title title = Title.title(message, Component.empty(), Title.Times.times(Duration.ofSeconds(20),Duration.ofSeconds(40),Duration.ofSeconds(20)));
                player.showTitle(title);
            }
        }
    }

    public static void notifyOnlineStaffSound() {
        for (ServerPlayer player : Sponge.server().onlinePlayers()) {

            Sound note = Sound.sound()
                    .volume(2)
                    .type(SoundTypes.BLOCK_NOTE_BLOCK_PLING)
                    .build();

            if (player.hasPermission(Permissions.STAFF)) {
                player.playSound(note, player.location().position());
            }
        }
    }

    /*public static String getNameFromUUID(UUID uuid) {
        if (uuid.toString().equals("00000000-0000-0000-0000-000000000000")) {
            return "Console";
        }

        Optional<Player> onlinePlayer = Sponge.getServer().getPlayer(uuid);
        if (onlinePlayer.isPresent()) {
            return Sponge.getServer().getPlayer(uuid).get().getName();
        }
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorage.get().get(uuid).get().getName();
    }*/

    public static void checkPlayerData(Main plugin, ServerPlayer player) {
        List<PlayerData> playerData = plugin.getDataStore().getPlayerData();
        boolean exists = false;
        for (PlayerData pData : playerData) {
            if (pData.getPlayerUUID().equals(player.uniqueId())) {
                exists = true;
            }
        }
        if (!exists) {
            plugin.getDataStore().addPlayerData(new PlayerData(player.uniqueId(), player.name(), 0));
        }
    }

    public static String getPlayerNameFromData(Main plugin, UUID uuid) {
        if (uuid.toString().equals("00000000-0000-0000-0000-000000000000")) {
            return "Console";
        }
        List<PlayerData> playerData = plugin.getDataStore().getPlayerData();
        for (PlayerData pData : playerData) {
            if (pData.getPlayerUUID().equals(uuid)) {
                return pData.getPlayerName();
            }
        }
        return "Unavailable";
    }

    /*public static UUID getUUIDFromName(String name) throws ExecutionException, InterruptedException {
        Optional<ServerPlayer> onlinePlayer = Sponge.server().player(name);
        if (onlinePlayer.isPresent()) {
            return Sponge.server().player(name).get().uniqueId();
        }
        GameProfileManager profileManager = Sponge.server().gameProfileManager();
        return profileManager.profile(name).get().uuid();
    }*/
}
