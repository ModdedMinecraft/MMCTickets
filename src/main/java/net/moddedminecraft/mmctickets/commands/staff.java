package net.moddedminecraft.mmctickets.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationService;

import java.util.ArrayList;
import java.util.List;

public class staff implements CommandExecutor {

    private final Main plugin;
    public staff(Main instance) {
        plugin = instance;
    }

    public CommandResult execute(CommandContext context) throws CommandException {
        Audience audience = context.cause().audience();

        PaginationService paginationService = Sponge.serviceProvider().provide(PaginationService.class).get();
        List<Component> staffList = new ArrayList<>();
        StringBuilder staff = new StringBuilder();
        String separator = Messages.getStaffListSeperator();

        for(ServerPlayer player : Sponge.server().onlinePlayers()) {
            if(player.hasPermission(Permissions.STAFF) && (!player.get(Keys.VANISH_STATE).get().invisible())) {
                staff.append("&e" + player.name());
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
                .sendTo(audience);
        return CommandResult.success();
    }
}
