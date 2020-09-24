package net.draycia.carbon.common.commands;

import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.standard.StringArgument;
import com.intellectualsites.commands.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.ChatUser;
import net.draycia.carbon.common.utils.CommandUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MessageCommand {

  @NonNull
  private final CarbonChat carbonChat;

  public MessageCommand(final @NonNull CommandManager<ChatUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CarbonChat carbonChat = CarbonChatProvider.carbonChat();
    final CommandSettings commandSettings = carbonChat.commandSettings().get("message");

    if (!commandSettings.enabled()) {
      return;
    }

    this.carbonChat.logger().info("Registering command [" + commandSettings.name() + "]");

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .withSenderType(ChatUser.class) // player
        .withPermission("carbonchat.message")
        .argument(CommandUtils.chatUserArgument())
        .argument(StringArgument.<ChatUser>newBuilder("message").greedy().build())
        .handler(this::sendMessage)
        .build()
    );
  }

  private void sendMessage(final @NonNull CommandContext<ChatUser> context) {
    context.<ChatUser>getRequired("target")
      .sendMessage(context.getSender(), context.getRequired("message"));
  }

}