package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;

public class ticket implements CommandExecutor {

    private final Main plugin;
    public ticket(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        showHelp(src);
        return CommandResult.success();
    }

    void showHelp(CommandSource sender) {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();

        List<Text> contents = new ArrayList<>();
        if (sender.hasPermission("mmcticket.commands.user.stafflist")) contents.add(formatHelp("/stafflist", "Display a list of online staff members."));
        if (sender.hasPermission("mmcticket.commands.user.open")) contents.add(formatHelp("/ticket", "open [reason for opening]", "Open a ticket."));
        if (sender.hasPermission("mmcticket.commands.staff.close")) contents.add(formatHelp("/ticket", "close [ticketID]", "Close an open ticket."));
        if (sender.hasPermission("mmcticket.commands.staff.assign")) contents.add(formatHelp("/ticket", "assign [ticketID] [user]", "Assign an open ticket to a specified user."));
        if (sender.hasPermission("mmcticket.commands.staff.hold")) contents.add(formatHelp("/ticket", "hold [ticketID]", "Put an open ticket on hold."));
        if (sender.hasPermission("mmcticket.commands.staff.check")) contents.add(formatHelp("/ticket", "check (ticketID)", "Display a list of open TicketData / Give more detail of a ticketID."));
        if (sender.hasPermission("mmcticket.commands.staff.reopen")) contents.add(formatHelp("/ticket", "reopen", "Reopen's a closed ticket."));
        if (sender.hasPermission("mmcticket.commands.staff.teleport")) contents.add(formatHelp("/ticket", "tp [ticketID]", "Teleport to where a ticket was created."));
        if (sender.hasPermission("mmcticket.commands.staff.claim")) contents.add(formatHelp("/ticket", "claim [ticketID]", "Claim an open ticket to let people know you are working on it."));
        if (sender.hasPermission("mmcticket.commands.staff.unclaim")) contents.add(formatHelp("/ticket", "unclaim [ticketID]", "Unclaim a claimed ticket"));


        if (!contents.isEmpty()) {
            paginationService.builder()
                    .title(plugin.fromLegacy("&6MMCTickets Help"))
                    .contents(contents)
                    .header(plugin.fromLegacy("&3[] = required  () = optional"))
                    .padding(Text.of("="))
                    .sendTo(sender);
        } else {
            paginationService.builder()
                    .title(plugin.fromLegacy("&6MMCTickets Help"))
                    .contents(plugin.fromLegacy("&3You currently do not have any permissions for this plugin."))
                    .padding(Text.of("="))
                    .sendTo(sender);
        }
    }

    public Text formatHelp(String command, String comment) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked("&3" + command + " &8- &7" + comment);
    }

    public Text formatHelp(String command, String args, String comment) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked("&3" + command + " &b" + args + " &8- &7" + comment);
    }
}
