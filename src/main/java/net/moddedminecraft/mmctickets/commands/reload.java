package net.moddedminecraft.mmctickets.commands;

import net.moddedminecraft.mmctickets.Main;
import net.moddedminecraft.mmctickets.config.Config;
import net.moddedminecraft.mmctickets.config.Messages;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.io.IOException;


public class reload implements CommandExecutor {

    private final Main plugin;

    public reload(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        try {
            plugin.config = new Config(this.plugin);
            plugin.messages = new Messages(this.plugin);
            plugin.loadData();
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
            throw new CommandException(Messages.parse(Messages.errorGeneral, "Unable to load data."));
        }
        return CommandResult.success();
    }
}
