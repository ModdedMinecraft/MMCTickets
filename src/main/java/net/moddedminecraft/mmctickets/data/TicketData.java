package net.moddedminecraft.mmctickets.data;

import net.moddedminecraft.mmctickets.util.TicketDataUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;


public class TicketData extends TicketDataUtil {

    public TicketData(int ticketID, String playerUUID, String staffUUID, String comment, long timestamp, String world, int x, int y, int z, Double yaw, Double pitch, String message, ticketStatus status, int notified, String server) {
        super(ticketID, playerUUID, staffUUID, comment, timestamp, world, x, y, z, yaw, pitch, message, status, notified,server);
    }

    public static class TicketSerializer implements TypeSerializer<TicketData> {

        @Override
        public TicketData deserialize(Type type, ConfigurationNode data) {
            return new TicketData(
                    data.node("ticketID").getInt(),
                    data.node("playerUUID").getString(),
                    data.node("staffUUID").getString(),
                    data.node("comment").getString(),
                    data.node("timestamp").getInt(),
                    data.node("world").getString(),
                    data.node("x").getInt(),
                    data.node("y").getInt(),
                    data.node("z").getInt(),
                    data.node("yaw").getDouble(),
                    data.node("pitch").getDouble(),
                    data.node("message").getString(),
                    ticketStatus.valueOf(data.node("status").getString()),
                    data.node("notified").getInt(),
                    data.node("server").getString());
        }

        @Override
        public void serialize(Type type, @Nullable TicketData ticket, ConfigurationNode data) throws SerializationException {
            if (ticket != null) {
                data.node("ticketID").set(ticket.ticketID);
                data.node("playerUUID").set(ticket.playerUUID);
                data.node("staffUUID").set(ticket.staffUUID);
                data.node("comment").set(ticket.comment);
                data.node("timestamp").set(ticket.timestamp);
                data.node("world").set(ticket.world);
                data.node("x").set(ticket.x);
                data.node("y").set(ticket.y);
                data.node("z").set(ticket.z);
                data.node("yaw").set(ticket.yaw);
                data.node("pitch").set(ticket.pitch);
                data.node("message").set(ticket.message);
                data.node("status").set(ticket.status.toString());
                data.node("notified").set(ticket.notified);
                data.node("server").set(ticket.server);
            }
        }
    }

}
