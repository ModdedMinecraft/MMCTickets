package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ban implements CommandExecutor {

    private final Main plugin;

    public ban(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final Player player = args.<Player>getOne("playername").get();
        final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getPlayerData());

        for (PlayerData pData : playerData) {
            CommonUtil.checkPlayerData(plugin, player);
            if (pData.getPlayerUUID().equals(player.getUniqueId())) {
                if (pData.getBannedStatus() == 1) {
                    throw new CommandException(Messages.getErrorBannedAlready(player.getName()));
                }
                pData.setBannedStatus(1);
                try {
                    plugin.saveData();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CommandException(Messages.getErrorBanUser(player.getName()));
                }
                return CommandResult.success();
            }
        }
        throw new CommandException(Messages.getErrorUserNotExist(player.getName()));
    }
}
