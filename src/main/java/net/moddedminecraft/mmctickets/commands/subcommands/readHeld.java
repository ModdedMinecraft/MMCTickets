package net.moddedminecraft.mmctickets.commands.subcommands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationService;

import java.util.ArrayList;
import java.util.List;

import static net.moddedminecraft.mmctickets.data.ticketStatus.Held;

public class readHeld implements CommandExecutor {

    private final Main plugin;

    public readHeld(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        final List<TicketData> tickets = new ArrayList<TicketData>(plugin.getDataStore().getTicketData());

        Audience audience = context.cause().audience();

        if (tickets.isEmpty()) {
            throw new CommandException(Messages.getErrorGen("Tickets list is empty."));
        } else {
            PaginationService paginationService = Sponge.serviceProvider().provide(PaginationService.class).get();
            List<Component> contents = new ArrayList<>();
            for (TicketData ticket : tickets) {
                if (ticket.getStatus() == Held) {
                    String online = CommonUtil.isUserOnline(ticket.getPlayerUUID());
                    TextComponent.@NotNull Builder send = Component.text();
                    send.append(plugin.fromLegacy("&6#" + ticket.getTicketID() + " " + CommonUtil.getTimeAgo(ticket.getTimestamp()) + " by " + online + CommonUtil.getPlayerNameFromData(plugin, ticket.getPlayerUUID()) + " &6on " + CommonUtil.checkTicketServer(ticket.getServer()) + " &6- &7" + CommonUtil.shortenMessage(ticket.getMessage())));
                    send.clickEvent(ClickEvent.runCommand("/ticket read " + ticket.getTicketID()));
                    send.hoverEvent(HoverEvent.showText(plugin.fromLegacy("Click here to get more details for ticket #" + ticket.getTicketID())));
                    contents.add(send.build());

                }
            }

            if (contents.isEmpty()) {
                contents.add(Messages.getTicketReadNoneHeld());
            }
            paginationService.builder()
                    .title(plugin.fromLegacy("&6Held Tickets"))
                    .contents(contents)
                    .padding(plugin.fromLegacy("-"))
                    .sendTo(audience);
        }
        return CommandResult.success();
    }
}
