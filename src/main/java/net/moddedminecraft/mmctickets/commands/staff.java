package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

public class staff implements CommandExecutor {

    private final Main plugin;
    public staff(Main instance) {
        plugin = instance;
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        List<Text> staffList = new ArrayList<>();
        StringBuilder staff = new StringBuilder();
        String separator = Messages.staffListSeperator;

        for(Player player : Sponge.getServer().getOnlinePlayers()) {
            if(player.hasPermission("mmctickets.staff")) {
                staff.append("&e" +player.getName());
                staff.append(separator);
            }
        }

        String staffString = staff.substring(0, staff.length() - separator.length());
        staffList.add(plugin.fromLegacy(staffString));

        if(staffList.isEmpty()) {
            staffList.add(Messages.parse(Messages.staffListEmpty));
        }

        paginationService.builder()
                .title(Messages.parse(Messages.staffListTitle))
                .contents(staffList)
                .padding(Text.of(Messages.staffListPadding))
                .sendTo(src);
        return CommandResult.success();
    }
}
