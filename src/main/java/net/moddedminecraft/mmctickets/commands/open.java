package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Config;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.moddedminecraft.mmctickets.data.ticketStatus.Closed;
import static net.moddedminecraft.mmctickets.data.ticketStatus.Open;

public class open implements CommandExecutor {

    private final Main plugin;

    public open(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final String message = args.<String>getOne("message").get();

        /*if (!(src instanceof Player)) {
            throw new CommandException(Messages.getErrorGen("Only players can run this command"));
        }*/

        if (src instanceof Player) {
            Player player = (Player) src;
            UUID uuid = player.getUniqueId();

            if (Config.server.isEmpty()) {
                throw new CommandException(Messages.getErrorGen("Server name inside config is not set"));
            }
            if (plugin.getWaitTimer().contains(src.getName())) {
                throw new CommandException(Messages.getTicketTooFast(Config.delayTimer));
            }
            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());
            int totalTickets = 0;
            boolean duplicate = false;
            int ticketID = tickets.size() + 1;


            if (!tickets.isEmpty()) {
                for (TicketData ticket : tickets) {
                    if (ticket.getTicketID() == ticketID) {
                        ticketID++;
                    }
                    if (ticket.getPlayerUUID().equals(uuid) && ticket.getStatus() != Closed) {
                        totalTickets++;
                    }
                    if (Config.preventDuplicates) {
                        if (ticket.getMessage().equals(message) && ticket.getStatus() != Closed && ticket.getPlayerUUID().equals(uuid)) {
                            duplicate = true;
                        }
                    }
                }
            }

            if (duplicate) {
                throw new CommandException(Messages.getTicketDuplicate());
            }
            if (totalTickets >= Config.maxTickets) {
                throw new CommandException(Messages.getTicketTooMany());
            }
            if (message.split("\\s+").length < Config.minWords) {
                throw new CommandException(Messages.getTicketTooShort(Config.minWords));
            }

            final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getDataStore().getPlayerData());
            for (PlayerData pData : playerData) {
                if (pData.getPlayerName().equals(src.getName()) && pData.getBannedStatus() == 1) {
                    throw new CommandException(Messages.getErrorBanned());
                }
            }

            try {
                plugin.getDataStore().addTicketData(new TicketData(ticketID,
                        String.valueOf(uuid),
                        UUID.fromString("00000000-0000-0000-0000-000000000000").toString(),
                        "",
                        System.currentTimeMillis() / 1000,
                        player.getWorld().getName(),
                        player.getLocation().getBlockX(),
                        player.getLocation().getBlockY(),
                        player.getLocation().getBlockZ(),
                        player.getHeadRotation().getX(),
                        player.getHeadRotation().getY(),
                        message,
                        Open,
                        0,
                        Config.server));

                player.sendMessage(Messages.getTicketOpenUser(ticketID));
                if (Config.staffNotification) {
                    CommonUtil.notifyOnlineStaffOpen(Messages.getTicketOpen(player.getName(), ticketID), ticketID);
                }
                if (Config.titleNotification) {
                    CommonUtil.notifyOnlineStaffTitle(Messages.getTicketTitleNotification(player.getName(), ticketID));
                }
                if (Config.soundNotification) {
                    CommonUtil.notifyOnlineStaffSound();
                }
            } catch (Exception e) {
                player.sendMessage(Messages.getErrorGen("Data was not saved correctly."));
                e.printStackTrace();
            }
            plugin.getWaitTimer().add(src.getName());

            Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
                @Override
                public void run() {
                    plugin.getWaitTimer().removeAll(Collections.singleton(src.getName()));
                }
            }).delay(Config.delayTimer, TimeUnit.SECONDS).name("mmctickets-s-openTicketWaitTimer").submit(this.plugin);

            return CommandResult.success();
        } else {
            if (Config.server.isEmpty()) {
                throw new CommandException(Messages.getErrorGen("Server name inside config is not set"));
            }

            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());
            int ticketID = tickets.size() + 1;

            try {
                plugin.getDataStore().addTicketData(new TicketData(ticketID,
                        UUID.fromString("00000000-0000-0000-0000-000000000000").toString(),
                        UUID.fromString("00000000-0000-0000-0000-000000000000").toString(),
                        "",
                        System.currentTimeMillis() / 1000,
                        Sponge.getServer().getDefaultWorldName(),
                        0,
                        0,
                        0,
                        0.0,
                        0.0,
                        message,
                        Open,
                        0,
                        Config.server));

                src.sendMessage(Messages.getTicketOpenUser(ticketID));
                if (Config.staffNotification) {
                    CommonUtil.notifyOnlineStaffOpen(Messages.getTicketOpen("Console", ticketID), ticketID);
                }
                if (Config.titleNotification) {
                    CommonUtil.notifyOnlineStaffTitle(Messages.getTicketTitleNotification("Console", ticketID));
                }
                if (Config.soundNotification) {
                    CommonUtil.notifyOnlineStaffSound();
                }
            } catch (Exception e) {
                src.sendMessage(Messages.getErrorGen("Data was not saved correctly."));
                e.printStackTrace();
            }

            return CommandResult.success();
        }
    }
}
