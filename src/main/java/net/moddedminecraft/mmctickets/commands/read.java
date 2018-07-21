package net.moddedminecraft.mmctickets.commands;

import com.flowpowered.math.vector.Vector3d;
import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Config;
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
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static net.moddedminecraft.mmctickets.data.ticketStatus.*;

public class read implements CommandExecutor {

    private final Main plugin;

    public read(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final Optional<Integer> ticketIDOp = args.<Integer>getOne("ticketID");

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());

        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (src instanceof Player) {
            Player player = (Player) src;
            uuid = player.getUniqueId();
        }

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            if (!ticketIDOp.isPresent()) {
                if (src.hasPermission(Permissions.COMMAND_TICKET_READ_ALL)) {
                    PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
                    List<Text> contents = new ArrayList<>();
                    int totalTickets = 0;
                    for (TicketData ticket : tickets) {
                        if (Config.hideOffline) {
                            if (CommonUtil.checkUserOnline(CommonUtil.getNameFromUUID(ticket.getPlayerUUID()))) {
                                if (ticket.getStatus() == Claimed || ticket.getStatus() == Open) {
                                    String online = CommonUtil.isUserOnline(ticket.getPlayerUUID());
                                    totalTickets++;
                                    Text.Builder send = Text.builder();
                                    String status = "";
                                    if (ticket.getStatus() == Claimed) status = "&eClaimed - ";
                                    send.append(plugin.fromLegacy(status + "&6#" + ticket.getTicketID() + " " + CommonUtil.getTimeAgo(ticket.getTimestamp()) + " by " + online + CommonUtil.getNameFromUUID(ticket.getPlayerUUID()) + " &6- &7" + CommonUtil.shortenMessage(ticket.getMessage())));
                                    send.onClick(TextActions.runCommand("/ticket read " + ticket.getTicketID()));
                                    send.onHover(TextActions.showText(plugin.fromLegacy("Click here to get more details for ticket #" + ticket.getTicketID())));
                                    contents.add(send.build());
                                }
                            }
                        } else {
                            if (ticket.getStatus() == Claimed || ticket.getStatus() == Open) {
                                String online = CommonUtil.isUserOnline(ticket.getPlayerUUID());
                                totalTickets++;
                                Text.Builder send = Text.builder();
                                String status = "";
                                if (ticket.getStatus() == Claimed) status = "&eClaimed - ";
                                send.append(plugin.fromLegacy(status + "&6#" + ticket.getTicketID() + " " + CommonUtil.getTimeAgo(ticket.getTimestamp()) + " by " + online + CommonUtil.getNameFromUUID(ticket.getPlayerUUID()) + " &6- &7" + CommonUtil.shortenMessage(ticket.getMessage())));
                                send.onClick(TextActions.runCommand("/ticket read " + ticket.getTicketID()));
                                send.onHover(TextActions.showText(plugin.fromLegacy("Click here to get more details for ticket #" + ticket.getTicketID())));
                                contents.add(send.build());
                            }
                        }
                    }

                    if (contents.isEmpty()) {
                        contents.add(Messages.getTicketReadNone());
                    }
                    int ticketsPer = 5;
                    if (Config.ticketsPerPage > 0) {
                        ticketsPer = Config.ticketsPerPage;
                    }
                    paginationService.builder()
                            .title(plugin.fromLegacy("&6" + totalTickets + " Open Tickets"))
                            .contents(contents)
                            .padding(Text.of("-"))
                            .linesPerPage(ticketsPer + 2)
                            .sendTo(src);
                    return CommandResult.success();
                } else {
                    if (src.hasPermission(Permissions.COMMAND_TICKET_READ_SELF)) {
                        throw new CommandException(Messages.getErrorIncorrectUsage("/check self or /check #"));
                    } else {
                        throw new CommandException(Messages.getErrorPermission(Permissions.COMMAND_TICKET_READ_ALL));
                    }
                }
            } else {
                if (src.hasPermission(Permissions.COMMAND_TICKET_READ_ALL) || (src.hasPermission(Permissions.COMMAND_TICKET_READ_SELF))) {
                    PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
                    List<Text> contents = new ArrayList<>();
                    int ticketID = ticketIDOp.get();
                    String ticketStatus = "";
                    for (TicketData ticket : tickets) {
                        if (ticket.getTicketID() == ticketID) {
                            if (!ticket.getPlayerUUID().equals(uuid) && !src.hasPermission(Permissions.COMMAND_TICKET_READ_ALL)) {
                                throw new CommandException(Messages.getErrorTicketOwner());
                            }
                            ticketStatus = CommonUtil.getTicketStatusColour(ticket.getStatus());
                            String online = CommonUtil.isUserOnline(ticket.getPlayerUUID());
                            Optional<World> worldOptional = Sponge.getServer().getWorld(ticket.getWorld());

                            Text.Builder action = Text.builder();

                            if (ticket.getStatus() == Open || ticket.getStatus() == Claimed) {
                                if (ticket.getStatus() == Open && src.hasPermission(Permissions.COMMAND_TICKET_CLAIM)) {
                                    action.append(Text.builder()
                                            .append(plugin.fromLegacy(Messages.getClaimButton()))
                                            .onHover(TextActions.showText(plugin.fromLegacy(Messages.getClaimButtonHover())))
                                            .onClick(TextActions.runCommand("/ticket claim " + ticket.getTicketID()))
                                            .build());
                                    action.append(plugin.fromLegacy(" "));
                                }
                                if (ticket.getStatus() == Claimed) {
                                    if (ticket.getStaffUUID().equals(uuid) && src.hasPermission(Permissions.COMMAND_TICKET_UNCLAIM)) {
                                        action.append(Text.builder()
                                                .append(plugin.fromLegacy(Messages.getUnclaimButton()))
                                                .onHover(TextActions.showText(plugin.fromLegacy(Messages.getUnclaimButtonHover())))
                                                .onClick(TextActions.runCommand("/ticket unclaim " + ticket.getTicketID()))
                                                .build());
                                        action.append(plugin.fromLegacy(" "));
                                    }
                                }
                                if ((ticket.getStatus() == Open || ticket.getStatus() == Claimed && ticket.getStaffUUID().equals(uuid)) && src.hasPermission(Permissions.COMMAND_TICKET_HOLD)) {
                                    action.append(Text.builder()
                                            .append(plugin.fromLegacy(Messages.getHoldButton()))
                                            .onHover(TextActions.showText(plugin.fromLegacy(Messages.getHoldButtonHover())))
                                            .onClick(TextActions.runCommand("/ticket hold " + ticket.getTicketID()))
                                            .build());
                                    action.append(plugin.fromLegacy(" "));
                                }
                            }
                            if (ticket.getStatus() == Held || ticket.getStatus() == Closed) {
                                if (src.hasPermission(Permissions.COMMAND_TICKET_REOPEN)) {
                                    action.append(Text.builder()
                                            .append(plugin.fromLegacy(Messages.getReopenButton()))
                                            .onHover(TextActions.showText(plugin.fromLegacy(Messages.getReopenButtonHover())))
                                            .onClick(TextActions.runCommand("/ticket reopen " + ticket.getTicketID()))
                                            .build());
                                    action.append(plugin.fromLegacy(" "));
                                }
                            }
                            if (ticket.getStatus() == Held || ticket.getStatus() == Claimed || ticket.getStatus() == Open) {
                                if ((ticket.getStatus() == Claimed && ticket.getStaffUUID().equals(uuid)) || ticket.getStatus() == Open || ticket.getStatus() == Held) {
                                    if (src.hasPermission(Permissions.COMMAND_TICKET_CLOSE_ALL) || src.hasPermission(Permissions.COMMAND_TICKET_CLOSE_SELF)) {
                                        action.append(Text.builder()
                                                .append(plugin.fromLegacy(Messages.getCloseButton()))
                                                .onHover(TextActions.showText(plugin.fromLegacy(Messages.getCloseButtonHover())))
                                                .onClick(TextActions.runCommand("/ticket complete " + ticket.getTicketID()))
                                                .build());
                                        action.append(plugin.fromLegacy(" "));
                                    }
                                }
                            }
                            if (ticket.getComment().isEmpty() && src.hasPermission(Permissions.COMMAND_TICKET_COMMENT)) {
                                if (ticket.getStatus() != Claimed || ticket.getStatus() == Claimed && ticket.getStaffUUID().equals(uuid)) {
                                    action.append(Text.builder()
                                            .append(plugin.fromLegacy(Messages.getCommentButton()))
                                            .onHover(TextActions.showText(plugin.fromLegacy(Messages.getCommentButtonHover())))
                                            .onClick(TextActions.suggestCommand("/ticket comment " + ticket.getTicketID() + " "))
                                            .build());
                                }
                            }



                            Text.Builder send = Text.builder();
                            send.append(plugin.fromLegacy("&a" + ticket.getWorld() + "&e x:&a" + ticket.getX() + "&e y:&a" + ticket.getY() + "&e z:&a" + ticket.getZ()));
                            if (src.hasPermission(Permissions.COMMAND_TICKET_TELEPORT)) {
                                send.onHover(TextActions.showText(Messages.getTicketOnHoverTeleportTo()));
                                worldOptional.ifPresent(world -> send.onClick(TextActions.executeCallback(teleportTo(world, ticket.getX(), ticket.getY(), ticket.getZ(), ticket.getPitch(), ticket.getYaw(), ticketID))));
                            }

                            if (!action.build().isEmpty()) {
                                contents.add(action.build());
                            }
                            if (!ticket.getStaffUUID().toString().equals("00000000-0000-0000-0000-000000000000")) {
                                if (ticket.getStatus() == Claimed)
                                    contents.add(plugin.fromLegacy("&eClaimed by: &7" + CommonUtil.getNameFromUUID(ticket.getStaffUUID())));
                                else if (ticket.getStatus() == Closed)
                                    contents.add(plugin.fromLegacy("&eHandled by: &7" + CommonUtil.getNameFromUUID(ticket.getStaffUUID())));
                            }
                            if (!ticket.getComment().isEmpty()) {
                                    contents.add(plugin.fromLegacy("&eComment: &7" + ticket.getComment()));
                            }
                            contents.add(plugin.fromLegacy("&eOpened by: " + online + CommonUtil.getNameFromUUID(ticket.getPlayerUUID())));
                            contents.add(plugin.fromLegacy("&eWhen: " + CommonUtil.getTimeAgo(ticket.getTimestamp())));
                            contents.add(send.build());
                            contents.add(plugin.fromLegacy("&7" + ticket.getMessage()));
                        }
                    }

                    if (contents.isEmpty()) {
                        throw new CommandException(Messages.getTicketNotExist(ticketID));
                    }

                    paginationService.builder()
                            .title(plugin.fromLegacy("&6Request #" + ticketID + " &b- &6" + ticketStatus))
                            .contents(contents)
                            .padding(plugin.fromLegacy("&b-"))
                            .sendTo(src);
                    return CommandResult.success();
                } else {
                    throw new CommandException(Messages.getErrorPermission(Permissions.COMMAND_TICKET_READ_SELF));
                }
            }
        }
    }
    private Consumer<CommandSource> teleportTo(World world, int x, int y, int z, double pitch, double yaw, int ticketID) {
        return consumer -> {
            Player player = (Player) consumer;
            Location loc = new Location(world, x, y, z);
            Vector3d vect = new Vector3d(pitch, yaw, 0);
            player.setLocationAndRotation(loc, vect);
            player.sendMessage(Messages.getTeleportToTicket(ticketID));
        };
    }
}
