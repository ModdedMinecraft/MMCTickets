package net.moddedminecraft.mmctickets.database;

import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.TicketData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDataStore {

    public abstract String getDatabaseName();

    public abstract boolean load();

    public abstract List<TicketData> getTicketData();

    public abstract List<PlayerData> getPlayerData();

    public abstract ArrayList<UUID> getNotifications();

    public abstract Optional<TicketData> getTicket(int ticketID);

    public abstract boolean addTicketData(TicketData ticketData);

    public abstract boolean addPlayerData(PlayerData playerData);

    public abstract boolean updateTicketData(TicketData ticketData);

    public abstract boolean updatePlayerData(PlayerData playerData);
}
