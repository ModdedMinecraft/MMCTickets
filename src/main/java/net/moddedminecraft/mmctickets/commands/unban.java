package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.data.PlayerData;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class unban implements CommandExecutor {

    private final Main plugin;

    public unban(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Parameter.Value<ServerPlayer> playerParameter = Parameter.player().key("player").build();

        final ServerPlayer user = context.requireOne(playerParameter);

        final List<PlayerData> playerData = new ArrayList<PlayerData>(plugin.getDataStore().getPlayerData());

        for (PlayerData pData : playerData) {
            if (pData.getPlayerUUID().equals(user.uniqueId())) {
                if (pData.getBannedStatus() == 0) {
                    throw new CommandException(Messages.getErrorNotBanned(user.name()));
                }
                pData.setBannedStatus(0);
                try {
                    plugin.getDataStore().updatePlayerData(pData);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CommandException(Messages.getErrorUnbanUser(user.name()));
                }
                return CommandResult.success();
            }
        }
        throw new CommandException(Messages.getErrorUserNotExist(user.name()));
    }
}
