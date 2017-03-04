package net.moddedminecraft.mmctickets.config;

import net.moddedminecraft.mmctickets.Main;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

public class Messages {

    private static Main plugin;

    public Path defaultMessage;

    public static ConfigurationLoader<CommentedConfigurationNode> messageLoader;
    public static CommentedConfigurationNode messages;

    public Messages(Main main) throws IOException, ObjectMappingException {
        plugin = main;
        defaultMessage = plugin.ConfigDir.resolve("messages.conf");
        messageLoader = HoconConfigurationLoader.builder().setPath(defaultMessage).build();
        messages = messageLoader.load();
        messageCheck();
    }

    public static String chatprefix = "&f[&6MMCTickets&f] ";

    //errors
    public static String errorGeneral = "&cAn error occurred. {0}";
    public static String errorIncorrectUsage = "&cIncorrect Usage: {0}";
    public static String errorBanned = "&cYou are not allowed to open new ticket.";
    public static String errorBannedAlready = "&c{0} is already banned from opening tickets.";
    public static String errorBanUser = "&cCannot ban {0} from opening new ticket.";
    public static String errorUnbanUser = "&cCannot unban {0} from opening new ticket.";
    public static String errorNotBanned = "&c{0} is not banned from opening tickets.";
    public static String errorPermission = "&eYou need permission \"{0}\" to do that.";
    public static String errorTicketStatus = "&cUnable to set ticket status. Check that the status of the ticket does not collide.";
    public static String errorTicketAlreadyClosed = "&cTicket is already closed.";
    public static String errorTicketNotClosed = "&cTicket #{0} is not closed or on hold.";
    public static String errorTicketNan = "&cTicket ID must be a number, provided: &e{0}";
    public static String errorTicketOwner = "&cYou are not the owner of that ticket.";
    public static String errorTicketClaim = "&cTicket #{0} is already claimed by {1}.";
    public static String errorUserNotExist = "&cThe specified user {0} does not exist or contains invalid characters.";
    public static String errorUserNotSpecified = "&cPlease specify a player.";

    //teleport
    public static String teleportToTicket = "&9Teleported to ticket #{0}.";

    //TicketData
    public static String ticketAssign = "&6{0} has been assigned to ticket #{1}.";
    public static String ticketAssignUser = "&6Your ticket has been assigned to {0}.";
    public static String ticketComment = "&6A comment was added to ticket #{0} by {1}.";
    public static String ticketCommentUser = "&6Your comment was added to ticket #{0}.";
    public static String ticketCommentText = "&6{0}&e: &a{1}";
    public static String ticketClaim = "&6{0} is now handling ticket #{1}.";
    public static String ticketClaimUser = "&6{0} is now handling your ticket.";
    public static String ticketClose = "&6Ticket #{0} was closed by {1}.";
    public static String ticketCloseOffline = "&6A ticket has been closed while you were offline.";
    public static String ticketCloseOfflineMulti = "&6While you were gone, {0} tickets were closed. Use /{1} to see your currently open tickets.";
    public static String ticketCloseText = "&6Ticket text: &e{0}";
    public static String ticketCloseUser = "&6Ticket #{0} has been closed by {1}.";
    public static String ticketDuplicate = "&cYour ticket has not been opened because it was detected as a duplicate.";
    public static String ticketOpen = "&aA new ticket has been opened by {0}, id assigned #{1}.";
    public static String ticketOpenUser = "&6You opened a ticket, it has been assigned ID #{0}. A staff member should be with you soon.";
    public static String ticketTitleNotification = "A new ticket has been opened by {0}, id assigned #{1}.";
    public static String ticketOnHoverTeleportTo = "Click here to teleport to this tickets location.";
    public static String ticketReadNone = "&6There are no open tickets.";
    public static String ticketReadNoneSelf = "&6You have no open tickets.";
    public static String ticketReadNoneClosed = "&6There are no closed tickets.";
    public static String ticketReadNoneHeld = "&6There are no tickets currently on hold.";
    public static String ticketHold = "&6Ticket #{0} was put on hold by {1}";
    public static String ticketHoldUser = "&6Your ticket #{0} was put on hold by {1}";
    public static String ticketHoldText = "&6Ticket text: &e{0} &6Reason: &e{1}";
    public static String ticketUnresolved = "&aThere are {0} open tickets. Type /{1} to see them.";
    public static String ticketUnresolvedHeld = "&aThere are {0} open tickets and {1} ticket on hold. Type /{2} to see them.";
    public static String ticketUnclaim = "&6{0} is no longer handling ticket #{1}.";
    public static String ticketUnclaimUser = "&6{0} is no longer handling your ticket #{1}.";
    public static String ticketNotExist = "&cTicket #{0} does not exist.";
    public static String ticketNotClaimed = "&cTicket #{0} is not claimed.";
    public static String ticketNotOpen = "&cThe ticket #{0} is not open.";
    public static String ticketReopen = "&6{0} has reopened ticket #{1}";
    public static String ticketTooShort = "&cYour ticket needs to contain at least {0} words.";
    public static String ticketTooMany = "&cYou have too many open tickets, please wait before opening more.";
    public static String ticketTooFast = "&cYou need to wait {0} seconds before attempting to open another ticket.";

