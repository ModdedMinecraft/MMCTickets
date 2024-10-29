package net.moddedminecraft.mmctickets.config;

import net.kyori.adventure.text.Component;
import net.moddedminecraft.mmctickets.Main;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import sawfowl.localeapi.LocaleAPIMain;
import sawfowl.localeapi.api.LocaleAPI;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

public class Messages {

    private static Main plugin;

    public Path defaultMessage;

    public static ConfigurationLoader<CommentedConfigurationNode> messageLoader;
    public static CommentedConfigurationNode messages;

    public Messages(Main main) throws IOException {
        plugin = main;
        String language = Config.language;
        checkLangAssetFiles();
        defaultMessage = plugin.ConfigDir.resolve("localization/messages_" + language + ".conf");
        if (Files.notExists(defaultMessage)) {
            plugin.getLogger().warn("Localization was not found");
        }

        messageLoader = HoconConfigurationLoader.builder().path(defaultMessage).build();
        messages = messageLoader.load();
        messageCheck();
    }

    private static String chatprefix = "&f[&6MMCTickets&f] ";

    //actions
    private static String claimButton = "&f[&6Claim&f]";
    private static String claimButtonHover = "&eClick here to claim this ticket.";
    private static String unclaimButton = "&f[&6Unclaim&f]";
    private static String unclaimButtonHover = "&eClick here to unclaim this ticket.";
    private static String closeButton = "&f[&6Close&f]";
    private static String closeButtonHover = "&eClick here to close this ticket.";
    private static String reopenButton = "&f[&6Reopen&f]";
    private static String reopenButtonHover = "&eClick here to reopen this ticket.";
    private static String commentButton = "&f[&6Comment&f]";
    private static String commentButtonHover = "&eClick here to put a comment on this ticket.";
    private static String holdButton = "&f[&6Hold&f]";
    private static String holdButtonHover = "&eClick here to put this ticket on hold.";
    private static String yesButton = "&f[&6Yes&f]";
    private static String yesButtonHover = "&eClick here to confirm the overwrite.";

    //errors
    private static String errorGeneral = "&cAn error occurred. {0}";
    private static String errorIncorrectUsage = "&cIncorrect Usage: {0}";
    private static String errorBanned = "&cYou are not allowed to open new ticket.";
    private static String errorBannedAlready = "&c{0} is already banned from opening tickets.";
    private static String errorBanUser = "&cCannot ban {0} from opening new ticket.";
    private static String errorUnbanUser = "&cCannot unban {0} from opening new ticket.";
    private static String errorNotBanned = "&c{0} is not banned from opening tickets.";
    private static String errorPermission = "&eYou need permission \"{0}\" to do that.";
    private static String errorTicketAlreadyClosed = "&cTicket is already closed.";
    private static String errorTicketNotClosed = "&cTicket #{0} is not closed or on hold.";
    private static String errorTicketlreadyHold = "&cTicket is already on hold.";
    private static String errorTicketOwner = "&cYou are not the owner of that ticket.";
    private static String errorTicketClaim = "&cTicket #{0} is already claimed by {1}.";
    private static String errorTicketUnclaim = "&cTicket #{0} is claimed by {1}.";
    private static String errorUserNotExist = "&cThe specified user {0} does not exist or contains invalid characters.";
    private static String errorTicketServer = "&cTicket #{0} was opened on another server.";

    //teleport
    private static String teleportToTicket = "&9Teleported to ticket #{0}.";

