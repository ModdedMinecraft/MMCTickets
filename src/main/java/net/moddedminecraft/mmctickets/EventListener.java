package net.moddedminecraft.mmctickets;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class EventListener {

    private final Main plugin;
    public EventListener(Main instance) {
        plugin = instance;
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join event, @Root Player player) {
        //TODO Add login notification for staff

        //TODO Add login notification for users for when the ticket has been closed while offline
    }
}
