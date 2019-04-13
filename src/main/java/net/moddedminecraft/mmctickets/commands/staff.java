package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
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
        String separator = Messages.getStaffListSeperator();

        for(Player player : Sponge.getServer().getOnlinePlayers()) {
            if(player.hasPermission(Permissions.STAFF) && (!player.get(Keys.VANISH).isPresent()) {
                staff.append("&e" +player.getName());
                staff.append(separator);
            }
        }

        if (staff.length() > 0) {
            String staffString = staff.substring(0, staff.length() - separator.length());
            staffList.add(plugin.fromLegacy(staffString));
        }

        if(staffList.isEmpty()) {
            staffList.add(Messages.getStaffListEmpty());
        }

        paginationService.builder()
                .title(Messages.getStaffListTitle())
                .contents(staffList)
                .padding(Messages.getStaffListPadding())
                .sendTo(src);
        return CommandResult.success();
    }
}
