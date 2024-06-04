/*
 * Copyright © 2023 Ben Petrillo. All rights reserved.
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

package dev.benpetrillo.elixir.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.music.AudioPlayerSendHandler;
import dev.benpetrillo.elixir.music.TrackScheduler;
import dev.benpetrillo.elixir.music.laudiolin.LaudiolinInterface;
import dev.benpetrillo.elixir.music.laudiolin.LaudiolinTypes;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

@Getter
public final class GuildMusicManager {

    private final Guild guild;
    public final AudioPlayer audioPlayer;
    @Getter
    public final TrackScheduler scheduler;
    private final LaudiolinInterface laudiolin;

    public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
        this.guild = guild;
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this);
        this.audioPlayer.addListener(this.scheduler);
        this.laudiolin = new LaudiolinInterface(this, guild);
        // Try connecting to the Laudiolin backend.
        if (Config.get("LAUDIOLIN-API") != null && !Config.get("LAUDIOLIN-API").isEmpty()) {
            this.laudiolin.connect();
        }
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(this.audioPlayer);
    }

    /**
     * Queues an audio track for playing.
     *
     * @param track The audio track to queue.
     */
    public void play(AudioTrack track) {
        this.getScheduler().queue(track);
    }

    /**
     * Queues an audio playlist's contents.
     *
     * @param playlist The audio playlist to queue.
     */
    public void play(AudioPlaylist playlist) {
        for (var track : playlist.getTracks())
            this.getScheduler().queue(track);
    }

    /**
     * Sets the volume of the audio player.
     *
     * @param volume The volume to set.
     */
    public void setVolume(int volume) {
        volume = Math.max(0, Math.min(150, volume));

        this.audioPlayer.setVolume(volume);
        if (this.laudiolin != null) {
            this.laudiolin.send(new LaudiolinTypes.Volume(volume));
        }
    }

    /**
     * Shuffles the queue.
     */
    public void shuffle() {
        this.scheduler.shuffle();
        assert this.laudiolin != null;
        this.laudiolin.send(new LaudiolinTypes.Queue(this.scheduler.serialize()));
    }

    public void stop() {
        this.audioPlayer.setVolume(100); // Reset the volume to default.
        this.audioPlayer.destroy(); // Destroy the audio player.
        this.scheduler.queue.clear(); // Clear the queue.
        this.scheduler.repeating = TrackScheduler.LoopMode.NONE; // Disable loop mode.
        assert this.laudiolin != null;
        this.laudiolin.close(); // Close the laudiolin interface.
    }
}
