package net.moddedminecraft.mmctickets.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Config;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

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
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<Integer> ticketIDParameter = Parameter.integerNumber().key("ticketID").build();

        final Optional<Integer> ticketIDOP = context.one(ticketIDParameter);
        
        final List<TicketData> tickets = new ArrayList<>(plugin.getDataStore().getTicketData());

        Subject subject = context.cause().subject();
        Audience audience = context.cause().audience();

        ServerPlayer player;
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        
        if (context.cause().root() instanceof ServerPlayer) {
            player = (ServerPlayer) context.cause().root();
            uuid = player.uniqueId();
        } else {
            player = null;
        }

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            if (!ticketIDOP.isPresent()) {
                if (subject.hasPermission(Permissions.COMMAND_TICKET_READ_ALL)) {
                    PaginationService paginationService = Sponge.serviceProvider().provide(PaginationService.class).get();
                    List<Component> contents = new ArrayList<>();
                    int totalTickets = 0;
                    for (TicketData ticket : tickets) {
                        if (Config.hideOffline) {
                            if (CommonUtil.checkUserOnline(CommonUtil.getPlayerNameFromData(plugin, ticket.getPlayerUUID()))) {
                                if (ticket.getStatus() == Claimed || ticket.getStatus() == Open) {
                                    String online = CommonUtil.isUserOnline(ticket.getPlayerUUID());
                                    totalTickets++;
                                    TextComponent.@NotNull Builder send = Component.text();
                                    String status = "";
                                    if (ticket.getStatus() == Claimed) status = "&eClaimed - ";
                                    send.append(plugin.fromLegacy(status + "&6#" + ticket.getTicketID()
                                            + " " + CommonUtil.getTimeAgo(ticket.getTimestamp())
                                            + " by " + online + CommonUtil.getPlayerNameFromData(plugin, ticket.getPlayerUUID())
                                            + " &6on " + CommonUtil.checkTicketServer(ticket.getServer())
                                            + " &6- &7" + CommonUtil.shortenMessage(ticket.getMessage())));
                                    send.clickEvent(ClickEvent.runCommand("/ticket read " + ticket.getTicketID()));
                                    send.hoverEvent(HoverEvent.showText(Component.text("Click here to get more details for ticket #" + ticket.getTicketID())));
                                    contents.add(send.build());
                                }
                            }
                        } else {
                            if (ticket.getStatus() == Claimed || ticket.getStatus() == Open) {
                                String online = CommonUtil.isUserOnline(ticket.getPlayerUUID());
                                totalTickets++;
                                TextComponent.@NotNull Builder send = Component.text();
                                String status = "";
                                if (ticket.getStatus() == Claimed) status = "&eClaimed - ";
                                send.append(plugin.fromLegacy(status + "&6#" + ticket.getTicketID()
                                        + " " + CommonUtil.getTimeAgo(ticket.getTimestamp())
                                        + " by " + online + CommonUtil.getPlayerNameFromData(plugin, ticket.getPlayerUUID())
                                        + " &6on " + CommonUtil.checkTicketServer(ticket.getServer())
                                        + " &6- &7" + CommonUtil.shortenMessage(ticket.getMessage())));
                                send.clickEvent(ClickEvent.runCommand("/ticket read " + ticket.getTicketID()));
                                send.hoverEvent(HoverEvent.showText(Component.text("Click here to get more details for ticket #" + ticket.getTicketID())));
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
                            .padding(Component.text("-"))
                            .linesPerPage(ticketsPer + 2)
                            .sendTo(audience);
                    return CommandResult.success();
                } else {
                    if (subject.hasPermission(Permissions.COMMAND_TICKET_READ_SELF)) {
                        throw new CommandException(Messages.getErrorIncorrectUsage("/check self or /check #"));
                    } else {
                        throw new CommandException(Messages.getErrorPermission(Permissions.COMMAND_TICKET_READ_ALL));
                    }
                }
            } else {
                if (subject.hasPermission(Permissions.COMMAND_TICKET_READ_ALL) || (subject.hasPermission(Permissions.COMMAND_TICKET_READ_SELF))) {
                    PaginationService paginationService = Sponge.serviceProvider().provide(PaginationService.class).get();
                    List<Component> contents = new ArrayList<>();
                    int ticketID = ticketIDOP.get();
                    String ticketStatus = "";
                    for (TicketData ticket : tickets) {
                        if (ticket.getTicketID() == ticketID) {
                            if (!ticket.getPlayerUUID().equals(uuid) && !subject.hasPermission(Permissions.COMMAND_TICKET_READ_ALL)) {
                                throw new CommandException(Messages.getErrorTicketOwner());
                            }
                            ticketStatus = CommonUtil.getTicketStatusColour(ticket.getStatus());
                            String online = CommonUtil.isUserOnline(ticket.getPlayerUUID());
                            Optional<ServerWorld> worldOptional = Sponge.server().worldManager().world(ResourceKey.minecraft(ticket.getWorld()));

                            TextComponent.@NotNull Builder action = Component.text();

                            if (ticket.getStatus() == Open || ticket.getStatus() == Claimed) {
                                if (ticket.getStatus() == Open && subject.hasPermission(Permissions.COMMAND_TICKET_CLAIM)) {
                                    action.append(
                                            Component.text().append(plugin.fromLegacy(Messages.getClaimButton()))
                                            .hoverEvent(HoverEvent.showText(plugin.fromLegacy(Messages.getClaimButtonHover())))
                                            .clickEvent(ClickEvent.runCommand("/ticket claim " + ticket.getTicketID()))
                                            .build());
                                    action.append(plugin.fromLegacy(" "));
                                }
                                if (ticket.getStatus() == Claimed) {
                                    if (ticket.getStaffUUID().equals(uuid) && subject.hasPermission(Permissions.COMMAND_TICKET_UNCLAIM)) {
                                        action.append(Component.text()
                                                .append(plugin.fromLegacy(Messages.getUnclaimButton()))
                                                .hoverEvent(HoverEvent.showText(plugin.fromLegacy(Messages.getUnclaimButtonHover())))
                                                .clickEvent(ClickEvent.runCommand("/ticket unclaim " + ticket.getTicketID()))
                                                .build());
                                        action.append(plugin.fromLegacy(" "));
                                    }
                                }
                                if ((ticket.getStatus() == Open || ticket.getStatus() == Claimed && ticket.getStaffUUID().equals(uuid)) && subject.hasPermission(Permissions.COMMAND_TICKET_HOLD)) {
                                    action.append(Component.text()
                                            .append(plugin.fromLegacy(Messages.getHoldButton()))
                                            .hoverEvent(HoverEvent.showText(plugin.fromLegacy(Messages.getHoldButtonHover())))
                                            .clickEvent(ClickEvent.runCommand("/ticket hold " + ticket.getTicketID()))
                                            .build());
                                    action.append(plugin.fromLegacy(" "));
                                }
                            }
                            if (ticket.getStatus() == Held || ticket.getStatus() == Closed) {
                                if (subject.hasPermission(Permissions.COMMAND_TICKET_REOPEN)) {
                                    action.append(Component.text()
                                            .append(plugin.fromLegacy(Messages.getReopenButton()))
                                            .hoverEvent(HoverEvent.showText(plugin.fromLegacy(Messages.getReopenButtonHover())))
                                            .clickEvent(ClickEvent.runCommand("/ticket reopen " + ticket.getTicketID()))
                                            .build());
                                    action.append(plugin.fromLegacy(" "));
                                }
                            }
                            if (ticket.getStatus() == Held || ticket.getStatus() == Claimed || ticket.getStatus() == Open) {
                                if ((ticket.getStatus() == Claimed && ticket.getStaffUUID().equals(uuid)) || ticket.getStatus() == Open || ticket.getStatus() == Held) {
                                    if (subject.hasPermission(Permissions.COMMAND_TICKET_CLOSE_ALL) || subject.hasPermission(Permissions.COMMAND_TICKET_CLOSE_SELF)) {
                                        action.append(Component.text()
                                                .append(plugin.fromLegacy(Messages.getCloseButton()))
                                                .hoverEvent(HoverEvent.showText(plugin.fromLegacy(Messages.getCloseButtonHover())))
                                                .clickEvent(ClickEvent.runCommand("/ticket complete " + ticket.getTicketID()))
                                                .build());
                                        action.append(plugin.fromLegacy(" "));
                                    }
                                }
                            }
                            if (subject.hasPermission(Permissions.COMMAND_TICKET_COMMENT)) {
                                if (ticket.getStatus() != Claimed || ticket.getStatus() == Claimed && ticket.getStaffUUID().equals(uuid)) {
                                    action.append(Component.text()
                                            .append(plugin.fromLegacy(Messages.getCommentButton()))
                                            .hoverEvent(HoverEvent.showText(plugin.fromLegacy(Messages.getCommentButtonHover())))
                                            .clickEvent(ClickEvent.suggestCommand("/ticket comment " + ticket.getTicketID() + " "))
                                            .build());
                                }
                            }



                            TextComponent.@NotNull Builder send = Component.text();
                            send.append(plugin.fromLegacy("&a" + ticket.getWorld() + "&e x:&a" + ticket.getX() + "&e y:&a" + ticket.getY() + "&e z:&a" + ticket.getZ()));
                            if (subject.hasPermission(Permissions.COMMAND_TICKET_TELEPORT) && ticket.getServer().equalsIgnoreCase(Config.server)) {
                                send.hoverEvent(HoverEvent.showText(Messages.getTicketOnHoverTeleportTo()));
                                worldOptional.ifPresent(world ->
                                        send.clickEvent(SpongeComponents.executeCallback(teleportTo(world, ticket.getX(), ticket.getY(), ticket.getZ(), ticket.getPitch(), ticket.getYaw(), ticketID, player))));
                            }

                            contents.add(action.build());
                            if (!ticket.getStaffUUID().toString().equals("00000000-0000-0000-0000-000000000000")) {
                                if (ticket.getStatus() == Claimed)
                                    contents.add(plugin.fromLegacy("&eClaimed by: &7" + CommonUtil.getPlayerNameFromData(plugin, ticket.getStaffUUID())));
                                else if (ticket.getStatus() == Closed)
                                    contents.add(plugin.fromLegacy("&eHandled by: &7" + CommonUtil.getPlayerNameFromData(plugin, ticket.getStaffUUID())));
                            }
                            if (!ticket.getComment().isEmpty()) {
                                contents.add(plugin.fromLegacy("&eComment: &7" + ticket.getComment()));
                            }
                            contents.add(plugin.fromLegacy("&eOpened by: " + online + CommonUtil.getPlayerNameFromData(plugin, ticket.getPlayerUUID())));
                            contents.add(plugin.fromLegacy("&eWhen: " + CommonUtil.getTimeAgo(ticket.getTimestamp())));
                            contents.add(plugin.fromLegacy("&eServer: " + CommonUtil.checkTicketServer(ticket.getServer())));
                            if (!ticket.getPlayerUUID().toString().equals("00000000-0000-0000-0000-000000000000")) {
                                contents.add(send.build());
                            }
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
                            .sendTo(audience);
                    return CommandResult.success();
                } else {
                    throw new CommandException(Messages.getErrorPermission(Permissions.COMMAND_TICKET_READ_SELF));
                }
            }
        }
    }
    private Consumer<CommandCause> teleportTo(ServerWorld world, int x, int y, int z, double pitch, double yaw, int ticketID, ServerPlayer source) {
        return consumer -> {
            ServerLocation loc = world.location(x, y, z);
            Vector3d vect = new Vector3d(pitch, yaw, 0);
            source.setLocationAndRotation(loc, vect);
            source.sendMessage(Messages.getTeleportToTicket(ticketID));
        };
    }
}
