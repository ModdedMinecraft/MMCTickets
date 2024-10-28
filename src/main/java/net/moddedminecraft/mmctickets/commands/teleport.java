package net.moddedminecraft.mmctickets.commands;


import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Config;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.TicketData;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class teleport implements CommandExecutor {

    private final Main plugin;

    public teleport(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<Integer> ticketIDParameter = Parameter.integerNumber().key("ticketID").build();

        final int ticketID = context.requireOne(ticketIDParameter);

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());

        if (!(context.cause().root() instanceof ServerPlayer)) {
            throw new CommandException(Messages.getErrorGen("Only players can use this command"));
        }
        ServerPlayer player = (ServerPlayer) context.cause().root();

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            boolean ticketExist = false;
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    if (ticket.getServer().equalsIgnoreCase(Config.server)) {
                        ticketExist = true;
                        ServerWorld world = Sponge.server().worldManager().world(ResourceKey.builder().namespace(ticket.getWorld()).build()).get();
                        ServerLocation loc = world.location(ticket.getX(), ticket.getY(), ticket.getZ());
                        Vector3d vector = new Vector3d(ticket.getPitch(), ticket.getYaw(), 0);
                        player.setLocationAndRotation(loc, vector);
                        player.sendMessage(Messages.getTeleportToTicket(ticketID));
                    } else {
                        throw new CommandException(Messages.getErrorTicketServer(ticketID));
                    }
                }
            }
            if (!ticketExist) {
                throw new CommandException(Messages.getTicketNotExist(ticketID));
            }
            return CommandResult.success();
        }
    }
}