    //ticket
    private static String ticketAssign = "&6{0} has been assigned to ticket #{1}.";
    private static String ticketAssignUser = "&6Your ticket #{0} has been assigned to {1}.";
    private static String ticketCommentedit = "&6Ticket #{0} already has a comment attached. Do you wish to overwrite this?";
    private static String ticketComment = "&6A comment was added to ticket #{0} by {1}.";
    private static String ticketCommentUser = "&6Your comment was added to ticket #{0}.";
    private static String ticketClaim = "&6{0} is now handling ticket #{1}.";
    private static String ticketClaimUser = "&6{0} is now handling your ticket #{1}.";
    private static String ticketClose = "&6Ticket #{0} was closed by {1}.";
    private static String ticketCloseOffline = "&6A ticket has been closed while you were offline.";
    private static String ticketCloseOfflineMulti = "&6While you were gone, {0} tickets were closed. Use /{1} to see your currently open tickets.";
    private static String ticketCloseUser = "&6Your Ticket #{0} has been closed by {1}.";
    private static String ticketDuplicate = "&cYour ticket has not been opened because it was detected as a duplicate.";
    private static String ticketOpen = "&aA new ticket has been opened by {0}, id assigned #{1}.";
    private static String ticketOpenUser = "&6You opened a ticket, it has been assigned ID #{0}. A staff member should be with you soon.";
    private static String ticketTitleNotification = "A new ticket has been opened by {0}, id assigned #{1}.";
    private static String ticketOnHoverTeleportTo = "Click here to teleport to this tickets location.";
    private static String ticketReadNone = "&6There are no open tickets.";
    private static String ticketReadNoneSelf = "&6You have no open tickets.";
    private static String ticketReadNoneClosed = "&6There are no closed tickets.";
    private static String ticketReadNoneHeld = "&6There are no tickets currently on hold.";
    private static String ticketHold = "&6Ticket #{0} was put on hold by {1}";
    private static String ticketHoldUser = "&6Your ticket #{0} was put on hold by {1}";
    private static String ticketUnresolved = "&aThere are {0} open tickets. Type /{1} to see them.";
    private static String ticketUnresolvedHeld = "&aThere are {0} open tickets and {1} ticket on hold. Type /{2} to see them.";
    private static String ticketUnclaim = "&6{0} is no longer handling ticket #{1}.";
    private static String ticketUnclaimUser = "&6{0} is no longer handling your ticket #{1}.";
    private static String ticketNotExist = "&cTicket #{0} does not exist.";
    private static String ticketNotClaimed = "&cTicket #{0} is not claimed.";
    private static String ticketNotOpen = "&cThe ticket #{0} is not open.";
    private static String ticketReopen = "&6{0} has reopened ticket #{1}";
    private static String ticketReopenUser = "&6{0} has reopened your ticket #{1}";
    private static String ticketTooShort = "&cYour ticket needs to contain at least {0} words.";
    private static String ticketTooMany = "&cYou have too many open tickets, please wait before opening more.";
    private static String ticketTooFast = "&cYou need to wait {0} seconds before attempting to open another ticket.";

    //staff
    private static String staffListSeperator = "&e, ";
    private static String staffListTitle = "&6Online Staff";
    private static String staffListEmpty = "&eThere are no staff members online.";
    private static String staffListPadding = "=";

    //plugin
    private static String pluginOutdated = "&cYou are not running the latest recommended build! Recommended build is: &6{0}";

