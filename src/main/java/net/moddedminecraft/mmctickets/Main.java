package net.moddedminecraft.mmctickets;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
//import net.moddedminecraft.mmctickets.util.UpdateChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.moddedminecraft.mmctickets.data.ticketStatus.*;

@Plugin("mmctickets")
public class Main {

    public static final Logger logger = LogManager.getLogger("MMCTickets");

    @Inject
    @DefaultConfig(sharedRoot = false)
    public Path defaultConf;

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path ConfigDir;

    public Config config;
    public Messages messages;

    private ArrayList<String> waitTimer;
    private DataStoreManager dataStoreManager;

    //public UpdateChecker updatechecker;

    public final PluginContainer container;

    @Inject
    public Main(final PluginContainer container) {
        this.container = container;
    }

    @Listener
    public void onServerAboutStart(ConstructPluginEvent event) throws IOException {
        Sponge.eventManager().registerListeners(container, new EventListener(this));
        TypeSerializerCollection.builder().register(TicketData.class, new TicketSerializer());
        TypeSerializerCollection.builder().register(PlayerData.class, new PlayerDataSerializer());

        config = new Config(this);
        messages = new Messages(this);
        //loadCommands();
    }

        @Listener
    public void onServerStart(StartedEngineEvent<Server> event) {
            dataStoreManager = new DataStoreManager(this);
            loadDataStore();

            for (String s : Arrays.asList(
                    "MMCTickets Loaded",
                    "Tickets loaded: " + getDataStore().getTicketData().size(),
                    "Notifications loaded: " + getDataStore().getNotifications().size(),
                    "PlayerData loaded: " + getDataStore().getPlayerData().size())
            ) {
                getLogger().info(s);
            }

            this.waitTimer = new ArrayList<String>();

            //TODO Fix update checker
            //updatechecker = new UpdateChecker(this, Sponge.pluginManager().plugin("mmctickets").get().metadata().version().toString());
            //updatechecker.startUpdateCheck();

            //start ticket nag timer
            nagTimer();
    }

