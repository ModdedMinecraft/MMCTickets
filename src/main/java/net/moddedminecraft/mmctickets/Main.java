package net.moddedminecraft.mmctickets;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import net.moddedminecraft.mmctickets.commands.*;
import net.moddedminecraft.mmctickets.commands.subcommands.readClosed;
import net.moddedminecraft.mmctickets.commands.subcommands.readHeld;
import net.moddedminecraft.mmctickets.commands.subcommands.readSelf;
import net.moddedminecraft.mmctickets.config.Config;
import net.moddedminecraft.mmctickets.config.Messages;
import net.moddedminecraft.mmctickets.config.Permissions;
import net.moddedminecraft.mmctickets.data.PlayerData;
import net.moddedminecraft.mmctickets.data.PlayerData.PlayerDataSerializer;
import net.moddedminecraft.mmctickets.data.TicketData;
import net.moddedminecraft.mmctickets.data.TicketData.TicketSerializer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

@Plugin(id = "mmctickets", name = "MMCTickets", version = "0.1", description = "A real time ticket system")
public class Main {

    @Inject
    public Logger logger;

    @Inject
    private Metrics metrics;

    @Inject
    @DefaultConfig(sharedRoot = false)
    public Path defaultConf;

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path ConfigDir;

    private static SimpleDateFormat sdf = new SimpleDateFormat("MMM.dd kk:mm z");

    private Config config;
    private Messages messages;

    private CommandManager cmdManager = Sponge.getCommandManager();

    private ArrayList<String> notifications;
    private Map<Integer, TicketData> tickets;
    private Map<UUID, PlayerData> playersData;

    @Listener
    public void Init(GameInitializationEvent event) throws IOException, ObjectMappingException {
        Sponge.getEventManager().registerListeners(this, new EventListener(this));

        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TicketData.class), new TicketSerializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(PlayerData.class), new PlayerDataSerializer());

        config = new Config(this);
        messages = new Messages(this);
        loadCommands();
        loadData();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {
        logger.info("MMCTickets Loaded");
        logger.info("Tickets loaded: " + tickets.size());
        logger.info("Notifications loaded: " + notifications.size());
    }

    @Listener
    public void onPluginReload(GameReloadEvent event) throws IOException, ObjectMappingException {
        this.config = new Config(this);
        this.messages = new Messages(this);
    }

    private void loadCommands() {
        //TODO Add commands

        // /stafflist
        CommandSpec staffList = CommandSpec.builder()
                .description(Text.of("List online staff members"))
                .executor(new staff(this))
                .build();

        // /ticket read self
        CommandSpec readSelf = CommandSpec.builder()
                .description(Text.of("Display a list of all tickets the player owns"))
                .executor(new readSelf(this))
                .build();

        // /ticket read closed
        CommandSpec readClosed = CommandSpec.builder()
                .description(Text.of("Display a list of all closed tickets"))
                .executor(new readClosed(this))
                .permission(Permissions.COMMAND_TICKET_READ_ALL)
                .build();

        // /ticket read held
        CommandSpec readHeld = CommandSpec.builder()
                .description(Text.of("Display a list of all held tickets"))
                .executor(new readHeld(this))
                .permission(Permissions.COMMAND_TICKET_READ_ALL)
                .build();

        // /ticket read (ticketID)
        CommandSpec ticketRead = CommandSpec.builder()
                .description(Text.of("Read all ticket or give more detail of a specific ticket"))
                .executor(new read(this))
                .child(readClosed, "closed")
                .child(readHeld, "held")
                .child(readSelf, "self")
                .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("ticketID"))))
                .build();

        // /ticket close (ticketID) (comment)
        CommandSpec ticketClose = CommandSpec.builder()
                .description(Text.of("Close a ticket"))
                .executor(new close(this))
                .arguments(GenericArguments.integer(Text.of("ticketID")),
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("comment"))))
                .build();

        // /ticket open
        CommandSpec ticketOpen = CommandSpec.builder()
                .description(Text.of("Open a ticket"))
                .executor(new open(this))
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
                .permission(Permissions.COMMAND_TICKET_OPEN)
                .build();

        // /ticket
        CommandSpec ticketBase = CommandSpec.builder()
                .description(Text.of("Ticket base command, Displays help"))
                .executor(new ticket(this))
                .child(ticketOpen, "open")
                .child(ticketRead, "read", "check")
                .child(ticketClose, "close")
                .build();

        cmdManager.register(this, ticketOpen, "modreq");
        cmdManager.register(this, ticketRead, "check");
        cmdManager.register(this, ticketBase, "ticket");
        cmdManager.register(this, staffList, "stafflist");
    }

    public Logger getLogger() {
        return logger;
    }

    synchronized public void loadData() throws IOException, ObjectMappingException {
        HoconConfigurationLoader loader = getDataLoader();
        ConfigurationNode rootNode = loader.load();

        List<TicketData> ticketList = rootNode.getNode("Tickets").getList(TypeToken.of(TicketData.class));
        this.tickets = new HashMap<Integer, TicketData>();
        this.notifications = new ArrayList<String>();
        for (TicketData ticket : ticketList) {
            this.tickets.put(ticket.getTicketID(), ticket);
            if (ticket.getNotified() == 0 && ticket.getStatus() == 3) this.notifications.add(ticket.getName());
        }

        List<PlayerData> playersDataList = rootNode.getNode("PlayersData").getList(TypeToken.of(PlayerData.class));
        this.playersData = new HashMap<UUID, PlayerData>();
        for (PlayerData pd : playersDataList) {
            this.playersData.put(pd.getPlayerUUID(), pd);
        }
    }

    synchronized public void saveData() throws IOException, ObjectMappingException {
        HoconConfigurationLoader loader = getDataLoader();
        ConfigurationNode rootNode = loader.load();

        rootNode.getNode("Tickets").setValue(TicketSerializer.token, new ArrayList<TicketData>(this.tickets.values()));
        rootNode.getNode("PlayersData").setValue(PlayerDataSerializer.token, new ArrayList<PlayerData>(this.playersData.values()));

        loader.save(rootNode);
    }

    public HoconConfigurationLoader getDataLoader() {
        return HoconConfigurationLoader.builder().setPath(this.ConfigDir.resolve("TicketData.conf")).build();
    }

    public TicketData getTicket(int ticketID) {
        return this.tickets.get(ticketID);
    }

    public Collection<TicketData> getTickets() {
        return Collections.unmodifiableCollection(this.tickets.values());
    }

    public ArrayList<String> getNotifications() {
        return this.notifications;
    }

    public void removeNotification(String name) {
        this.notifications.remove(name);
    }

    public TicketData addTicket(TicketData ticket) {
        return this.tickets.put(ticket.getTicketID(), ticket);
    }

    public Text fromLegacy(String legacy) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(legacy);
    }
}
