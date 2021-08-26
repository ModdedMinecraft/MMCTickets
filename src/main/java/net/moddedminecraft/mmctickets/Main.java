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
import net.moddedminecraft.mmctickets.database.DataStoreManager;
import net.moddedminecraft.mmctickets.database.IDataStore;
import net.moddedminecraft.mmctickets.util.CommonUtil;
import net.moddedminecraft.mmctickets.util.UpdateChecker;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
//import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.moddedminecraft.mmctickets.data.ticketStatus.*;

@Plugin(id = "mmctickets", name = "MMCTickets", version = "2.0.7", description = "A real time ticket system")
public class Main {

    @Inject
    private Logger logger;

    /*@Inject
    private Metrics2 metrics;*/

    @Inject
    @DefaultConfig(sharedRoot = false)
    public Path defaultConf;

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path ConfigDir;

    public Config config;
    public Messages messages;

    private CommandManager cmdManager = Sponge.getCommandManager();

    private ArrayList<String> waitTimer;
    private DataStoreManager dataStoreManager;

    public UpdateChecker updatechecker;

    @Listener
    public void Init(GameInitializationEvent event) throws IOException, ObjectMappingException {
        Sponge.getEventManager().registerListeners(this, new EventListener(this));

        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TicketData.class), new TicketSerializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(PlayerData.class), new PlayerDataSerializer());

        config = new Config(this);
        messages = new Messages(this);
        loadCommands();
    }

    @Listener
    public void onServerAboutStart(GameAboutToStartServerEvent event) {
        dataStoreManager = new DataStoreManager(this);
        if (dataStoreManager.load()) {
            getLogger().info("MMCTickets datastore Loaded");
        } else {
            getLogger().error("Unable to load a datastore please check your Console/Config!");
        }
    }

        @Listener
    public void onServerStart(GameStartedServerEvent event) {
            getLogger().info("MMCTickets Loaded");
            getLogger().info("Tickets loaded: " + getDataStore().getTicketData().size());
            getLogger().info("Notifications loaded: " + getDataStore().getNotifications().size());
            getLogger().info("PlayerData loaded: " + getDataStore().getPlayerData().size());

            this.waitTimer = new ArrayList<String>();

            updatechecker = new UpdateChecker(this, Sponge.getPluginManager().getPlugin("mmctickets").get().getVersion().get());
            updatechecker.startUpdateCheck();

            //start ticket nag timer
            nagTimer();
    }

    @Listener
    public void onPluginReload(GameReloadEvent event) throws IOException, ObjectMappingException {
        this.config = new Config(this);
        this.messages = new Messages(this);
        dataStoreManager = new DataStoreManager(this);
        loadDataStore();
    }

    public void loadDataStore() {
        if (dataStoreManager.load()) {
            getLogger().info("MMCTickets datastore Loaded");
        } else {
            getLogger().error("Unable to load a datastore please check your Console/Config!");
        }
    }

    public void setDataStoreManager(DataStoreManager dataStoreManager) {
        this.dataStoreManager = dataStoreManager;
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
                .arguments(GenericArguments.user(Text.of("playername")))
                .permission(Permissions.COMMAND_TICKET_BAN)
                .build();

        // /ticket unban (username)
        CommandSpec ticketUnban = CommandSpec.builder()
                .description(Text.of("Unban a player from being able to create new tickets"))
                .executor(new unban(this))
                .arguments(GenericArguments.user(Text.of("playername")))
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
                        GenericArguments.user(Text.of("player")))
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

        // /ticket teleport (ticketID)
        CommandSpec ticketTeleport = CommandSpec.builder()
                .description(Text.of("Teleport to a ticket"))
                .executor(new teleport(this))
                .arguments(GenericArguments.integer(Text.of("ticketID")))
                .permission(Permissions.COMMAND_TICKET_TELEPORT)
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
                .child(ticketTeleport, "teleport", "tp")
                .build();

        cmdManager.register(this, ticketOpen, "modreq");
        cmdManager.register(this, ticketRead, "check");
        cmdManager.register(this, ticketBase, "ticket");
        cmdManager.register(this, staffList, "stafflist");
    }

    public Logger getLogger() {
        return logger;
    }

    public IDataStore getDataStore() {
        return dataStoreManager.getDataStore();
    }

    public void nagTimer() {
        if(Config.nagTimer > 0){
            Sponge.getScheduler().createSyncExecutor(this).scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    final List<TicketData> tickets = new ArrayList<TicketData>(getDataStore().getTicketData());
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

    public ArrayList<String> getWaitTimer() {
        return this.waitTimer;
    }

    public Text fromLegacy(String legacy) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(legacy);
    }

    @Deprecated
    public List<TicketData> getTickets() {
        return getDataStore().getTicketData();
    }

    @Deprecated
    public TicketData getTicket(int ticketID) {
        if (getDataStore().getTicket(ticketID).isPresent()) {
            return getDataStore().getTicket(ticketID).get();
        }
        return null;
    }

}
