package net.moddedminecraft.mmctickets;

import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.data.ticketStatus;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventListener {

    private Main plugin;
    public EventListener(Main instance) {
        plugin = instance;
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join event, @Root Player player) {
        //Save the players data as they login for the first time, If the player already exists, Check if they have changed their name.
        if (!plugin.playersData.containsKey(player.getUniqueId())) {
            plugin.addPlayerData(new PlayerData(player.getUniqueId(), player.getName(), 0));
            try {
                plugin.saveData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                public void run() {
                    final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getPlayerData());
                    for (PlayerData pData : playerData) {
                        if (pData.getPlayerUUID().equals(player.getUniqueId()) && !pData.getPlayerName().equals(player.getName())) {
                            pData.setPlayerName(player.getName());
                            try {
                                plugin.saveData();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).delay(15, TimeUnit.SECONDS).name("mmctickets-s-checkUserNameOnLogin").submit(this.plugin);

        }

        //Notify a player if a ticket they created was closed while they were offline
        if (plugin.getNotifications().contains(player.getUniqueId())) {
            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());
            int totalTickets = 0;
            for (TicketData ticket : tickets) {
                if (ticket.getPlayerUUID().equals(player.getUniqueId()) && ticket.getNotified() == 0) {
                    totalTickets++;
                    ticket.setNotified(1);
                }
            }
            try {
                plugin.saveData();
            } catch (Exception e) {
                e.printStackTrace();
            }
            plugin.getNotifications().removeAll(Collections.singleton(player.getUniqueId()));
            final int finalTotalTickets = totalTickets;
            Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                public void run() {
                    if (finalTotalTickets < 2) {
                        player.sendMessage(Messages.getTicketCloseOffline());
                    } else {
                        player.sendMessage(Messages.getTicketCloseOfflineMulti(finalTotalTickets, "check self"));
                    }
                }
            }).delay(5, TimeUnit.SECONDS).name("mmctickets-s-sendUserNotifications").submit(this.plugin);
        }

        //Notify staff of the current open tickets when they login
        if (player.hasPermission(Permissions.STAFF)) {
            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());
            int openTickets = 0;
            int heldTickets = 0;
            for (TicketData ticket : tickets) {
                if (ticket.getStatus() == ticketStatus.Open) openTickets++;
                if (ticket.getStatus() == ticketStatus.Held) heldTickets++;
            }
            final int finalOpen = openTickets;
            final int finalHeld = heldTickets;
            Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                public void run() {

                    if (finalOpen == 0) {
                        player.sendMessage(Messages.getTicketReadNone());
                    }
                    if (finalOpen > 0 && finalHeld == 0) {
                        player.sendMessage(Messages.getTicketUnresolved(finalOpen, "check"));
                    }
                    if (finalOpen > 0 && finalHeld > 0) {
                        player.sendMessage(Messages.getTicketUnresolvedHeld(finalOpen, finalHeld, "check"));
                    }
                }
            }).delay(3, TimeUnit.SECONDS).name("mmctickets-s-sendStaffNotifications").submit(this.plugin);
        }

        //Send update notification to players with permission
        if (player.hasPermission(Permissions.NOTIFY)) {
            plugin.updatechecker.startUpdateCheckPlayer(player);
        }
    }
}
