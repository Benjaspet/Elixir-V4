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
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public LoopMode repeating = LoopMode.NONE;
    private final Guild guild;

    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.player = player;
        this.guild = guild;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.add(track);
        }
    }

    public void nextTrack() {
        if (queue.isEmpty()) {
            player.stopTrack();
            return;
        }
        if (player.getPlayingTrack() != null) {
            player.stopTrack();
        }
        player.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (this.queue.size() == 0 && this.repeating == LoopMode.NONE) {
            this.player.destroy();
            final AudioManager audioManager = guild.getAudioManager();
            if (Objects.requireNonNull(guild.getSelfMember().getVoiceState()).inAudioChannel()) {
                audioManager.closeAudioConnection();
            }
        }
        if (endReason.mayStartNext) {
            if (this.repeating == LoopMode.TRACK) {
                this.player.startTrack(track.makeClone(), false);
                return;
            } else if (this.repeating == LoopMode.QUEUE) {
                this.queue.add(track.makeClone());
            } else if (this.queue.size() > 0 && this.queue.poll() != null) {
                ElixirMusicManager.getInstance().lazyPlaySingularTrack(Objects.requireNonNull(this.queue.poll()).getInfo().title, guild);
            }
            nextTrack();
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return this.queue;
    }
    
    public enum LoopMode {
        NONE,
        TRACK,
        QUEUE,
        AUTOPLAY
    }
}