    private void messageCheck() throws IOException {
        if (!Files.exists(defaultMessage)) {
            Files.createFile(defaultMessage);
        }

        //actions
        claimButton = check(messages.node("action", "claim-button"), claimButton).getString();
        claimButtonHover = check(messages.node("action", "claim-button-hover"), claimButtonHover).getString();
        unclaimButton = check(messages.node("action", "unclaim-button"), unclaimButton).getString();
        unclaimButtonHover = check(messages.node("action", "unclaim-button-hover"), unclaimButtonHover).getString();
        closeButton = check(messages.node("action", "close-button"), closeButton).getString();
        closeButtonHover = check(messages.node("action", "close-button-hover"), closeButtonHover).getString();
        reopenButton = check(messages.node("action", "reopen-button"), reopenButton).getString();
        reopenButtonHover = check(messages.node("action", "reopen-button-hover"), reopenButtonHover).getString();
        commentButton = check(messages.node("action", "comment-button"), commentButton).getString();
        commentButtonHover = check(messages.node("action", "comment-button-hover"), commentButtonHover).getString();
        holdButton = check(messages.node("action", "hold-button"), holdButton).getString();
        holdButtonHover = check(messages.node("action", "hold-button-hover"), holdButtonHover).getString();
        yesButton = check(messages.node("action", "yes-button"), yesButton).getString();
        yesButtonHover = check(messages.node("action", "yes-button-hover"), yesButtonHover).getString();

        //errors
        errorGeneral = check(messages.node("error", "general"), errorGeneral).getString();
        errorIncorrectUsage = check(messages.node("error", "incorrect-usage"), errorIncorrectUsage).getString();
        errorBanned = check(messages.node("error", "banned"), errorBanned).getString();
        errorBannedAlready = check(messages.node("error", "banned-already"), errorBannedAlready).getString();
        errorBanUser = check(messages.node("error", "ban-user"), errorBanUser).getString();
        errorUnbanUser = check(messages.node("error", "unban-user"), errorUnbanUser).getString();
        errorNotBanned = check(messages.node("error", "not-banned"), errorNotBanned).getString();
        errorPermission = check(messages.node("error", "permission"), errorPermission).getString();
        errorTicketAlreadyClosed = check(messages.node("error", "ticket-already-closed"), errorTicketAlreadyClosed).getString();
        errorTicketNotClosed = check(messages.node("error", "ticket-not-closed"), errorTicketNotClosed).getString();
        errorTicketlreadyHold = check(messages.node("error", "ticket-already-on-hold"), errorTicketlreadyHold).getString();
        errorTicketOwner = check(messages.node("error", "ticket-owner"), errorTicketOwner).getString();
        errorTicketClaim = check(messages.node("error", "ticket-claim"), errorTicketClaim).getString();
        errorTicketUnclaim = check(messages.node("error", "ticket-unclaim"), errorTicketUnclaim).getString();
        errorUserNotExist = check(messages.node("error", "user-not-exist"), errorUserNotExist).getString();
        errorTicketServer = check(messages.node("error", "ticket-server"), errorTicketServer).getString();

        //teleport
        teleportToTicket = check(messages.node("teleport", "to-ticket"), teleportToTicket).getString();

        //tickets
        ticketAssign = check(messages.node("ticket", "assign"), ticketAssign).getString();
        ticketAssignUser = check(messages.node("ticket", "assign-user"), ticketAssignUser).getString();
        ticketCommentedit = check(messages.node("ticket", "comment-edit"), ticketCommentedit).getString();
        ticketComment = check(messages.node("ticket", "comment"), ticketComment).getString();
        ticketCommentUser = check(messages.node("ticket", "comment-user"), ticketCommentUser).getString();
        ticketClaim = check(messages.node("ticket", "claim"), ticketClaim).getString();
        ticketClaimUser = check(messages.node("ticket", "claim-user"), ticketClaimUser).getString();
        ticketClose = check(messages.node("ticket", "close"), ticketClose).getString();
        ticketCloseOffline = check(messages.node("ticket", "close-offline"), ticketCloseOffline).getString();
        ticketCloseOfflineMulti = check(messages.node("ticket", "close-offline-multi"), ticketCloseOfflineMulti).getString();
        ticketCloseUser = check(messages.node("ticket", "close-user"), ticketCloseUser).getString();
        ticketDuplicate = check(messages.node("ticket", "duplicate"), ticketDuplicate).getString();
        ticketOpen = check(messages.node("ticket", "open"), ticketOpen).getString();
        ticketOpenUser = check(messages.node("ticket", "open-user"), ticketOpenUser).getString();
        ticketTitleNotification = check(messages.node("ticket", "open-title-notification"), ticketTitleNotification).getString();
        ticketOnHoverTeleportTo = check(messages.node("ticket", "on-hover-teleport-to"), ticketOnHoverTeleportTo).getString();
        ticketReadNone = check(messages.node("ticket", "read-none"), ticketReadNone).getString();
        ticketReadNoneSelf = check(messages.node("ticket", "read-none-self"), ticketReadNoneSelf).getString();
        ticketReadNoneClosed = check(messages.node("ticket", "read-none-closed"), ticketReadNoneClosed).getString();
        ticketReadNoneHeld = check(messages.node("ticket", "read-none-held"), ticketReadNoneHeld).getString();
        ticketHold = check(messages.node("ticket", "hold"), ticketHold).getString();
        ticketHoldUser = check(messages.node("ticket", "hold-user"), ticketHoldUser).getString();
        ticketUnresolved = check(messages.node("ticket", "unresolved"), ticketUnresolved).getString();
        ticketUnresolvedHeld = check(messages.node("ticket", "unresolved-held"), ticketUnresolvedHeld).getString();
        ticketUnclaim = check(messages.node("ticket", "unclaim"), ticketUnclaim).getString();
        ticketUnclaimUser = check(messages.node("ticket", "unclaim-user"), ticketUnclaimUser).getString();
        ticketNotExist = check(messages.node("ticket", "not-exist"), ticketNotExist).getString();
        ticketNotClaimed = check(messages.node("ticket", "not-claimed"), ticketNotClaimed).getString();
        ticketNotOpen = check(messages.node("ticket", "not-open"), ticketNotOpen).getString();
        ticketReopen = check(messages.node("ticket", "reopen"), ticketReopen).getString();
        ticketReopenUser = check(messages.node("ticket", "reopen-user"), ticketReopenUser).getString();
        ticketTooShort = check(messages.node("ticket", "too-short"), ticketTooShort).getString();
        ticketTooMany = check(messages.node("ticket", "too-many"), ticketTooMany).getString();
        ticketTooFast = check(messages.node("ticket", "too-fast"), ticketTooFast).getString();

        //staff
        staffListSeperator = check(messages.node("staff", "list-separator"), staffListSeperator).getString();
        staffListTitle = check(messages.node("staff", "list-title"), staffListTitle).getString();
        staffListEmpty = check(messages.node("staff", "list-empty"), staffListEmpty).getString();
        staffListPadding = check(messages.node("staff", "list-padding"), staffListPadding).getString();

        //plugin
        pluginOutdated = check(messages.node("plugin", "outdated"), pluginOutdated).getString();

        messageLoader.save(messages);

    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue) throws SerializationException {
        if (node.virtual()) {
            node.set(defaultValue);
        }
        return node;
    }

