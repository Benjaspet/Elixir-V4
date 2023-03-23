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
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class TrackScheduler extends AudioEventAdapter {
    
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public final Guild guild;

    public LoopMode repeating = LoopMode.NONE;

    public TrackScheduler(AudioPlayer player, Guild guild) {
        this.guild = guild;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (player.getPlayingTrack() == null) {
            player.playTrack(track);
            return;
        }
        queue.add(track);
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

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (this.queue.size() == 0 && this.repeating == LoopMode.NONE) {
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
    
    public enum LoopMode {
        NONE,
        TRACK,
        QUEUE
    }
}
