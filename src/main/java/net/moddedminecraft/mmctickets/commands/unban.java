package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.PlayerData;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;

public class unban implements CommandExecutor {

    private final Main plugin;

    public unban(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final Player player = args.<Player>getOne("playername").get();
        final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getPlayerData());

        for (PlayerData pData : playerData) {
            if (pData.getPlayerUUID().equals(player.getUniqueId())) {
                if (pData.getBannedStatus() == 0) {
                    throw new CommandException(Messages.getErrorNotBanned(player.getName()));
                }
                pData.setBannedStatus(0);
                try {
                    plugin.saveData();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CommandException(Messages.getErrorUnbanUser(player.getName()));
                }
                return CommandResult.success();
            }
        }
        throw new CommandException(Messages.getErrorUserNotExist(player.getName()));
    }
}
