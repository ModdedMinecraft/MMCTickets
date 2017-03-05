package net.moddedminecraft.mmctickets.commands;


import com.flowpowered.math.vector.Vector3d;
import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.TicketData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

public class teleport implements CommandExecutor {

    private final Main plugin;

    public teleport(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final int ticketID = args.<Integer>getOne("ticketID").get();
        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());

        if (!(src instanceof Player)) {
            throw new CommandException(Messages.getErrorGen("Only players can use this command"));
        }
        Player player = (Player) src;

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            boolean ticketExist = false;
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    ticketExist = true;
                    World world = Sponge.getServer().getWorld(ticket.getWorld()).get();
                    Location loc = new Location(world, ticket.getX(), ticket.getY(), ticket.getZ());
                    Vector3d vect = new Vector3d(ticket.getPitch(), ticket.getYaw(), 0);
                    player.setLocationAndRotation(loc, vect);
                    player.sendMessage(Messages.getTeleportToTicket(ticketID));
                }
            }
            if (!ticketExist) {
                throw new CommandException(Messages.getTicketNotExist(ticketID));
            }
            return CommandResult.success();
        }
    }
}
