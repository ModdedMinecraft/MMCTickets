package net.moddedminecraft.mmctickets;

import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.data.ticketStatus;
import net.moddedminecraft.mmctickets.util.CommonUtil;
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
        //If the playerdata for the player exists, Check if they have changed their name.
        Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
            public void run() {
                boolean exists = false;
                final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getDataStore().getPlayerData());
                for (PlayerData pData : playerData) {
                    if (pData.getPlayerUUID().equals(player.getUniqueId()) && !pData.getPlayerName().equals(player.getName())) {
                        exists = true;
                        pData.setPlayerName(player.getName());
                        try {
                            plugin.getDataStore().updatePlayerData(pData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!exists) {
                    CommonUtil.checkPlayerData(plugin, player);
                }
            }
        }).delay(15, TimeUnit.SECONDS).name("mmctickets-s-checkUserNameOnLogin").submit(this.plugin);

        //Notify a player if a ticket they created was closed while they were offline
        if (plugin.getDataStore().getNotifications().contains(player.getUniqueId())) {
            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());
            int totalTickets = 0;
            for (TicketData ticket : tickets) {
                if (ticket.getPlayerUUID().equals(player.getUniqueId()) && ticket.getNotified() == 0) {
                    totalTickets++;
                    ticket.setNotified(1);
                    try {
                        plugin.getDataStore().updateTicketData(ticket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            plugin.getDataStore().getNotifications().removeAll(Collections.singleton(player.getUniqueId()));
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
            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());
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
