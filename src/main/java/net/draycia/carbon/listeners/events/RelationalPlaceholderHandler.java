package net.draycia.carbon.listeners.events;

import me.clip.placeholderapi.PlaceholderAPI;
import net.draycia.carbon.events.CarbonEvents;
import net.draycia.carbon.events.api.ChatFormatEvent;
import net.kyori.event.PostOrders;
import org.bukkit.entity.Player;

public class RelationalPlaceholderHandler {

  public RelationalPlaceholderHandler() {
    CarbonEvents.register(ChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      if (event.target() == null) {
        return;
      }

      if (!event.sender().online() || !event.target().online()) {
        return;
      }

      final Player sender = event.sender().player();
      final Player target = event.target().player();

      event.format(PlaceholderAPI.setRelationalPlaceholders(sender, target, event.format()));
    });
  }

}
