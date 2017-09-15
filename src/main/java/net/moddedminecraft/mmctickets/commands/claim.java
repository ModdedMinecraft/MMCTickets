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
import java.util.UUID;

import static net.moddedminecraft.mmctickets.data.ticketStatus.*;

public class claim implements CommandExecutor {

    private final Main plugin;

    public claim(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final int ticketID = args.<Integer>getOne("ticketID").get();
        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());

        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (src instanceof Player) {
            Player player = (Player) src;
            uuid = player.getUniqueId();
        }

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    if (!ticket.getStaffUUID().equals(uuid) && ticket.getStatus() == Claimed && !src.hasPermission(Permissions.CLAIMED_TICKET_BYPASS)) {
                        throw new CommandException(Messages.getErrorTicketClaim(ticket.getTicketID(), CommonUtil.getNameFromUUID(ticket.getStaffUUID())));
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
                        plugin.saveData();
                    } catch (Exception e) {
                        src.sendMessage(Messages.getErrorGen("Unable to claim ticket"));
                        e.printStackTrace();
                    }

                    Optional<Player> ticketPlayerOP = Sponge.getServer().getPlayer(ticket.getPlayerUUID());
                    if (ticketPlayerOP.isPresent()) {
                        Player ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketClaimUser(src.getName(), ticket.getTicketID()));
                    }

                    CommonUtil.notifyOnlineStaff(Messages.getTicketClaim(src.getName(), ticket.getTicketID()));

                    return CommandResult.success();
                }
            }
                throw new CommandException(Messages.getTicketNotExist(ticketID));
        }
    }

}
