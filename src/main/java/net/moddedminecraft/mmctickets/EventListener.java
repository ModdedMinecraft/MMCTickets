package net.moddedminecraft.mmctickets;

import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
import net.moddedminecraft.mmctickets.data.TicketData;
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

        if (plugin.getNotifications().contains(player.getName())) {
            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());

            int totalTickets = 0;
            for (TicketData ticket : tickets) {
                if (ticket.getName().equals(player.getName()) && ticket.getNotified() == 0) {
                    totalTickets++;
                    ticket.setNotified(1);
                }
            }

            try {
                plugin.saveData();
            } catch (Exception e) {
                e.printStackTrace();
            }

            plugin.getNotifications().removeAll(Collections.singleton(player.getName()));

            final int finalTotalTickets = totalTickets;
            Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                public void run() {
                    if (finalTotalTickets < 2) {
                        player.sendMessage(Messages.parse(Messages.ticketCloseOffline));
                    } else {
                        player.sendMessage(Messages.parse(Messages.ticketCloseOfflineMulti, finalTotalTickets, "check self"));
                    }
                }
            }).delay(5, TimeUnit.SECONDS).name("mmctickets-s-sendUserNotifications").submit(this.plugin);

        }

        if (player.hasPermission(Permissions.STAFF)) {
            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());
            int openTickets = 0;
            int heldTickets = 0;
            for (TicketData ticket : tickets) {
                if (ticket.getStatus() == 0) openTickets++;
                if (ticket.getStatus() == 2) heldTickets++;
            }
            final int finalOpen = openTickets;
            final int finalHeld = heldTickets;
            Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                public void run() {
                    if (finalOpen == 0) {
                        player.sendMessage(Messages.parse(Messages.ticketReadNone));
                    }
                    if (finalOpen > 0 && finalHeld == 0) {
                        player.sendMessage(Messages.parse(Messages.ticketUnresolved, finalOpen, "check"));
                    }
                    if (finalOpen > 0 && finalHeld > 0) {
                        player.sendMessage(Messages.parse(Messages.ticketUnresolvedHeld, finalOpen, finalHeld, "check"));
                    }
                }
            }).delay(3, TimeUnit.SECONDS).name("mmctickets-s-sendStaffNotifications").submit(this.plugin);
        }
        //TODO Add login notification for staff

        //TODO Add login notification for users for when the ticket has been closed while offline
    }
}
