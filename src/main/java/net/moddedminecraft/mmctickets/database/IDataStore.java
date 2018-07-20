package net.moddedminecraft.mmctickets.database;

import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.TicketData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface IDataStore {

    public abstract String getDatabaseName();

    public abstract boolean load();

    public abstract List<TicketData> getTicketData();

    public abstract List<PlayerData> getPlayerData();

    public abstract ArrayList<UUID> getNotifications();

    public abstract boolean addTicketData(TicketData ticketData);

    public abstract boolean addPlayerData(PlayerData playerData);

}