    private void checkLangAssetFiles() throws IOException {

        if (!Files.isDirectory(plugin.ConfigDir.resolve("localization"))) {
            Files.createDirectory(plugin.ConfigDir.resolve("localization"));
        }
        String[] assets = {
                "messages_EN.conf",
                "messages_DE.conf",
                "messages_IT.conf",
                "messages_RU.conf"
        };

        for (String asset : assets) {
            if (!Files.exists(plugin.ConfigDir.resolve("localization/" +asset))) {
                InputStream stream = Main.class.getClassLoader().getResourceAsStream(asset);
                if (stream != null) {
                    Files.copy(stream, plugin.ConfigDir.resolve("localization/" +asset));
                }
                //Sponge.getAssetManager().getAsset(plugin, asset).get()
                        //.copyToFile(plugin.ConfigDir.resolve("localization/" +asset));

            }
        }
    }

    private static Component parse(String key, Object ... params){
        return plugin.fromLegacy(MessageFormat.format(key, params));
    }

    public static Component getChatprefix() {
        return parse(chatprefix);
    }

    public static Component getErrorBanned() {
        return parse(errorBanned);
    }

    public static Component getErrorBannedAlready(String playerName) {
        return parse(errorBannedAlready, playerName);
    }

    public static Component getErrorBanUser(String playerName) {
        return parse(errorBanUser, playerName);
    }

    public static Component getErrorGen(String text) {
        return parse(errorGeneral, text);
    }

    public static Component getErrorIncorrectUsage(String text) {
        return parse(errorIncorrectUsage, text);
    }

    public static Component getErrorNotBanned(String playerName) {
        return parse(errorNotBanned, playerName);
    }

    public static Component getErrorPermission(String permission) {
        return parse(errorPermission, permission);
    }

    public static Component getErrorTicketAlreadyClosed() {
        return parse(errorTicketAlreadyClosed);
    }

    public static Component getErrorTicketlreadyHold() {
        return parse(errorTicketlreadyHold);
    }

    public static Component getErrorTicketClaim(int ticketID, String staffName) {
        return parse(errorTicketClaim, ticketID, staffName);
    }

    public static Component getErrorTicketNotClosed(int ticketID) {
        return parse(errorTicketNotClosed, ticketID);
    }

    public static Component getErrorTicketOwner() {
        return parse(errorTicketOwner);
    }

    public static Component getErrorTicketUnclaim(int ticketID, String staffName) {
        return parse(errorTicketUnclaim, ticketID, staffName);
    }

    public static Component getErrorUnbanUser(String playerName) {
        return parse(errorUnbanUser, playerName);
    }

    public static Component getErrorUserNotExist(String playerName) {
        return parse(errorUserNotExist, playerName);
    }

    public static String getStaffListSeperator() {
        return staffListSeperator;
    }

    public static Component getStaffListTitle() {
        return parse(staffListTitle);
    }

    public static Component getStaffListEmpty() {
        return parse(staffListEmpty);
    }

    public static Component getTeleportToTicket(int ticketID) {
        return parse(teleportToTicket, ticketID);
    }

    public static Component getTicketAssign(String staffName, int ticketID) {
        return parse(ticketAssign, staffName, ticketID);
    }

    public static Component getTicketAssignUser(int ticketID, String staffName) {
        return parse(ticketAssignUser, ticketID, staffName);
    }

    public static Component getPluginOutdated(String version) {
        return parse(pluginOutdated, version);
    }

    public static Component getStaffListPadding() {
        return parse(staffListPadding);
    }

    public static Component getTicketClaim(String staffName, int ticketID) {
        return parse(ticketClaim, staffName, ticketID);
    }

    public static Component getTicketClaimUser(String staffName, int ticketID) {
        return parse(ticketClaimUser, staffName, ticketID);
    }

    public static Component getTicketClose(int ticketID, String staffName) {
        return parse(ticketClose, ticketID, staffName);
    }

    public static Component getTicketCloseOffline() {
        return parse(ticketCloseOffline);
    }

