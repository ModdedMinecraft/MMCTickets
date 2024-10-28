package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Config;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.database.DataStoreManager;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.io.IOException;


public class reload implements CommandExecutor {

    private final Main plugin;

    public reload(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        try {
            plugin.config = new Config(this.plugin);
            plugin.messages = new Messages(this.plugin);
            plugin.setDataStoreManager(new DataStoreManager(this.plugin));
            plugin.loadDataStore();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommandException(Messages.getErrorGen("Unable to load data."));
        }
        context.cause().audience().sendMessage(plugin.fromLegacy("&eTicket and Player data reloaded."));
        return CommandResult.success();
    }
}
