package net.draycia.carbon.messaging.impl;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import net.draycia.carbon.CarbonChat;
import net.draycia.carbon.messaging.MessageService;
import net.draycia.carbon.storage.ChatUser;
import net.draycia.carbon.util.RedisListener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RedisMessageService implements MessageService {

  @NonNull
  private final Map<@NonNull String, @NonNull BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput>> userLoadedListeners = new HashMap<>();

  @NonNull
  private final Map<@NonNull String, @NonNull BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput>> userNotLoadedListeners = new HashMap<>();

  @NonNull
  private final RedisPubSubCommands<@NonNull String, @NonNull String> subscribeSync;

  @NonNull
  private final RedisPubSubCommands<@NonNull String, @NonNull String> publishSync;

  @NonNull
  private final UUID serverUUID = UUID.randomUUID();

  @NonNull
  private final CarbonChat carbonChat;

  public RedisMessageService(@NonNull final CarbonChat carbonChat) {
    this.carbonChat = carbonChat;

    final String host = this.carbonChat.getConfig().getString("redis.host");
    final String password = this.carbonChat.getConfig().getString("redis.password");
    final int port = this.carbonChat.getConfig().getInt("redis.port");
    final int database = this.carbonChat.getConfig().getInt("redis.database");

    final RedisURI.Builder builder = RedisURI.Builder.redis(host, port)
      .withDatabase(database);

    if (password != null) {
      builder.withPassword(password.toCharArray());
    }

    final RedisClient client = RedisClient.create(builder.build());

    final StatefulRedisPubSubConnection<String, String> subscribeConnection = client.connectPubSub();
    this.subscribeSync = subscribeConnection.sync();

    final StatefulRedisPubSubConnection<String, String> publishConnection = client.connectPubSub();
    this.publishSync = publishConnection.sync();

    subscribeConnection.addListener((RedisListener) (channel, message) -> {
      final ByteArrayDataInput input = ByteStreams.newDataInput(Base64.getDecoder().decode(message));

      final UUID messageUUID = new UUID(input.readLong(), input.readLong());

      if (messageUUID.equals(this.serverUUID)) {
        return;
      }

      final UUID uuid = new UUID(input.readLong(), input.readLong());

      this.receiveMessage(uuid, channel, input);
    });
  }

  private void receiveMessage(@NonNull final UUID uuid, @NonNull final String key, @NonNull final ByteArrayDataInput value) {
    final ChatUser user = this.carbonChat.userService().wrapIfLoaded(uuid);

    if (user != null) {
      for (final Map.Entry<String, BiConsumer<ChatUser, ByteArrayDataInput>> listener : this.userLoadedListeners.entrySet()) {
        if (key.equals(listener.getKey())) {
          listener.getValue().accept(user, value);
        }
      }
    }

    for (final Map.Entry<String, BiConsumer<UUID, ByteArrayDataInput>> listener : this.userNotLoadedListeners.entrySet()) {
      if (key.equals(listener.getKey())) {
        listener.getValue().accept(uuid, value);
      }
    }
  }

  @Override
  public void registerUserMessageListener(@NonNull final String key, @NonNull final BiConsumer<@NonNull ChatUser, @NonNull ByteArrayDataInput> listener) {
    this.userLoadedListeners.put(key, listener);
    this.subscribeSync.subscribe(key);
  }

  @Override
  public void registerUUIDMessageListener(@NonNull final String key, @NonNull final BiConsumer<@NonNull UUID, @NonNull ByteArrayDataInput> listener) {
    this.userNotLoadedListeners.put(key, listener);
    this.subscribeSync.subscribe(key);
  }

  @Override
  public void unregisterMessageListener(@NonNull final String key) {
    this.userLoadedListeners.remove(key);
    this.userNotLoadedListeners.remove(key);

    this.subscribeSync.unsubscribe(key);
  }

  @Override
  public void sendMessage(@NonNull final String key, @NonNull final UUID uuid, @NonNull final Consumer<@NonNull ByteArrayDataOutput> consumer) {
    final ByteArrayDataOutput msg = ByteStreams.newDataOutput();

    msg.writeLong(this.serverUUID.getMostSignificantBits());
    msg.writeLong(this.serverUUID.getLeastSignificantBits());
    msg.writeLong(uuid.getMostSignificantBits());
    msg.writeLong(uuid.getLeastSignificantBits());

    consumer.accept(msg);

    this.publishSync.publish(key, Base64.getEncoder().encodeToString(msg.toByteArray()));
  }

}
