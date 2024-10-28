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
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.moddedminecraft.mmctickets.data.ticketStatus.Claimed;
import static net.moddedminecraft.mmctickets.data.ticketStatus.Closed;


public class close implements CommandExecutor {

    private final Main plugin;

    public close(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {

        Parameter.Value<Integer> ticketIDParameter = Parameter.integerNumber().key("ticketID").build();
        Parameter.Value<String> commentParameter = Parameter.remainingJoinedStrings().key("comment").build();


        final int ticketID = context.requireOne(ticketIDParameter);
        final Optional<String> commentOP = context.one(commentParameter);

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());

        Subject subject = context.cause().subject();

        ServerPlayer player = null;
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        if (context.cause().root() instanceof ServerPlayer) {
            player = (ServerPlayer) context.cause().root();
            uuid = player.uniqueId();
        }

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    if (ticket.getPlayerUUID().equals(uuid) && !subject.hasPermission(Permissions.COMMAND_TICKET_CLOSE_SELF)) {
                        throw new CommandException(Messages.getErrorPermission(Permissions.COMMAND_TICKET_CLOSE_SELF));
                    }
                    if (!ticket.getPlayerUUID().equals(uuid) && !subject.hasPermission(Permissions.COMMAND_TICKET_CLOSE_ALL)) {
                        throw new CommandException(Messages.getErrorTicketOwner());
                    }
                    if (ticket.getStatus() == Closed) {
                        throw new CommandException(Messages.getErrorTicketAlreadyClosed());
                    }
                    if (ticket.getStatus() == Claimed && !ticket.getStaffUUID().equals(uuid) && !subject.hasPermission(Permissions.CLAIMED_TICKET_BYPASS)) {
                        throw new CommandException(Messages.getErrorTicketClaim(ticket.getTicketID(), CommonUtil.getPlayerNameFromData(plugin, ticket.getStaffUUID())));
                    }
                    if (commentOP.isPresent()) {
                        String comment = commentOP.get();
                        ticket.setComment(comment);
                    }
                    ticket.setStatus(Closed);
                    ticket.setStaffUUID(uuid.toString());

                    CommonUtil.notifyOnlineStaff(Messages.getTicketClose(ticketID, player.name()));
                    Optional<ServerPlayer> ticketPlayerOP = Sponge.server().player(ticket.getPlayerUUID());
                    if (ticketPlayerOP.isPresent()) {
                        ServerPlayer ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketCloseUser(ticket.getTicketID(), player.name()));
                        ticket.setNotified(1);
                    } else {
                        plugin.getDataStore().getNotifications().add(ticket.getPlayerUUID());
                    }

                    try {
                        plugin.getDataStore().updateTicketData(ticket);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CommandException(Messages.getErrorGen("Unable to close ticket"));
                    }
                    return CommandResult.success();

                }
            }
            throw new CommandException(Messages.getTicketNotExist(ticketID));
        }
    }
}
