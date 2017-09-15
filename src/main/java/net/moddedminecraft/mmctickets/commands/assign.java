package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
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
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final int ticketID = args.<Integer>getOne("ticketID").get();
        final Player player = args.<Player>getOne("player").get();

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    if (ticket.getStatus() == Closed) {
                        src.sendMessage(Messages.getErrorTicketAlreadyClosed());
                    }
                    if (ticket.getStatus() == Claimed && !src.hasPermission(Permissions.CLAIMED_TICKET_BYPASS)) {
                        throw new CommandException(Messages.getErrorTicketClaim(ticket.getTicketID(), CommonUtil.getNameFromUUID(ticket.getStaffUUID())));
                    }
                    ticket.setStatus(Claimed);
                    ticket.setStaffUUID(player.getUniqueId().toString());

                    try {
                        plugin.saveData();
                    } catch (Exception e) {
                        src.sendMessage(Messages.getErrorGen("Unable to assign " + player.getName() + " to ticket"));
                        e.printStackTrace();
                    }

                    CommonUtil.notifyOnlineStaff(Messages.getTicketAssign(CommonUtil.getNameFromUUID(ticket.getStaffUUID()), ticket.getTicketID()));

                    Optional<Player> ticketPlayerOP = Sponge.getServer().getPlayer(ticket.getPlayerUUID());
                    if (ticketPlayerOP.isPresent()) {
                        Player ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketAssignUser(ticket.getTicketID(), CommonUtil.getNameFromUUID(ticket.getStaffUUID())));
                    }
                    return CommandResult.success();
                }
            }
            throw new CommandException(Messages.getTicketNotExist(ticketID));
        }
    }
}
