package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.common.utils.CommandUtils;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChannelCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public ChannelCommand(final @NonNull CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("channel");

    if (!commandSettings.enabled()) {
      return;
    }

    this.carbonChat.logger().info("Registering command [" + commandSettings.name() + "]");

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.channel")
        .argument(CommandUtils.channelArgument())
        .handler(this::channel)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.channel.message")
        .argument(CommandUtils.channelArgument())
        .argument(StringArgument.required("message"))
        .handler(this::sendMessage)
        .build()
    );
  }

  private void channel(final @NonNull CommandContext<ChatUser> context) {
    final ChatUser user = context.getSender();
    final ChatChannel channel = context.getRequired("channel");

    if (user.channelSettings(channel).ignored()) {
      user.sendMessage(this.carbonChat.messageProcessor().processMessage(channel.cannotUseMessage(),
        "br", "\n",
        "color", "<" + channel.channelColor(user).toString() + ">",
        "channel", channel.name()));

      return;
    }

    user.selectedChannel(channel);
  }

  private void sendMessage(final @NonNull CommandContext<ChatUser> context) {
    final ChatUser user = context.getSender();
    final ChatChannel channel = context.getRequired("channel");
    final String message = context.getRequired("message");

    final Component component = channel.sendMessage(user, message, false);

    this.carbonChat.messageProcessor().audiences().console().sendMessage(component);
  }

}