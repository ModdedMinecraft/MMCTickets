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
import net.moddedminecraft.mmctickets.util.CommonUtil;
import net.moddedminecraft.mmctickets.util.UpdateChecker;
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
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.moddedminecraft.mmctickets.data.ticketStatus.*;

@Plugin(id = "mmctickets", name = "MMCTickets", version = "1.4.0", description = "A real time ticket system")
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

    public Config config;
    public Messages messages;

    private CommandManager cmdManager = Sponge.getCommandManager();

    private ArrayList<String> waitTimer;
    private ArrayList<UUID> notifications;
    private Map<Integer, TicketData> tickets;
    public Map<UUID, PlayerData> playersData;

    public UpdateChecker updatechecker;
    public String version = "1.4.0";

    @Listener
    public void Init(GameInitializationEvent event) throws IOException, ObjectMappingException {
        Sponge.getEventManager().registerListeners(this, new EventListener(this));
        convertOldData();

        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TicketData.class), new TicketSerializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(PlayerData.class), new PlayerDataSerializer());

        config = new Config(this);
        messages = new Messages(this);
        loadCommands();
        loadData();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {
        checkOldNames();
        logger.info("MMCTickets Loaded");
        logger.info("Tickets loaded: " + tickets.size());
        logger.info("Notifications loaded: " + notifications.size());
        logger.info("PlayerData loaded: " + playersData.size());

        this.waitTimer = new ArrayList<String>();

        updatechecker = new UpdateChecker(this, version);
        updatechecker.startUpdateCheck();

        //start ticket nag timer
        nagTimer();
    }

    @Listener
    public void onPluginReload(GameReloadEvent event) throws IOException, ObjectMappingException {
        this.config = new Config(this);
        this.messages = new Messages(this);
    }

    private void loadCommands() {
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

        // /ticket ban (username)
        CommandSpec ticketBan = CommandSpec.builder()
                .description(Text.of("Ban a player from being able to create new tickets"))
                .executor(new ban(this))
                .arguments(GenericArguments.player(Text.of("playername")))
                .permission(Permissions.COMMAND_TICKET_BAN)
                .build();

        // /ticket unban (username)
        CommandSpec ticketUnban = CommandSpec.builder()
                .description(Text.of("Unban a player from being able to create new tickets"))
                .executor(new unban(this))
                .arguments(GenericArguments.player(Text.of("playername")))
                .permission(Permissions.COMMAND_TICKET_BAN)
                .build();

        // /ticket reload
        CommandSpec ticketReload = CommandSpec.builder()
                .description(Text.of("Reload ticket and player data."))
                .executor(new reload(this))
                .permission(Permissions.COMMAND_RELOAD)
                .build();

        // /ticket claim (ticketID)
        CommandSpec ticketClaim = CommandSpec.builder()
                .description(Text.of("Claim a ticket"))
                .executor(new claim(this))
                .arguments(GenericArguments.integer(Text.of("ticketID")))
                .permission(Permissions.COMMAND_TICKET_CLAIM)
                .build();

        // /ticket unclaim (ticketID)
        CommandSpec ticketUnclaim = CommandSpec.builder()
                .description(Text.of("Unclaim a ticket"))
                .executor(new unclaim(this))
                .arguments(GenericArguments.integer(Text.of("ticketID")))
                .permission(Permissions.COMMAND_TICKET_UNCLAIM)
                .build();

        // /ticket reopen (ticketID)
        CommandSpec ticketReopen = CommandSpec.builder()
                .description(Text.of("Reopen a ticket"))
                .executor(new reopen(this))
                .arguments(GenericArguments.integer(Text.of("ticketID")))
                .permission(Permissions.COMMAND_TICKET_REOPEN)
                .build();

        // /ticket assign (ticketID) (player)
        CommandSpec ticketAssign = CommandSpec.builder()
                .description(Text.of("Unclaim a ticket"))
                .executor(new assign(this))
                .arguments(GenericArguments.integer(Text.of("ticketID")),
                        GenericArguments.player(Text.of("player")))
                .permission(Permissions.COMMAND_TICKET_ASSIGN)
                .build();

        // /ticket hold (ticketID)
        CommandSpec ticketHold = CommandSpec.builder()
                .description(Text.of("Put a ticket on hold"))
                .executor(new hold(this))
                .arguments(GenericArguments.integer(Text.of("ticketID")))
                .permission(Permissions.COMMAND_TICKET_HOLD)
                .build();

        // /ticket comment (ticketID) (comment)
        CommandSpec ticketComment = CommandSpec.builder()
                .description(Text.of("Open a ticket"))
                .executor(new comment(this))
                .arguments(GenericArguments.integer(Text.of("ticketID")),
                        GenericArguments.remainingJoinedStrings(Text.of("comment")))
                .permission(Permissions.COMMAND_TICKET_COMMENT)
                .build();

        // /ticket
        CommandSpec ticketBase = CommandSpec.builder()
                .description(Text.of("Ticket base command, Displays help"))
                .executor(new ticket(this))
                .child(ticketOpen, "open")
                .child(ticketRead, "read", "check")
                .child(ticketClose, "close", "complete")
                .child(ticketBan, "ban")
                .child(ticketUnban, "unban")
                .child(ticketReload, "reload")
                .child(ticketClaim, "claim")
                .child(ticketUnclaim, "unclaim")
                .child(ticketReopen, "reopen")
                .child(ticketAssign, "assign")
                .child(ticketHold, "hold")
                .child(ticketComment, "comment")
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
        HoconConfigurationLoader loader = getTicketDataLoader();
        ConfigurationNode rootNode = loader.load();

        List<TicketData> ticketList = rootNode.getNode("Tickets").getList(TypeToken.of(TicketData.class));
        this.tickets = new HashMap<Integer, TicketData>();
        this.notifications = new ArrayList<UUID>();
        for (TicketData ticket : ticketList) {
            this.tickets.put(ticket.getTicketID(), ticket);
            if (ticket.getNotified() == 0 && ticket.getStatus() == Closed) this.notifications.add(ticket.getPlayerUUID());
        }

        HoconConfigurationLoader playerloader = getPlayerDataLoader();
        ConfigurationNode playerrootNode = playerloader.load();

        List<PlayerData> playersDataList = playerrootNode.getNode("PlayersData").getList(TypeToken.of(PlayerData.class));
        this.playersData = new HashMap<UUID, PlayerData>();
        for (PlayerData pd : playersDataList) {
            this.playersData.put(pd.getPlayerUUID(), pd);
        }
    }

    synchronized public void saveData() throws IOException, ObjectMappingException {
        HoconConfigurationLoader loader = getTicketDataLoader();
        ConfigurationNode rootNode = loader.load();

        rootNode.getNode("Tickets").setValue(TicketSerializer.token, new ArrayList<TicketData>(this.tickets.values()));
        loader.save(rootNode);

        HoconConfigurationLoader playerloader = getPlayerDataLoader();
        ConfigurationNode playerrootNode = playerloader.load();

        playerrootNode.getNode("PlayersData").setValue(PlayerDataSerializer.token, new ArrayList<PlayerData>(this.playersData.values()));
        playerloader.save(playerrootNode);

    }

    public void nagTimer() {
        if(Config.nagTimer > 0){
            Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    final List<TicketData> tickets = new ArrayList<TicketData>(getTickets());
                    int openTickets = 0;
                    int heldTickets = 0;
                    for (TicketData ticket : tickets) {
                        if (ticket.getStatus() == Open || ticket.getStatus() == Claimed) {
                            openTickets++;
                        }
                        if (ticket.getStatus() == Held) {
                            heldTickets++;
                        }
                    }
                    if(Config.nagHeld) {
                        if(heldTickets > 0) {
                            if(openTickets > 0) {
                                CommonUtil.notifyOnlineStaff(Messages.getTicketUnresolvedHeld(openTickets, heldTickets, "check"));
                            }
                        } else {
                            if(openTickets > 0) {
                                CommonUtil.notifyOnlineStaff(Messages.getTicketUnresolved(openTickets, "check"));
                            }
                        }
                    } else {
                        if(openTickets > 0) {
                            CommonUtil.notifyOnlineStaff(Messages.getTicketUnresolved(openTickets, "check"));
                        }
                    }
                }
            }, Config.nagTimer, Config.nagTimer, TimeUnit.MINUTES);
        }
    }
    public HoconConfigurationLoader getTicketDataLoader() {
        return HoconConfigurationLoader.builder().setPath(this.ConfigDir.resolve("TicketData.conf")).build();
    }

    public HoconConfigurationLoader getPlayerDataLoader() {
        return HoconConfigurationLoader.builder().setPath(this.ConfigDir.resolve("PlayerData.conf")).build();
    }

    public TicketData getTicket(int ticketID) {
        return this.tickets.get(ticketID);
    }

    public Collection<TicketData> getTickets() {
        return Collections.unmodifiableCollection(this.tickets.values());
    }

    public Collection<PlayerData> getPlayerData() {
        return Collections.unmodifiableCollection(this.playersData.values());
    }

    public ArrayList<UUID> getNotifications() {
        return this.notifications;
    }

    public ArrayList<String> getWaitTimer() {
        return this.waitTimer;
    }

    public TicketData addTicket(TicketData ticket) {
        return this.tickets.put(ticket.getTicketID(), ticket);
    }

    public PlayerData addPlayerData(PlayerData pData) {
        return this.playersData.put(pData.getPlayerUUID(), pData);
    }

    public Text fromLegacy(String legacy) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(legacy);
    }

    public String fromLegacyS(String legacy) {
        return String.valueOf(TextSerializers.FORMATTING_CODE.deserializeUnchecked(legacy));
    }

    private void convertOldData() throws IOException {
        Path path = this.ConfigDir.resolve("TicketData.conf");
        if (path.toFile().exists()) {
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            if (content.contains("status=0") || content.contains("status=1") || content.contains("status=2") || content.contains("status=3")) {
                content = content.replaceAll("status=0", "status=Open");
                content = content.replaceAll("status=1", "status=Claimed");
                content = content.replaceAll("status=2", "status=Held");
                content = content.replaceAll("status=3", "status=Closed");
                Files.write(path, content.getBytes(charset));
            }
            if (content.contains("name") || content.contains("staffname")) {
                content = content.replaceAll("staffname", "staffUUID");
                content = content.replaceAll("staffUUID=\"\"", "staffUUID=\"00000000-0000-0000-0000-000000000000\"");
                content = content.replaceAll("name", "playerUUID");
                Files.write(path, content.getBytes(charset));
            }
        }
    }

    private void checkOldNames() {
        final List<TicketData> tickets = new ArrayList<TicketData>(getTickets());
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        Boolean save = false;
        for (TicketData ticket : tickets) {
            if (!ticket.getOldPlayer().matches("[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}")) {
                ticket.setPlayerUUID(userStorage.get().get(ticket.getOldPlayer()).get().getUniqueId());
                save = true;
            }
            if (!ticket.getOldStaffname().matches("[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}")) {
                ticket.setStaffUUID(userStorage.get().get(ticket.getOldStaffname()).get().getUniqueId().toString());
                save = true;
            }
        }
        if (save) {
            try {
                saveData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
