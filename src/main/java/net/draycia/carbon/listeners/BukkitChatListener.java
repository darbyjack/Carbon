package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChannelUser;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.channels.impls.ChannelUserWrapper;
import net.draycia.carbon.storage.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class BukkitChatListener implements Listener {

    private final CarbonChat carbonChat;

    public BukkitChatListener(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    // Chat messages
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerchat(AsyncPlayerChatEvent event) {
        ChatUser user = carbonChat.getUserService().wrap(event.getPlayer());
        ChatChannel channel = user.getSelectedChannel();

        if (channel.shouldCancelChatEvent()) {
            event.setCancelled(true);
        }

        for (ChatChannel entry : carbonChat.getChannelManager().getRegistry().values()) {
            if (entry.getMessagePrefix() == null) {
                continue;
            }

            if (event.getMessage().startsWith(entry.getMessagePrefix())) {
                if (entry.canPlayerUse(user)) {
                    event.setMessage(event.getMessage().substring(entry.getMessagePrefix().length()));
                    channel = entry;
                    break;
                }
            }
        }

        final ChatChannel selectedChannel = channel;

        if (!selectedChannel.canPlayerUse(user)) {
            return;
        }

        final Iterable<? extends ChannelUser> recipients;

        if (selectedChannel.honorsRecipientList()) {
            recipients = new HashSet<>();

            for (Player recipient : event.getRecipients()) {
                ((HashSet<ChannelUserWrapper>)recipients)
                        .add(new ChannelUserWrapper(carbonChat.getUserService().wrap(recipient), false));
            }
        } else {
            recipients = selectedChannel.audiences();
        }

        event.getRecipients().clear();

        if (event.isAsynchronous()) {
            Component component = selectedChannel.sendMessage(user, recipients, event.getMessage(), false);

            event.setFormat(LegacyComponentSerializer.legacySection().serialize(component)
                    .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(carbonChat, () -> {
                Component component = selectedChannel.sendMessage(user, recipients, event.getMessage(), false);

                carbonChat.getLogger().info(LegacyComponentSerializer.legacySection().serialize(component)
                        .replaceAll("(?:[^%]|\\A)%(?:[^%]|\\z)", "%%"));

                if (carbonChat.getConfig().getBoolean("show-tips")) {
                    carbonChat.getLogger().info("Tip: Sync chat event! I cannot set the message format due to this. :(");
                    carbonChat.getLogger().info("Tip: To 'solve' this, do a binary search and see which plugin is triggering");
                    carbonChat.getLogger().info("Tip: sync chat events and causing this, and let that plugin author know.");
                }
            });
        }
    }

}