    //staff
    public static String staffListSeperator = "&e, ";
    public static String staffListTitle = "&6Online Staff";
    public static String staffListEmpty = "&eThere are no staff members online.";
    public static String staffListPadding = "=";

    private void messageCheck() throws IOException, ObjectMappingException {
        if (!Files.exists(defaultMessage)) {
            Files.createFile(defaultMessage);
        }

        //errors
        errorGeneral = check(messages.getNode("error", "general"), errorGeneral).getString();
        errorIncorrectUsage = check(messages.getNode("error", "general"), errorIncorrectUsage).getString();
        errorBanned = check(messages.getNode("error", "banned"), errorBanned).getString();
        errorBannedAlready = check(messages.getNode("error", "banned-already"), errorBannedAlready).getString();
        errorBanUser = check(messages.getNode("error", "ban-user"), errorBanUser).getString();
        errorUnbanUser = check(messages.getNode("error", "unban-user"), errorUnbanUser).getString();
        errorNotBanned = check(messages.getNode("error", "not-banned"), errorNotBanned).getString();
        errorPermission = check(messages.getNode("error", "permission"), errorPermission).getString();
        errorTicketStatus = check(messages.getNode("error", "ticket-status"), errorTicketStatus).getString();
        errorTicketAlreadyClosed = check(messages.getNode("error", "ticket-already-closed"), errorTicketAlreadyClosed).getString();
        errorTicketNotClosed = check(messages.getNode("error", "ticket-not-closed"), errorTicketNotClosed).getString();
        errorTicketNan = check(messages.getNode("error", "ticket-nan"), errorTicketNan).getString();
        errorTicketOwner = check(messages.getNode("error", "ticket-owner"), errorTicketOwner).getString();
        errorTicketClaim = check(messages.getNode("error", "ticket-claim"), errorTicketClaim).getString();
        errorUserNotExist = check(messages.getNode("error", "user-not-exist"), errorUserNotExist).getString();
        errorUserNotSpecified = check(messages.getNode("error", "user-not-specified"), errorUserNotSpecified).getString();

        //teleport
        teleportToTicket = check(messages.getNode("teleport", "to-ticket"), teleportToTicket).getString();

        //TicketData
        ticketAssign = check(messages.getNode("ticket", "assign"), ticketAssign).getString();
        ticketAssignUser = check(messages.getNode("ticket", "assign-user"), ticketAssignUser).getString();
        ticketComment = check(messages.getNode("ticket", "comment"), ticketComment).getString();
        ticketCommentUser = check(messages.getNode("ticket", "comment-user"), ticketCommentUser).getString();
        ticketCommentText = check(messages.getNode("ticket", "comment-text"), ticketCommentText).getString();
        ticketClaim = check(messages.getNode("ticket", "claim"), ticketClaim).getString();
        ticketClaimUser = check(messages.getNode("ticket", "claim-user"), ticketClaimUser).getString();
        ticketClose = check(messages.getNode("ticket", "close"), ticketClose).getString();
        ticketCloseOffline = check(messages.getNode("ticket", "close-offline"), ticketCloseOffline).getString();
        ticketCloseOfflineMulti = check(messages.getNode("ticket", "close-offline-multi"), ticketCloseOfflineMulti).getString();
        ticketCloseText = check(messages.getNode("ticket", "close-text"), ticketCloseText).getString();
        ticketCloseUser = check(messages.getNode("ticket", "close-user"), ticketCloseUser).getString();
        ticketDuplicate = check(messages.getNode("ticket", "duplicate"), ticketDuplicate).getString();
        ticketOpen = check(messages.getNode("ticket", "open"), ticketOpen).getString();
        ticketOpenUser = check(messages.getNode("ticket", "open-user"), ticketOpenUser).getString();
        ticketTitleNotification = check(messages.getNode("ticket", "open-title-notification"), ticketTitleNotification).getString();
        ticketOnHoverTeleportTo = check(messages.getNode("ticket", "on-hover-teleport-to"), ticketOnHoverTeleportTo).getString();
        ticketReadNone = check(messages.getNode("ticket", "read-none"), ticketReadNone).getString();
        ticketReadNoneSelf = check(messages.getNode("ticket", "read-none-self"), ticketReadNoneSelf).getString();
        ticketReadNoneClosed = check(messages.getNode("ticket", "read-none-closed"), ticketReadNoneClosed).getString();
        ticketReadNoneHeld = check(messages.getNode("ticket", "read-none-held"), ticketReadNoneHeld).getString();
        ticketHold = check(messages.getNode("ticket", "hold"), ticketHold).getString();
        ticketHoldUser = check(messages.getNode("ticket", "hold-user"), ticketHoldUser).getString();
        ticketHoldText = check(messages.getNode("ticket", "hold-text"), ticketHoldText).getString();
        ticketUnresolved = check(messages.getNode("ticket", "unresolved"), ticketUnresolved).getString();
        ticketUnresolvedHeld = check(messages.getNode("ticket", "unresolved-held"), ticketUnresolvedHeld).getString();
        ticketUnclaim = check(messages.getNode("ticket", "unclaim"), ticketUnclaim).getString();
        ticketUnclaimUser = check(messages.getNode("ticket", "unclaim-user"), ticketUnclaimUser).getString();
        ticketNotExist = check(messages.getNode("ticket", "not-exist"), ticketNotExist).getString();
        ticketNotClaimed = check(messages.getNode("ticket", "not-claimed"), ticketNotClaimed).getString();
        ticketNotOpen = check(messages.getNode("ticket", "not-open"), ticketNotOpen).getString();
        ticketReopen = check(messages.getNode("ticket", "reopen"), ticketReopen).getString();
        ticketTooShort = check(messages.getNode("ticket", "too-short"), ticketTooShort).getString();
        ticketTooMany = check(messages.getNode("ticket", "too-many"), ticketTooMany).getString();
        ticketTooFast = check(messages.getNode("ticket", "too-fast"), ticketTooFast).getString();

        //staff
        staffListSeperator = check(messages.getNode("staff", "list-separator"), staffListSeperator).getString();
        staffListTitle = check(messages.getNode("staff", "list-title"), staffListTitle).getString();
        staffListEmpty = check(messages.getNode("staff", "list-empty"), staffListEmpty).getString();
        staffListPadding = check(messages.getNode("staff", "list-padding"), staffListPadding).getString();

        //plugin
        //pluginOutdated = check(messages.getNode("plugin", "outdated"), "&cYou are not running the latest recommended build! Recommended build is: &6{0}").getString();

        messageLoader.save(messages);

    }

    private CommentedConfigurationNode check(CommentedConfigurationNode node, Object defaultValue) {
        if (node.isVirtual()) {
            node.setValue(defaultValue);
        }
        return node;
    }

    public static Text parse(String key, Object ... params){
        return plugin.fromLegacy(MessageFormat.format(key, params));
    }
}
