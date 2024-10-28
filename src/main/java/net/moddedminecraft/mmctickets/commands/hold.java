package net.moddedminecraft.mmctickets.commands;

import net.kyori.adventure.audience.Audience;
import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.moddedminecraft.mmctickets.data.ticketStatus.*;

public class hold implements CommandExecutor {
    private final Main plugin;

    public hold(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<Integer> ticketIDParameter = Parameter.integerNumber().key("ticketID").build();

        final int ticketID = context.requireOne(ticketIDParameter);

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());

        Audience audience = context.cause().audience();

        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        ServerPlayer player = null;
        if (context.cause().root() instanceof ServerPlayer) {
            player = (ServerPlayer) context.cause().root();
            uuid = player.uniqueId();
        }
        String staffName = "Console";
        if (player != null) {
            staffName = player.name();
        }

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    if (ticket.getStatus() == Closed) {
                        audience.sendMessage(Messages.getErrorTicketAlreadyClosed());
                    }
                    if (ticket.getStatus() == Held) {
                        audience.sendMessage(Messages.getErrorTicketlreadyHold());
                    }
                    if (ticket.getStatus() == Claimed && !ticket.getStaffUUID().equals(uuid)) {
                        audience.sendMessage(Messages.getErrorTicketClaim(ticket.getTicketID(), CommonUtil.getPlayerNameFromData(plugin, ticket.getStaffUUID())));
                    }
                    ticket.setStatus(Held);
                    ticket.setStaffUUID(UUID.fromString("00000000-0000-0000-0000-000000000000").toString());

                    try {
                        plugin.getDataStore().updateTicketData(ticket);
                    } catch (Exception e) {
                        audience.sendMessage(Messages.getErrorGen("Unable to put ticket on hold"));
                        e.printStackTrace();
                    }

                    CommonUtil.notifyOnlineStaff(Messages.getTicketHold(ticket.getTicketID(), staffName));

                    Optional<ServerPlayer> ticketPlayerOP = Sponge.server().player(ticket.getPlayerUUID());
                    if (ticketPlayerOP.isPresent()) {
                        ServerPlayer ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketHoldUser(ticket.getTicketID(), staffName));
                    }
                    return CommandResult.success();
                }
            }
            throw new CommandException(Messages.getTicketNotExist(ticketID));
        }
    }
}
