package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
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
import static net.moddedminecraft.mmctickets.data.ticketStatus.Open;


public class reopen implements CommandExecutor {
    private final Main plugin;

    public reopen(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final int ticketID = args.<Integer>getOne("ticketID").get();

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    if (ticket.getStatus() == Claimed || ticket.getStatus() == Open) {
                        throw new CommandException(Messages.getErrorTicketNotClosed(ticketID));
                    }
                    if (ticket.getStatus() == Claimed) {
                        throw new CommandException(Messages.getErrorTicketClaim(ticket.getTicketID(), ticket.getStaffName()));
                    }
                    ticket.setStatus(Open);
                    ticket.setStaffName("");
                    ticket.setComment("");
                    ticket.setNotified(0);

                    try {
                        plugin.saveData();
                    } catch (Exception e) {
                        src.sendMessage(Messages.getErrorGen("Unable to reopen ticket"));
                        e.printStackTrace();
                    }

                    CommonUtil.notifyOnlineStaff(Messages.getTicketReopen(src.getName(), ticket.getTicketID()));

                    Optional<Player> ticketPlayerOP = Sponge.getServer().getPlayer(ticket.getName());
                    if (ticketPlayerOP.isPresent()) {
                        Player ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketReopenUser(src.getName(), ticket.getTicketID()));
                    }
                    return CommandResult.success();
                }
            }
            throw new CommandException(Messages.getTicketNotExist(ticketID));
        }
    }
}
