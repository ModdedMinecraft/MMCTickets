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

public class close implements CommandExecutor {

    private final Main plugin;

    public close(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        final int ticketID = args.<Integer>getOne("ticketID").get();
        final Optional<String> commentOP = args.<String>getOne("comment");

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    if (ticket.getName().equals(src.getName()) && !src.hasPermission(Permissions.COMMAND_TICKET_CLOSE_SELF)) {
                        throw new CommandException(Messages.getErrorPermission(Permissions.COMMAND_TICKET_CLOSE_SELF));
                    }
                    if (!ticket.getName().equals(src.getName()) && !src.hasPermission(Permissions.COMMAND_TICKET_CLOSE_ALL)) {
                        throw new CommandException(Messages.getErrorTicketOwner());
                    }
                    if (ticket.getStatus() == 3) {
                        throw new CommandException(Messages.getErrorTicketAlreadyClosed());
                    }
                    if (ticket.getStatus() == 1 && !ticket.getStaffName().equals(src.getName()) && !src.hasPermission(Permissions.CLAIMED_TICKET_BYPASS)) {
                        throw new CommandException(Messages.getErrorTicketClaim(ticket.getTicketID(), ticket.getStaffName()));
                    }
                    if (commentOP.isPresent()) {
                        String comment = commentOP.get();
                        plugin.getTicket(ticketID).setComment(comment);
                    }
                    plugin.getTicket(ticketID).setStatus(3);
                    ticket.setStaffName(src.getName());

                    CommonUtil.notifyOnlineStaff(Messages.getTicketClose(ticketID, src.getName()));
                    Optional<Player> ticketPlayerOP = Sponge.getServer().getPlayer(ticket.getName());
                    if (ticketPlayerOP.isPresent()) {
                        Player ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketCloseUser(ticket.getTicketID(), src.getName()));
                        ticket.setNotified(1);
                    } else {
                        plugin.getNotifications().add(ticket.getName());
                    }

                    try {
                        plugin.saveData();
                    } catch (Exception e) {
                        src.sendMessage(Messages.getErrorGen("Unable to close ticket"));
                        e.printStackTrace();
                    }
                    return CommandResult.success();

                }
            }
            throw new CommandException(Messages.getTicketNotExist(ticketID));
        }
    }
}
