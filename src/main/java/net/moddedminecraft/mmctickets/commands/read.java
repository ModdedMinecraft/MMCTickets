package net.moddedminecraft.mmctickets.commands;

import com.flowpowered.math.vector.Vector3d;
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
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class read implements CommandExecutor {

    private final Main plugin;

    public read(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final Optional<Integer> ticketIDOp = args.<Integer>getOne("ticketID");

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getTickets());

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.parse(Messages.errorGeneral, "Tickets list is empty."));
        } else {
            if (!ticketIDOp.isPresent()) {
                PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
                List<Text> contents = new ArrayList<>();
                int totalTickets = 0;
                for (TicketData ticket : tickets) {
                    if (ticket.getStatus() == 0) {
                        String online = CommonUtil.isUserOnline(ticket.getName());
                        totalTickets++;
                        Text.Builder send = Text.builder();
                        send.append(plugin.fromLegacy("&6#" + ticket.getTicketID() + " " + CommonUtil.getTimeAgo(ticket.getTimestamp()) + " by " + online + ticket.getName() + " &6- &7" + CommonUtil.shortenMessage(ticket.getMessage())));
                        send.onClick(TextActions.runCommand("/ticket read " + ticket.getTicketID()));
                        send.onHover(TextActions.showText(plugin.fromLegacy("Click here to get more details for ticket #" + ticket.getTicketID())));
                        contents.add(send.build());
                    }
                }

                if (contents.isEmpty()) {
                    contents.add(Messages.parse(Messages.ticketReadNone));
                }
                paginationService.builder()
                        .title(plugin.fromLegacy("&6" + totalTickets + " Open Tickets"))
                        .contents(contents)
                        .padding(Text.of("-"))
                        .sendTo(src);
                return CommandResult.success();
            } else {
                PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
                List<Text> contents = new ArrayList<>();
                int ticketID = ticketIDOp.get();
                String ticketStatus = "";
                for (TicketData ticket : tickets) {
                    if (ticket.getTicketID() == ticketID) {
                        ticketStatus = CommonUtil.getTicketStatusColour(ticket.getStatus());
                        String online = CommonUtil.isUserOnline(ticket.getName());
                        World world = Sponge.getServer().getWorld(ticket.getWorld()).get();
                        Text.Builder send = Text.builder();
                        send.append(plugin.fromLegacy("&a" + ticket.getWorld() + "&e x:&a" + ticket.getX() + "&e y:&a" + ticket.getY() + "&e z:&a" + ticket.getZ()));
                        send.onHover(TextActions.showText(plugin.fromLegacy("Click here to teleport to this ticket's location.")));
                        send.onClick(TextActions.executeCallback(teleportTo(world, ticket.getX(), ticket.getY(), ticket.getZ(), ticket.getPitch(), ticket.getYaw())));
                        contents.add(plugin.fromLegacy("&eOpened by: " + online +ticket.getName()));
                        contents.add(plugin.fromLegacy("&eTime: " + CommonUtil.getTimeAgo(ticket.getTimestamp())));
                        contents.add(send.build());
                        contents.add(plugin.fromLegacy("&7" + ticket.getMessage()));
                    }
                }

                if (contents.isEmpty()) {
                    throw new CommandException(Messages.parse(Messages.ticketNotExist, ticketID));
                }

                paginationService.builder()
                        .title(plugin.fromLegacy("&6Request #" + ticketID + " &b- &6" + ticketStatus))
                        .contents(contents)
                        .padding(plugin.fromLegacy("&b-"))
                        .sendTo(src);
                return CommandResult.success();
            }
        }
    }
    private Consumer<CommandSource> teleportTo(World world, int x, int y, int z, double pitch, double yaw) {
        return consumer -> {
            if (consumer.hasPermission("mmctickets.commands.teleport")) {
                Player player = (Player) consumer;
                Location loc = new Location(world, x, y, z);
                Vector3d vect = new Vector3d(pitch, yaw, 0);
                player.setLocationAndRotation(loc, vect);
            } else {

            }
        };
    }
}
