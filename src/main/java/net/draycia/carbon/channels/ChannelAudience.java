/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.draycia.carbon.channels;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A receiver that wraps one or more receivers.
 *
 * <p><code>ForwardingAudience</code> is designed to easily allow users or
 * implementations wrap an existing (collection of) <code>Audience</code>(s).</p>
 *
 * @see Audience
 * @since 4.0.0
 * @version 4.0.0
 */
@FunctionalInterface
public interface ChannelAudience extends Audience {
    /**
     * Gets the audiences.
     *
     * @return the audiences
     * @since 4.0.0
     */
    @NonNull Iterable<? extends ChannelUser> audiences();

    @Override
    default void sendMessage(final @NonNull Component message, final @NonNull MessageType type) {
        for (final Audience audience : this.audiences()) audience.sendMessage(message, type);
    }

    @Override
    default void sendActionBar(final @NonNull Component message) {
        for (final Audience audience : this.audiences()) audience.sendActionBar(message);
    }

    @Override
    default void showTitle(final @NonNull Title title) {
        for (final Audience audience : this.audiences()) audience.showTitle(title);
    }

    @Override
    default void clearTitle() {
        for (final Audience audience : this.audiences()) audience.clearTitle();
    }

    @Override
    default void resetTitle() {
        for (final Audience audience : this.audiences()) audience.resetTitle();
    }

    @Override
    default void showBossBar(final @NonNull BossBar bar) {
        for (final Audience audience : this.audiences()) audience.showBossBar(bar);
    }

    @Override
    default void hideBossBar(final @NonNull BossBar bar) {
        for (final Audience audience : this.audiences()) audience.hideBossBar(bar);
    }

    @Override
    default void playSound(final @NonNull Sound sound) {
        for (final Audience audience : this.audiences()) audience.playSound(sound);
    }

    @Override
    default void playSound(final @NonNull Sound sound, final double x, final double y, final double z) {
        for (final Audience audience : this.audiences()) audience.playSound(sound, x, y, z);
    }

    @Override
    default void stopSound(final @NonNull SoundStop stop) {
        for (final Audience audience : this.audiences()) audience.stopSound(stop);
    }

    @Override
    default void openBook(final @NonNull Book book) {
        for (final Audience audience : this.audiences()) audience.openBook(book);
    }
}