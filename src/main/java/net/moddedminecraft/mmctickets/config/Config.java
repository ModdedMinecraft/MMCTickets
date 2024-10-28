package net.moddedminecraft.mmctickets.config;

import net.moddedminecraft.mmctickets.Main;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;

public class Config {

    private final Main plugin;

    public static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode config;

    public Config(Main main) throws IOException {
        plugin = main;
        loader = HoconConfigurationLoader.builder().path(plugin.defaultConf).build();
        config = loader.load();
        configCheck();
    }

    public static Boolean soundNotification;
    public static Boolean staffNotification;
    public static Boolean titleNotification;

    public static int minWords;
    public static int delayTimer;
    public static int maxTickets;
    public static Boolean preventDuplicates;

    public static Boolean hideOffline;
    public static int ticketsPerPage;
    public static int nagTimer;
    public static Boolean nagHeld;

    public static Boolean checkForUpdate;

    public static String language;

    //Database
    public static String storageEngine;
    public static String databaseFile;
    public static String h2Prefix;
    public static String mysqlHost;
    public static int mysqlPort;
    public static String mysqlDatabase;
    public static String mysqlUser;
    public static String mysqlPass;
    public static String mysqlPrefix;
    public static String server;



    private void configCheck() throws IOException {
        if (!plugin.defaultConf.toFile().exists()) {
            plugin.defaultConf.toFile().createNewFile();
        }

        //server
        server = check(config.node("server"), "", "Required, Name of the server. Used for ticket's originating server identification").getString();

        //locale
        language = check(config.node("language"), "EN", "Localization to be used, All available translations are in the 'localization' folder").getString();

        // notifications
        soundNotification = check(config.node("notifications", "sound"), true, "If true, a notification sound will be played when requests are created.").getBoolean();
        staffNotification = check(config.node("notifications", "staff"), true, "Notifies staff members when a new request is filed.").getBoolean();
        titleNotification = check(config.node("notifications", "title"), false, "Notifies staff members with a title message in the centre of the screen.").getBoolean();

        // ticket
        minWords = check(config.node("ticket", "user", "minimum-words"), 3).getInt();
        delayTimer = check(config.node("ticket", "user", "delay"), 60, "User has to wait this amount of seconds before opening another ticket.").getInt();
        maxTickets = check(config.node("ticket", "user", "max-tickets"), 5, "Maximum number of tickets a user may have open at the same time.").getInt();
        preventDuplicates = check(config.node("ticket", "user", "prevent-duplicates"), true, "Prevent duplicate tickets by the same user.").getBoolean();

        hideOffline = check(config.node("ticket", "user", "hide-offline"), false, "If set to true, hides all tickets in /ticket read from offline users.").getBoolean();
        ticketsPerPage = check(config.node("ticket", "user", "tickets-per-page"), 5, "This sets the total amount of tickets that should be shown on each page.").getInt();
        nagTimer = check(config.node("ticket", "user", "nag"), 5, "If above 0 (minutes), nag the online staff members about open tickets.").getInt();
        nagHeld = check(config.node("ticket", "user", "nag-held"), true, "If true, the nag feature will mention tickets on hold. ").getBoolean();

        checkForUpdate = check(config.node("update", "check"), true, "If true, will notify at startup and if a player with \""+Permissions.STAFF+"\" logs in, if there is an update available.").getBoolean();

        //Database
        storageEngine = check(config.node("storage", "storage-engine"), "h2", "The stoage engine that should be used, Allowed values: h2 or mysql").getString();
        databaseFile = check(config.node("storage", "h2", "database-file"), "Database.db", "Where the databaseFile will be stored. Can be a relative or absolute path. An absolute path is recommended when using this to synchronize over several servers").getString();
        h2Prefix = check(config.node("storage", "h2", "prefix"), "mmctickets_", "Prefix for the plugin tables").getString();
        mysqlHost = check(config.node("storage", "mysql", "host"), "localhost", "Host of the MySQL Server").getString();
        mysqlPort = check(config.node("storage", "mysql", "port"), "3306", "Port of the MySQL server. Default: 3306").getInt();
        mysqlDatabase = check(config.node("storage", "mysql", "database"), "mmctickets", "The database to store in").getString();
        mysqlUser = check(config.node("storage", "mysql", "user"), "root", "The user for the database").getString();
        mysqlPass = check(config.node("storage", "mysql", "password"), "pass", "Password for that user").getString();
        mysqlPrefix = check(config.node("storage", "mysql", "table-prefix"), "mmctickets_", "Prefix for the plugin tables").getString();

        loader.save(config);

    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue, String comment) throws SerializationException {
        if (node.virtual()) {
            node.set(defaultValue).comment(comment);
        }
        return node;
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue) throws SerializationException {
        if (node.virtual()) {
            node.set(defaultValue);
        }
        return node;
    }
}
