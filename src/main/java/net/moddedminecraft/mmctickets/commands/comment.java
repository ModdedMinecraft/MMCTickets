package net.moddedminecraft.mmctickets.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static net.moddedminecraft.mmctickets.data.ticketStatus.Claimed;

public class comment implements CommandExecutor {

    private final Main plugin;

    public comment(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<Integer> ticketIDParameter = Parameter.integerNumber().key("ticketID").build();
        Parameter.Value<String> commentParameter = Parameter.remainingJoinedStrings().key("comment").build();

        final int ticketID = context.requireOne(ticketIDParameter);
        final String comment = context.requireOne(commentParameter);

        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());

        Subject subject = context.cause().subject();
        Audience audience = context.cause().audience();

        ServerPlayer player = null;
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        if (context.cause().root() instanceof ServerPlayer) {
            player = (ServerPlayer) context.cause().root();
            uuid = player.uniqueId();
        }
        String staffName = "Console";
        if (player != null) {
            staffName = player.name();
        }

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    if (!ticket.getStaffUUID().equals(uuid) && ticket.getStatus() == Claimed && !subject.hasPermission(Permissions.CLAIMED_TICKET_BYPASS)) {
                        throw new CommandException(Messages.getErrorTicketClaim(ticket.getTicketID(), CommonUtil.getPlayerNameFromData(plugin, ticket.getStaffUUID())));
                    }
                    if (!ticket.getComment().isEmpty()) {
                        if (subject.hasPermission(Permissions.COMMAND_TICKET_EDIT_COMMENT)) {
                            TextComponent.@NotNull Builder action = Component.text();
                            action.append(Component.text()
                                    .append(plugin.fromLegacy(Messages.getYesButton()))
                                    .hoverEvent(HoverEvent.showText(plugin.fromLegacy(Messages.getYesButtonHover())))
                                    .clickEvent(SpongeComponents.executeCallback(changeTicketComment(ticketID, comment, staffName)))
                                    .build());
                            audience.sendMessage(Messages.getTicketCommentedit(ticketID));
                            audience.sendMessage(action.build());
                            return CommandResult.success();
                        } else {
                            throw new CommandException(Messages.getErrorGen("There is already a comment on this ticket."));
                        }
                    }
                    ticket.setComment(comment);

                    try {
                        plugin.getDataStore().updateTicketData(ticket);
                    } catch (Exception e) {
                        audience.sendMessage(Messages.getErrorGen("Unable to comment on ticket"));
                        e.printStackTrace();
                    }

                    Optional<ServerPlayer> ticketPlayerOP = Sponge.server().player(ticket.getPlayerUUID());
                    if (ticketPlayerOP.isPresent()) {
                        ServerPlayer ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketComment(ticket.getTicketID(), staffName));
                    }

                    audience.sendMessage(Messages.getTicketCommentUser(ticket.getTicketID()));

                    return CommandResult.success();
                }
            }
                throw new CommandException(Messages.getTicketNotExist(ticketID));
        }
    }

    private Consumer<CommandCause> changeTicketComment(int ticketID, String comment, String name) {
        return consumer -> {
            final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());
            for (TicketData ticket : tickets) {
                if (ticket.getTicketID() == ticketID) {
                    ticket.setComment(comment);

                    try {
                        plugin.getDataStore().updateTicketData(ticket);
                    } catch (Exception e) {
                        consumer.audience().sendMessage(Messages.getErrorGen("Unable to comment on ticket"));
                        e.printStackTrace();
                    }

                    Optional<ServerPlayer> ticketPlayerOP = Sponge.server().player(ticket.getPlayerUUID());
                    if (ticketPlayerOP.isPresent()) {
                        ServerPlayer ticketPlayer = ticketPlayerOP.get();
                        ticketPlayer.sendMessage(Messages.getTicketComment(ticket.getTicketID(), name));
                    }

                    consumer.audience().sendMessage(Messages.getTicketCommentUser(ticket.getTicketID()));
                }
            }
        };
    }

}
