package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class ban implements CommandExecutor {

    private final Main plugin;

    public ban(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<ServerPlayer> playerParameter = Parameter.player().key("playername").build();
        final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getDataStore().getPlayerData());

        final ServerPlayer user = context.requireOne(playerParameter);

        for (PlayerData pData : playerData) {
            CommonUtil.checkPlayerData(plugin, user);
            if (pData.getPlayerUUID().equals(user.uniqueId())) {
                if (pData.getBannedStatus() == 1) {
                    throw new CommandException(Messages.getErrorBannedAlready(user.name()));
                }
                pData.setBannedStatus(1);
                try {
                    plugin.getDataStore().updatePlayerData(pData);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CommandException(Messages.getErrorBanUser(user.name()));
                }
                return CommandResult.success();
            }
        }
        throw new CommandException(Messages.getErrorUserNotExist(user.name()));
    }
}
