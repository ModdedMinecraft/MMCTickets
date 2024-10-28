package net.moddedminecraft.mmctickets.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Permissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.permission.Subject;

import java.util.ArrayList;
import java.util.List;

public class ticket implements CommandExecutor {

    private final Main plugin;
    public ticket(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        showHelp(context);
        return CommandResult.success();
    }

    void showHelp(CommandContext context) {
        PaginationService paginationService = Sponge.serviceProvider().provide(PaginationService.class).get();

        Subject subject = context.cause().subject();
        Audience audience = context.cause().audience();

        List<Component> contents = new ArrayList<>();

        if (subject.hasPermission(Permissions.COMMAND_STAFFLIST)) contents.add(formatHelp("/stafflist", "Display a list of online staff members."));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_OPEN)) contents.add(formatHelp("/ticket", "open [reason for opening]", "Open a ticket."));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_CLOSE_ALL) || subject.hasPermission(Permissions.COMMAND_TICKET_CLOSE_SELF)) contents.add(formatHelp("/ticket", "close [ticketID] (comment)", "Close an open ticket."));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_ASSIGN)) contents.add(formatHelp("/ticket", "assign [ticketID] [user]", "Assign an open ticket to a specified user."));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_HOLD)) contents.add(formatHelp("/ticket", "hold [ticketID]", "Put an open ticket on hold."));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_READ_ALL) || subject.hasPermission(Permissions.COMMAND_TICKET_READ_SELF)) contents.add(formatHelp("/ticket", "check (ticketID)", "Display a list of open tickets / Give more detail of a ticketID."));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_REOPEN)) contents.add(formatHelp("/ticket", "reopen", "Reopen's a closed ticket."));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_TELEPORT)) contents.add(formatHelp("/ticket", "tp [ticketID]", "Teleport to where a ticket was created."));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_CLAIM)) contents.add(formatHelp("/ticket", "claim [ticketID]", "Claim an open ticket to let people know you are working on it."));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_UNCLAIM)) contents.add(formatHelp("/ticket", "unclaim [ticketID]", "Unclaim a claimed ticket"));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_BAN)) contents.add(formatHelp("/ticket", "ban [playername]", "Ban a player from opening new tickets"));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_BAN)) contents.add(formatHelp("/ticket", "unban [playername]", "Unban a player from opening new tickets"));
        if (subject.hasPermission(Permissions.COMMAND_TICKET_COMMENT)) contents.add(formatHelp("/ticket", "comment [ticketID] [comment]", "put a comment on a ticket"));
        if (subject.hasPermission(Permissions.COMMAND_RELOAD)) contents.add(formatHelp("/ticket", "reload", "Reload ticket and player data."));


        if (!contents.isEmpty()) {
            paginationService.builder()
                    .title(plugin.fromLegacy("&6MMCTickets Help"))
                    .contents(contents)
                    .header(plugin.fromLegacy("&3[] = required  () = optional"))
                    .padding(Component.text("="))
                    .sendTo(audience);
        } else {
            paginationService.builder()
                    .title(plugin.fromLegacy("&6MMCTickets Help"))
                    .contents(plugin.fromLegacy("&3You currently do not have any permissions for this plugin."))
                    .padding(Component.text("="))
                    .sendTo(audience);
        }
    }

    public Component formatHelp(String command, String comment) {
        return plugin.fromLegacy("&3" + command + " &8- &7" + comment);
    }

    public Component formatHelp(String command, String args, String comment) {
        return plugin.fromLegacy("&3" + command + " &b" + args + " &8- &7" + comment);
    }
}
