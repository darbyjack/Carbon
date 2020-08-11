package net.draycia.carbon.listeners;

import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.events.ChatComponentEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PingHandler implements Listener {

    private final CarbonChat carbonChat;

    public PingHandler(CarbonChat carbonChat) {
        this.carbonChat = carbonChat;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPing(ChatComponentEvent event) {
        if (event.getTarget() == null) {
            return;
        }

        if (event.getSender().getUUID().equals(event.getTarget().getUUID())) {
            return;
        }

        String senderName = event.getSender().asOfflinePlayer().getName();

        if (senderName == null || !event.getOriginalMessage().contains(senderName)) {
            return;
        }

        if (!carbonChat.getConfig().getBoolean("pings.enabled")) {
            return;
        }

        Key key = Key.of(carbonChat.getConfig().getString("pings.sound"));
        Sound.Source source = Sound.Source.valueOf(carbonChat.getConfig().getString("pings.source"));
        float volume = (float) carbonChat.getConfig().getDouble("pings.volume");
        float pitch = (float) carbonChat.getConfig().getDouble("pings.pitch");

        event.getSender().playSound(Sound.of(key, source, volume, pitch));
    }

}
