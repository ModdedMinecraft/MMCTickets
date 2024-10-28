package net.moddedminecraft.mmctickets.data;

import com.google.common.reflect.TypeToken;
import net.moddedminecraft.mmctickets.util.PlayerDataUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.UUID;

public class PlayerData extends PlayerDataUtil {

    public PlayerData(UUID playerUUID, String playerName, int bannedStatus) {
        super(playerUUID, playerName, bannedStatus);
    }

    public static class PlayerDataSerializer implements TypeSerializer<PlayerData> {

        @Override
        public PlayerData deserialize(Type type, ConfigurationNode node) {
            return new PlayerData(
                    UUID.fromString(node.node("uuid").getString()),
                    node.node("name").getString(),
                    node.node("bannedstatus").getInt());
        }

        @Override
        public void serialize(Type type, @Nullable PlayerData obj, ConfigurationNode node) throws SerializationException {
            if (obj != null) {
                node.node("uuid").set(obj.playerUUID.toString());
                node.node("name").set(obj.playerName);
                node.node("bannedstatus").set(obj.bannedStatus);
            }
        }
    }
}
