/*
 * Copyright © 2022 Ben Petrillo. All rights reserved.
 *
 * Project licensed under the MIT License: https://www.mit.edu/~amini/LICENSE.md
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * All portions of this software are available for public use, provided that
 * credit is given to the original author(s).
 */

package dev.benpetrillo.elixir.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.music.laudiolin.LaudiolinTypes;
import dev.benpetrillo.elixir.types.laudiolin.LaudiolinTrackInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class TrackScheduler extends AudioEventAdapter {
    private final GuildMusicManager manager;
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public final Guild guild;

    public LoopMode repeating = LoopMode.NONE;

    public TrackScheduler(GuildMusicManager manager) {
        this.manager = manager;
        this.guild = manager.getGuild();
        this.player = manager.getAudioPlayer();
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Shuffles the remaining songs in the queue.
     */
    public List<AudioTrack> shuffle() {
        // Shuffle the queue.
        var tracks = new ArrayList<>(this.getQueue());
        Collections.shuffle(tracks);

        // Add the tracks to the queue.
        this.queue.clear();
        this.queue.addAll(tracks);

        return tracks;
    }

    /**
     * Serializes the queue into a list of LaudiolinTrackInfo objects.
     *
     * @see LaudiolinTrackInfo
     * @return A list of LaudiolinTrackInfo objects.
     */
    public List<LaudiolinTrackInfo> serialize() {
        return this.getQueue().stream()
                .map(LaudiolinTrackInfo::from)
                .toList();
    }

    /**
     * Synchronizes the queue with the client.
     */
    public void syncQueue() {
        this.manager.getLaudiolin().send(
                new LaudiolinTypes.Queue(this.serialize()));
    }

    /**
     * Queues or plays a track.
     * If a track is already playing, the track will be queued.
     * If a track is not playing, the track will be played.
     *
     * @param track The track to queue.
     */
    public void queue(AudioTrack track) {
        // Check if an item is already playing.
        if (this.player.getPlayingTrack() == null) {
            this.player.playTrack(track);
            this.manager.getLaudiolin().sync();
            return;
        }

        this.queue.add(track); // Place the track into the queue.
        this.syncQueue();
    }

    public void nextTrack() {
        if (this.queue.isEmpty()) {
            this.player.stopTrack();
            return;
        }
        if (this.player.getPlayingTrack() != null) {
            if(this.repeating == LoopMode.QUEUE)
                this.queue.add(player.getPlayingTrack().makeClone());
            this.player.stopTrack();
        }
        this.player.startTrack(queue.poll(), false);
    }

    /**
     * Sets the queue to the given tracks.
     *
     * @param tracks The tracks to set the queue to.
     */
    public void setQueue(Collection<AudioTrack> tracks) {
        this.queue.clear();
        this.queue.addAll(tracks);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (this.queue.isEmpty() && this.repeating == LoopMode.NONE) {
            this.player.destroy();
        }
        if (endReason.mayStartNext) {
            if (this.repeating == LoopMode.TRACK) {
                this.player.startTrack(track.makeClone(), false);
                return;
            } else if (this.repeating == LoopMode.QUEUE) {
                this.queue.add(track.makeClone());
            }
            nextTrack();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        final Throwable error = exception.getCause();
        if (error instanceof RuntimeException && error.getMessage().contains("403")) {
            this.player.startTrack(track.makeClone(), false);
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return this.queue;
    }

    @Getter
    @AllArgsConstructor
    public enum LoopMode {
        NONE(0),
        TRACK(2),
        QUEUE(1);

        final int value;

        /**
         * Converts the loop mode to a number.
         *
         * @return The loop mode as a number.
         */
        public static LoopMode fromNumber(int value) {
            return switch (value) {
                default -> throw new RuntimeException("Invalid loop mode.");
                case 0 -> LoopMode.NONE;
                case 1 -> LoopMode.QUEUE;
                case 2 -> LoopMode.TRACK;
            };
        }
    }
}
