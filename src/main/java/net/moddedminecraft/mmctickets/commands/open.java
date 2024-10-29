package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Config;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.DefaultWorldKeys;

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
    public CommandResult execute(CommandContext context) throws CommandException {

        Parameter.Value<String> messageParameter = Parameter.remainingJoinedStrings().key("message").build();

        final String message = context.requireOne(messageParameter);

        if (context.cause().root() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) context.cause().root();
            UUID uuid = player.uniqueId();

            if (Config.server.isEmpty()) {
                throw new CommandException(Messages.getErrorGen("Server name inside config is not set"));
            }
            if (plugin.getWaitTimer().contains(player.name())) {
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
                if (pData.getPlayerName().equals(player.name()) && pData.getBannedStatus() == 1) {
                    throw new CommandException(Messages.getErrorBanned());
                }
            }

            try {
                plugin.getDataStore().addTicketData(new TicketData(ticketID,
                        String.valueOf(uuid),
                        "00000000-0000-0000-0000-000000000000",
                        "",
                        System.currentTimeMillis() / 1000,
                        player.world().key().namespace(),
                        player.world().key().value(),
                        player.serverLocation().blockX(),
                        player.serverLocation().blockY(),
                        player.serverLocation().blockZ(),
                        player.headDirection().x(),
                        player.headDirection().y(),
                        message,
                        Open,
                        0,
                        Config.server));

                player.sendMessage(Messages.getTicketOpenUser(ticketID));
                if (Config.staffNotification) {
                    CommonUtil.notifyOnlineStaffOpen(Messages.getTicketOpen(player.name(), ticketID), ticketID);
                }
                if (Config.titleNotification) {
                    CommonUtil.notifyOnlineStaffTitle(Messages.getTicketTitleNotification(player.name(), ticketID));
                }
                if (Config.soundNotification) {
                    CommonUtil.notifyOnlineStaffSound();
                }
            } catch (Exception e) {
                player.sendMessage(Messages.getErrorGen("Data was not saved correctly."));
                e.printStackTrace();
            }
            plugin.getWaitTimer().add(player.name());

            Sponge.asyncScheduler().executor(plugin.container).schedule(new Runnable() {
                @Override
                public void run() {
                    plugin.getWaitTimer().removeAll(Collections.singleton(player.name()));
                    plugin.getLogger().info("Removed " + player.name() + " from open wait list");
                }
            }, Config.delayTimer, TimeUnit.SECONDS);
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
                        Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get().key().namespace(),
                        Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get().key().value(),
                        0,
                        0,
                        0,
                        0.0,
                        0.0,
                        message,
                        Open,
                        0,
                        Config.server));

                context.cause().audience().sendMessage(Messages.getTicketOpenUser(ticketID));
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
                context.cause().audience().sendMessage(Messages.getErrorGen("Data was not saved correctly."));
                e.printStackTrace();
            }
            return CommandResult.success();
        }
    }
}
