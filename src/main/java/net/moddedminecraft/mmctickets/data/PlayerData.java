package net.moddedminecraft.mmctickets.data;

import com.google.common.reflect.TypeToken;
import net.moddedminecraft.mmctickets.util.PlayerDataUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.List;
import java.util.UUID;

public class PlayerData extends PlayerDataUtil {

    public PlayerData(UUID playerUUID, String playerName, int bannedStatus) {
        super(playerUUID, playerName, bannedStatus);
    }

    public static class PlayerDataSerializer implements TypeSerializer<PlayerData> {
        @SuppressWarnings("serial")
        final public static TypeToken<List<PlayerData>> token = new TypeToken<List<PlayerData>>() {};

        @Override
        public PlayerData deserialize(TypeToken<?> token, ConfigurationNode node) throws ObjectMappingException {
            return new PlayerData(
                    UUID.fromString(node.getNode("uuid").getString()),
                    node.getNode("name").getString(),
                    node.getNode("bannedstatus").getInt());
        }

        @Override
        public void serialize(TypeToken<?> token, PlayerData playerData, ConfigurationNode node) throws ObjectMappingException {
            node.getNode("uuid").setValue(playerData.playerUUID.toString());
            node.getNode("name").setValue(playerData.playerName);
            node.getNode("bannedstatus").setValue(playerData.bannedStatus);
        }
    }
}
