package net.moddedminecraft.mmctickets.database;

import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.TicketData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MYSQLDataStore implements IDataStore  {

    @Override
    public String getDatabaseName() {
        return null;
    }

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public List<TicketData> getTicketData() {
        return null;
    }

    @Override
    public List<PlayerData> getPlayerData() {
        return null;
    }

    @Override
    public ArrayList<UUID> getNotifications() {
        return null;
    }

    @Override
    public Optional<TicketData> getTicket(int ticketID) {
        return Optional.empty();
    }

    @Override
    public boolean addTicketData(TicketData ticketData) {
        return false;
    }

    @Override
    public boolean addPlayerData(PlayerData playerData) {
        return false;
    }

    @Override
    public boolean updateTicketData(TicketData ticketData) {
        return false;
    }

    @Override
    public boolean updatePlayerData(PlayerData playerData) {
        return false;
    }
}
