package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Config;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class open implements CommandExecutor {

    private final Main plugin;

    public open(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final String message = args.<String>getAll("message").toString();

        if (message.split("\\s+").length < Config.minWords) {
            throw new CommandException(Messages.parse(Messages.ticketTooShort, Config.minWords));
        }

        if (!(src instanceof Player)) {
            throw new CommandException(Messages.parse(Messages.errorGeneral, "Only players can run this command"));
        }

        Player player = (Player) src;
        int ticketID = plugin.getTickets().size() + 1;

        plugin.addTicket(new TicketData(ticketID,
                src.getName(),
                "",
                "",
                System.currentTimeMillis()/1000,
                player.getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ(),
                player.getHeadRotation().getX(),
                player.getHeadRotation().getY(),
                message,
                0,
                0));

        try {
            plugin.saveData();
            player.sendMessage(Messages.parse(Messages.ticketOpenUser, ticketID));
            if (Config.staffNotification) {
                CommonUtil.notifyOnlineStaff(Messages.parse(Messages.ticketOpen, player.getName() , ticketID));
            }
            if (Config.titleNotification) {
                CommonUtil.notifyOnlineStaffTitle(Messages.parse(Messages.ticketTitleNotification, player.getName() , ticketID));
            }
            if (Config.soundNotification) {
                CommonUtil.notifyOnlineStaffSound();
            }
        } catch (Exception e) {
            player.sendMessage(Messages.parse(Messages.errorGeneral, "Data was not saved correctly."));
            e.printStackTrace();
        }
        return CommandResult.success();
    }
}
