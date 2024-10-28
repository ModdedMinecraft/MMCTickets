package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.service.permission.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.moddedminecraft.mmctickets.data.ticketStatus.Claimed;
import static net.moddedminecraft.mmctickets.data.ticketStatus.Closed;

public class assign implements CommandExecutor {
    private final Main plugin;

    public assign(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<Integer> ticketIDParameter = Parameter.integerNumber().key("ticketID").build();
        Parameter.Value<ServerPlayer> playerParameter = Parameter.player().key("player").build();

        final int ticketID = context.requireOne(ticketIDParameter);
        final ServerPlayer user = context.requireOne(playerParameter);
        final Subject subject = context.cause().subject();

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    if (ticket.getStatus() == Closed) {
                        throw new CommandException(Messages.getErrorTicketAlreadyClosed());
                    }
                    if (ticket.getStatus() == Claimed && !subject.hasPermission(Permissions.CLAIMED_TICKET_BYPASS)) {
                        throw new CommandException(Messages.getErrorTicketClaim(ticket.getTicketID(), CommonUtil.getPlayerNameFromData(plugin, ticket.getStaffUUID())));
                    }
                    ticket.setStatus(Claimed);
                    ticket.setStaffUUID(user.uniqueId().toString());

                    try {
                        plugin.getDataStore().updateTicketData(ticket);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CommandException(Messages.getErrorGen("Unable to assign " + user.name() + " to ticket"));
                    }

                    CommonUtil.notifyOnlineStaff(Messages.getTicketAssign(CommonUtil.getPlayerNameFromData(plugin, ticket.getStaffUUID()), ticket.getTicketID()));

                    Optional<ServerPlayer> ticketPlayerOP = Sponge.server().player(ticket.getPlayerUUID());
                    if (ticketPlayerOP.isPresent()) {
                        Player ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketAssignUser(ticket.getTicketID(), CommonUtil.getPlayerNameFromData(plugin, ticket.getStaffUUID())));
                    }
                    return CommandResult.success();
                }
            }
            throw new CommandException(Messages.getTicketNotExist(ticketID));
        }
    }
}
