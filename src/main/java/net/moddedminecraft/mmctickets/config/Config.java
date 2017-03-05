package net.moddedminecraft.mmctickets.config;

import net.moddedminecraft.mmctickets.Main;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;

public class Config {

    private final Main plugin;

    public static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode config;

    public Config(Main main) throws IOException, ObjectMappingException {
        plugin = main;
        loader = HoconConfigurationLoader.builder().setPath(plugin.defaultConf).build();
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



    private void configCheck() throws IOException, ObjectMappingException {
        if (!plugin.defaultConf.toFile().exists()) {
            plugin.defaultConf.toFile().createNewFile();
        }

        // notifications
        soundNotification = check(config.getNode("notifications", "sound"), true, "If true, a notification sound will be played when requests are created.").getBoolean();
        staffNotification = check(config.getNode("notifications", "staff"), true, "Notifies staff members when a new request is filed.").getBoolean();
        titleNotification = check(config.getNode("notifications", "title"), false, "Notifies staff members with a title message in the centre of the screen.").getBoolean();

        // ticket
        minWords = check(config.getNode("ticket", "user", "minimum-words"), 3).getInt();
        delayTimer = check(config.getNode("ticket", "user", "delay"), 60, "User has to wait this amount of seconds before opening another ticket.").getInt();
        maxTickets = check(config.getNode("ticket", "user", "max-tickets"), 5, "Maximum number of TicketData a user may have open at the same time.").getInt();
        preventDuplicates = check(config.getNode("ticket", "user", "prevent-duplicates"), true, "Prevent duplicate TicketData by the same user.").getBoolean();

        hideOffline = check(config.getNode("ticket", "user", "hide-offline"), false, "If set to true, hides all TicketData in /ticket read from offline users.").getBoolean();
        ticketsPerPage = check(config.getNode("ticket", "user", "tickets-per-page"), 5, "This sets the total amount of TicketData that should be shown on each page.").getInt();
        nagTimer = check(config.getNode("ticket", "user", "nag"), 5, "If above 0 (minutes), nag the online staff members about open TicketData.").getInt();
        nagHeld = check(config.getNode("ticket", "user", "nag-held"), true, "If true, the nag feature will mention TicketData on hold. ").getBoolean();

        checkForUpdate = check(config.getNode("update", "check"), true, "If true, will notify at startup and if a player with "+Permissions.STAFF+" logs in, if there is an update available.").getBoolean();

        loader.save(config);

    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue, String comment) {
        if (node.isVirtual()) {
            node.setValue(defaultValue).setComment(comment);
        }
        return node;
    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue) {
        if (node.isVirtual()) {
            node.setValue(defaultValue);
        }
        return node;
    }
}
