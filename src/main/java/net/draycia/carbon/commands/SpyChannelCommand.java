package net.draycia.carbon.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.channels.ChatChannel;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.storage.CommandSettings;
import net.draycia.carbon.storage.UserChannelSettings;
import net.draycia.carbon.util.CarbonUtils;
import net.draycia.carbon.util.CommandUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SpyChannelCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public SpyChannelCommand(@NonNull final CarbonChat carbonChat, @NonNull final CommandSettings commandSettings) {
    this.carbonChat = carbonChat;

    if (!commandSettings.enabled()) {
      return;
    }

    CommandUtils.handleDuplicateCommands(commandSettings);

    final List<Argument> channelArguments = new ArrayList<>();
    channelArguments.add(CarbonUtils.channelArgument("channel"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(channelArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.spy"))
      .executesPlayer(this::execute)
      .register();

    final List<Argument> whisperArguments = new ArrayList<>();
    whisperArguments.add(new LiteralArgument("whispers"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(whisperArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.spy"))
      .executesPlayer(this::executeWhispers)
      .register();

    final List<Argument> everythingArguments = new ArrayList<>();
    everythingArguments.add(new LiteralArgument("*"));
    everythingArguments.add(new BooleanArgument("should-spy"));

    new CommandAPICommand(commandSettings.name())
      .withArguments(everythingArguments)
      .withAliases(commandSettings.aliases())
      .withPermission(CommandPermission.fromString("carbonchat.spy"))
      .executesPlayer(this::executeEverything) // lul
      .register();
  }

  private void execute(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatChannel chatChannel = (ChatChannel) args[0];
    final ChatUser user = this.carbonChat.userService().wrap(player);

    final String message;

    final UserChannelSettings settings = user.channelSettings(chatChannel);

    if (settings.spying()) {
      settings.spying(false);
      message = this.carbonChat.language().getString("spy-toggled-off");
    } else {
      settings.spying(true);
      message = this.carbonChat.language().getString("spy-toggled-on");
    }

    user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player, message, "br", "\n",
      "color", "<color:" + chatChannel.channelColor(user).toString() + ">", "channel", chatChannel.name()));
  }

  private void executeWhispers(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final ChatUser user = this.carbonChat.userService().wrap(player);

    final String message;

    if (user.spyingwhispers()) {
      user.spyingWhispers(false);
      message = this.carbonChat.language().getString("spy-whispers-off");
    } else {
      user.spyingWhispers(true);
      message = this.carbonChat.language().getString("spy-whispers-on");
    }

    user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player, message, "br", "\n"));
  }

  private void executeEverything(@NonNull final Player player, @NonNull final Object @NonNull [] args) {
    final Boolean shouldSpy = (Boolean) args[0];

    final ChatUser user = this.carbonChat.userService().wrap(player);

    final String message;

    if (shouldSpy) {
      user.spyingWhispers(true);

      for (final ChatChannel channel : this.carbonChat.channelManager().registry().values()) {
        user.channelSettings(channel).spying(true);
      }

      message = this.carbonChat.language().getString("spy-everything-off");
    } else {
      user.spyingWhispers(false);

      for (final ChatChannel channel : this.carbonChat.channelManager().registry().values()) {
        user.channelSettings(channel).spying(false);
      }

      message = this.carbonChat.language().getString("spy-everything-on");
    }

    user.sendMessage(this.carbonChat.adventureManager().processMessageWithPapi(player, message, "br", "\n"));
  }

}
