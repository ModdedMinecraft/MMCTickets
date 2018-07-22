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
import org.spongepowered.api.entity.living.player.User;

import java.util.ArrayList;
import java.util.List;

public class unban implements CommandExecutor {

    private final Main plugin;

    public unban(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final User user = args.<Player>getOne("playername").get();
        final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getDataStore().getPlayerData());

        for (PlayerData pData : playerData) {
            if (pData.getPlayerUUID().equals(user.getUniqueId())) {
                if (pData.getBannedStatus() == 0) {
                    throw new CommandException(Messages.getErrorNotBanned(user.getName()));
                }
                pData.setBannedStatus(0);
                try {
                    plugin.getDataStore().updatePlayerData(pData);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CommandException(Messages.getErrorUnbanUser(user.getName()));
                }
                return CommandResult.success();
            }
        }
        throw new CommandException(Messages.getErrorUserNotExist(user.getName()));
    }
}