    public static Component getTicketCloseOfflineMulti(int ticketsNum, String command) {
        return parse(ticketCloseOfflineMulti, ticketsNum, command);
    }

    public static Component getTicketCloseUser(int ticketID, String staffName) {
        return parse(ticketCloseUser, ticketID, staffName);
    }

    public static Component getTicketDuplicate() {
        return parse(ticketDuplicate);
    }

    public static Component getTicketHold(int ticketID, String staffName) {
        return parse(ticketHold, ticketID, staffName);
    }

    public static Component getTicketOnHoverTeleportTo() {
        return parse(ticketOnHoverTeleportTo);
    }

    public static Component getTicketHoldUser(int ticketID, String staffName) {
        return parse(ticketHoldUser, ticketID, staffName);
    }

    public static Component getTicketNotClaimed(int ticketID) {
        return parse(ticketNotClaimed, ticketID);
    }

    public static Component getTicketNotExist(int ticketID) {
        return parse(ticketNotExist, ticketID);
    }

    public static Component getTicketOpen(String playerName, int ticketID) {
        return parse(ticketOpen, playerName, ticketID);
    }

    public static Component getTicketOpenUser(int ticketID) {
        return parse(ticketOpenUser, ticketID);
    }

    public static Component getTicketReadNone() {
        return parse(ticketReadNone);
    }

    public static Component getTicketNotOpen(int ticketID) {
        return parse(ticketNotOpen, ticketID);
    }

    public static Component getTicketReadNoneClosed() {
        return parse(ticketReadNoneClosed);
    }

    public static Component getTicketReadNoneHeld() {
        return parse(ticketReadNoneHeld);
    }

    public static Component getTicketReadNoneSelf() {
        return parse(ticketReadNoneSelf);
    }

    public static Component getTicketTitleNotification(String playerName, int ticketID) {
        return parse(ticketTitleNotification, playerName, ticketID);
    }

    public static Component getTicketReopen(String staffName, int ticketID) {
        return parse(ticketReopen, staffName, ticketID);
    }

    public static Component getTicketReopenUser(String staffName, int ticketID) {
        return parse(ticketReopenUser, staffName, ticketID);
    }

    public static Component getTicketTooFast(int seconds) {
        return parse(ticketTooFast, seconds);
    }

    public static Component getTicketTooMany() {
        return parse(ticketTooMany);
    }

    public static Component getTicketTooShort(int minWords) {
        return parse(ticketTooShort, minWords);
    }

    public static Component getTicketUnclaim(String staffName, int ticketID) {
        return parse(ticketUnclaim, staffName, ticketID);
    }

    public static Component getTicketUnclaimUser(String staffName, int ticketID) {
        return parse(ticketUnclaimUser, staffName, ticketID);
    }

    public static Component getTicketUnresolved(int totalTickets, String command) {
        return parse(ticketUnresolved, totalTickets, command);
    }

    public static Component getTicketUnresolvedHeld(int totalOpenTickets, int totalHeldTickets, String command) {
        return parse(ticketUnresolvedHeld, totalOpenTickets, totalHeldTickets, command);
    }

    public static Component getTicketComment(int ticketID, String staffName) {
        return parse(ticketComment, ticketID, staffName);
    }

    public static Component getTicketCommentUser(int ticketID) {
        return parse(ticketCommentUser, ticketID);
    }

    public static String getClaimButton() {
        return claimButton;
    }

    public static String getClaimButtonHover() {
        return claimButtonHover;
    }

    public static String getCloseButton() {
        return closeButton;
    }

    public static String getCloseButtonHover() {
        return closeButtonHover;
    }

    public static String getCommentButton() {
        return commentButton;
    }

    public static String getCommentButtonHover() {
        return commentButtonHover;
    }

    public static String getReopenButton() {
        return reopenButton;
    }

    public static String getReopenButtonHover() {
        return reopenButtonHover;
    }

    public static String getUnclaimButton() {
        return unclaimButton;
    }

    public static String getUnclaimButtonHover() {
        return unclaimButtonHover;
    }

    public static String getHoldButton() {
        return holdButton;
    }

    public static String getHoldButtonHover() {
        return holdButtonHover;
    }

    public static String getYesButton() {
        return yesButton;
    }

    public static Component getTicketCommentedit(int ticketID) {
        return parse(ticketCommentedit, ticketID);
    }

    public static String getYesButtonHover() {
        return yesButtonHover;
    }

    public static Component getErrorTicketServer(int ticketID) {
        return parse (errorTicketServer, ticketID);
    }
}