    @Listener
    public void onPluginReload(RefreshGameEvent event) throws IOException {
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

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        // /ticket staff
        Command.Parameterized staffCMD = Command.builder()
                .shortDescription(Component.text("Display list of staff online"))
                .executor(new staff(this))
                .build();

        // /ticket read self
        Command.Parameterized readSelfCMD = Command.builder()
                .shortDescription(Component.text("Display a list of all tickets the player owns"))
                .executor(new readSelf(this))
                .build();

        // /ticket read closed
        Command.Parameterized readClosedCMD = Command.builder()
                .shortDescription(Component.text("Display a list of all closed tickets"))
                .executor(new readClosed(this))
                .permission(Permissions.COMMAND_TICKET_READ_ALL)
                .build();

        // /ticket read held
        Command.Parameterized readHeldCMD = Command.builder()
                .shortDescription(Component.text("Display a list of all held tickets"))
                .executor(new readHeld(this))
                .permission(Permissions.COMMAND_TICKET_READ_ALL)
                .build();

        // /ticket read (ticketID)
        Command.Parameterized ticketReadCMD = Command.builder()
                .shortDescription(Component.text("Read all ticket or give more detail of a specific ticket"))
                .executor(new read(this))
                .addChild(readClosedCMD, "closed")
                .addChild(readHeldCMD, "held")
                .addChild(readSelfCMD, "self")
                .addParameters(Parameter.integerNumber().key("ticketID").optional().build())
                .build();

        // /ticket close [ticketID] (comment)
        Command.Parameterized ticketCloseCMD = Command.builder()
                .shortDescription(Component.text("Close a ticket"))
                .executor(new close(this))
                .addParameters(Parameter.integerNumber().key("ticketID").build(),
                        Parameter.remainingJoinedStrings().key("comment").optional().build())
                .build();

        // /ticket open [Message]
        Command.Parameterized ticketOpenCMD = Command.builder()
                .shortDescription(Component.text("Open a ticket"))
                .executor(new open(this))
                .addParameters(Parameter.remainingJoinedStrings().key("message").build())
                .permission(Permissions.COMMAND_TICKET_OPEN)
                .build();

        // /ticket ban [username]
        Command.Parameterized ticketBanCMD = Command.builder()
                .shortDescription(Component.text("Ban a player from being able to create new tickets"))
                .executor(new ban(this))
                .addParameters(Parameter.player().key("playername").build())
                .permission(Permissions.COMMAND_TICKET_BAN)
                .build();

        // /ticket unban [username]
        Command.Parameterized ticketUnbanCMD = Command.builder()
                .shortDescription(Component.text("Unban a player from being able to create new tickets"))
                .executor(new unban(this))
                .addParameters(Parameter.player().key("playername").build())
                .permission(Permissions.COMMAND_TICKET_BAN)
                .build();

        // /ticket reload
        Command.Parameterized ticketReloadCMD = Command.builder()
                .shortDescription(Component.text("Reload ticket and player data."))
                .executor(new reload(this))
                .permission(Permissions.COMMAND_RELOAD)
                .build();

        // /ticket claim [ticketID]
        Command.Parameterized ticketClaimCMD = Command.builder()
                .shortDescription(Component.text("Claim a ticket"))
                .executor(new claim(this))
                .addParameters(Parameter.integerNumber().key("ticketID").build())
                .permission(Permissions.COMMAND_TICKET_CLAIM)
                .build();

        // /ticket unclaim [ticketID]
        Command.Parameterized ticketUnclaimCMD = Command.builder()
                .shortDescription(Component.text("Unclaim a ticket"))
                .executor(new unclaim(this))
                .addParameters(Parameter.integerNumber().key("ticketID").build())
                .permission(Permissions.COMMAND_TICKET_UNCLAIM)
                .build();

        // /ticket reopen [ticketID]
        Command.Parameterized ticketReopenCMD = Command.builder()
                .shortDescription(Component.text("Reopen a ticket"))
                .executor(new reopen(this))
                .addParameters(Parameter.integerNumber().key("ticketID").build())
                .permission(Permissions.COMMAND_TICKET_REOPEN)
                .build();

        // /ticket assign [ticketID] [player]
        Command.Parameterized ticketAssignCMD = Command.builder()
                .shortDescription(Component.text("Unclaim a ticket"))
                .executor(new assign(this))
                .addParameters(Parameter.integerNumber().key("ticketID").build(),
                        Parameter.player().key("playername").build())
                .permission(Permissions.COMMAND_TICKET_ASSIGN)
                .build();

        // /ticket hold [ticketID]
        Command.Parameterized ticketHoldCMD = Command.builder()
                .shortDescription(Component.text("Put a ticket on hold"))
                .executor(new hold(this))
                .addParameters(Parameter.integerNumber().key("ticketID").build())
                .permission(Permissions.COMMAND_TICKET_HOLD)
                .build();

        // /ticket comment [ticketID] [comment]
        Command.Parameterized ticketCommentCMD = Command.builder()
                .shortDescription(Component.text("Open a ticket"))
                .executor(new comment(this))
                .addParameters(Parameter.integerNumber().key("ticketID").build(),
                        Parameter.remainingJoinedStrings().key("comment").build())
                .permission(Permissions.COMMAND_TICKET_COMMENT)
                .build();

        // /ticket teleport [ticketID]
        Command.Parameterized ticketTeleportCMD = Command.builder()
                .shortDescription(Component.text("Teleport to a ticket"))
                .executor(new teleport(this))
                .addParameters(Parameter.integerNumber().key("ticketID").build())
                .permission(Permissions.COMMAND_TICKET_TELEPORT)
                .build();
        
        // /ticket
        event.register(this.container,
                Command.builder()
                        .shortDescription(Component.text("Ticket base command, Displays help"))
                        .executor(new ticket(this))
                        .addChild(staffCMD, "staff")
                        .addChild(ticketBanCMD, "ban")
                        .addChild(ticketOpenCMD, "open")
                        .addChild(ticketReadCMD, "read", "check")
                        .addChild(ticketCloseCMD, "close", "complete")
                        .addChild(ticketUnbanCMD, "unban")
                        .addChild(ticketReloadCMD, "reload")
                        .addChild(ticketClaimCMD, "claim")
                        .addChild(ticketUnclaimCMD, "unclaim")
                        .addChild(ticketHoldCMD, "hold")
                        .addChild(ticketCommentCMD, "comment")
                        .addChild(ticketTeleportCMD, "teleport", "tp")
                        .addChild(ticketAssignCMD, "assign")
                        .addChild(ticketReopenCMD, "reopen")
                        .build(), "ticket"
        );

        // /stafflist
        event.register(this.container,
                Command.builder()
                        .shortDescription(Component.text("Display list of staff online"))
                        .executor(new staff(this))
                        .build(), "stafflist"
        );
        // /modreq [Message]
        event.register(this.container,Command.builder()
                .shortDescription(Component.text("Open a ticket"))
                .executor(new open(this))
                .addParameters(Parameter.remainingJoinedStrings().key("message").build())
                .permission(Permissions.COMMAND_TICKET_OPEN)
                .build(), "modreq"
        );

        // /check (ticketID)
        event.register(this.container,Command.builder()
                .shortDescription(Component.text("Read all ticket or give more detail of a specific ticket"))
                .executor(new read(this))
                .addChild(readClosedCMD, "closed")
                .addChild(readHeldCMD, "held")
                .addChild(readSelfCMD, "self")
                .addParameters(Parameter.integerNumber().key("ticketID").optional().build())
                .build(), "check"
        );
    }

    public org.apache.logging.log4j.Logger getLogger() {
        return logger;
    }

    public IDataStore getDataStore() {
        return dataStoreManager.getDataStore();
    }

    public void nagTimer() {
        if(Config.nagTimer > 0){
            Sponge.asyncScheduler().executor(container).scheduleWithFixedDelay(new Runnable() {
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

    public Component fromLegacy(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
    }

    public Component clearDecoration(String decoration) {
        return decoration == null ? Component.empty() : LegacyComponentSerializer.legacyAmpersand().deserialize(decoration);
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
