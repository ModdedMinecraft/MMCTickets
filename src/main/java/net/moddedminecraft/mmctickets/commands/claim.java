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
import org.spongepowered.api.service.permission.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.moddedminecraft.mmctickets.data.ticketStatus.*;

public class claim implements CommandExecutor {

    private final Main plugin;

    public claim(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<Integer> ticketIDParameter = Parameter.integerNumber().key("ticketID").build();

        final Subject subject = context.cause().subject();

        final int ticketID = context.requireOne(ticketIDParameter);

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());

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
                    if (!ticket.getStaffUUID().equals(uuid) && ticket.getStatus() == Claimed && !subject.hasPermission(Permissions.CLAIMED_TICKET_BYPASS)) {
                        throw new CommandException(Messages.getErrorTicketClaim(ticket.getTicketID(), CommonUtil.getPlayerNameFromData(plugin, ticket.getStaffUUID())));
                    }
                    if (ticket.getStaffUUID().equals(uuid) && ticket.getStatus() == Claimed) {
                        throw new CommandException(Messages.getErrorTicketClaim(ticket.getTicketID(), "you"));
                    }
                    if (ticket.getStatus() == Closed || ticket.getStatus() == Held) {
                        throw new CommandException(Messages.getTicketNotOpen(ticketID));
                    }

                    ticket.setStaffUUID(uuid.toString());
                    ticket.setStatus(Claimed);

                    try {
                        plugin.getDataStore().updateTicketData(ticket);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CommandException(Messages.getErrorGen("Unable to claim ticket"));
                    }

                    Optional<ServerPlayer> ticketPlayerOP = Sponge.server().player(ticket.getPlayerUUID());
                    if (ticketPlayerOP.isPresent()) {
                        ServerPlayer ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketClaimUser(player.name(), ticket.getTicketID()));
                    }

                    CommonUtil.notifyOnlineStaff(Messages.getTicketClaim(player.name(), ticket.getTicketID()));

                    return CommandResult.success();
                }
            }
                throw new CommandException(Messages.getTicketNotExist(ticketID));
        }
    }

}
