package net.moddedminecraft.mmctickets;

import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.data.ticketStatus;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.scheduler.Task;

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
    public void onPlayerLogin(ServerSideConnectionEvent.Join event) {
        ServerPlayer player = event.player();
        //If the playerdata for the player exists, Check if they have changed their name.
        Sponge.asyncScheduler().executor(plugin.container).schedule(new Runnable() {
            public void run() {
                boolean exists = false;
                final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getDataStore().getPlayerData());
                for (PlayerData pData : playerData) {
                    if (pData.getPlayerUUID().equals(player.uniqueId()) && !pData.getPlayerName().equals(player.name())) {
                        exists = true;
                        pData.setPlayerName(player.name());
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
        },15, TimeUnit.SECONDS);

        //Notify a player if a ticket they created was closed while they were offline
        if (plugin.getDataStore().getNotifications().contains(player.uniqueId())) {
            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());
            int totalTickets = 0;
            for (TicketData ticket : tickets) {
                if (ticket.getPlayerUUID().equals(player.uniqueId()) && ticket.getNotified() == 0) {
                    totalTickets++;
                    ticket.setNotified(1);
                    try {
                        plugin.getDataStore().updateTicketData(ticket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            plugin.getDataStore().getNotifications().removeAll(Collections.singleton(player.uniqueId()));
            final int finalTotalTickets = totalTickets;
            Sponge.asyncScheduler().executor(plugin.container).schedule(new Runnable() {
                public void run() {
                    if (finalTotalTickets < 2) {
                        player.sendMessage(Messages.getTicketCloseOffline());
                    } else {
                        player.sendMessage(Messages.getTicketCloseOfflineMulti(finalTotalTickets, "check self"));
                    }
                }
            },5, TimeUnit.SECONDS);//.delay(5, TimeUnit.SECONDS).build();
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
            Sponge.asyncScheduler().executor(plugin.container).schedule(new Runnable() {
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
            },3, TimeUnit.SECONDS);

        }

        //Send update notification to players with permission
        //if (player.hasPermission(Permissions.NOTIFY)) {
        //    plugin.updatechecker.startUpdateCheckPlayer(player);
        //}
    }
}
