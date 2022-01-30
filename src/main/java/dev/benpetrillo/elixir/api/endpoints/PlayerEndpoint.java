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

package dev.benpetrillo.elixir.api.endpoints;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.api.HttpEndpoint;
import dev.benpetrillo.elixir.api.HttpResponse;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.Utilities;

import java.io.IOException;
import java.util.List;

public final class PlayerEndpoint extends HttpEndpoint {

    /**
     * TODO: If the bot isn't in a voice channel, throw an error on pause, resume, nowplaying, etc.
     */

    private GuildMusicManager musicManager;
    
    @Override
    public void get() throws IOException {
        var guildId = this.arguments.getOrDefault("guildId", "");
        var action = this.arguments.getOrDefault("action", "");
        if (guildId.isEmpty() || action.isEmpty()) {
            this.respond(new HttpResponse.NotFound()); return;
        }
        this.musicManager = ElixirMusicManager.getInstance().getMusicManager(guildId);
        if (this.musicManager == null) {
            this.respond(new HttpResponse.NotFound()); return;
        }
        switch (action) {
            default -> this.respond(new HttpResponse.NotFound());
            case "nowplaying" -> {
                this.nowPlaying(); return;
            }
            case "pause" -> this.musicManager.audioPlayer.setPaused(true);
            case "resume" -> this.musicManager.audioPlayer.setPaused(false);
            case "skip" -> this.musicManager.scheduler.nextTrack();
            case "play" -> {
                this.play(); return;
            }
        }
        this.respond(new HttpResponse.Success());
    }
    
    private void nowPlaying() throws IOException {
        var track = this.musicManager.audioPlayer.getPlayingTrack();
        if (track == null) {
            this.respond(new HttpResponse.NotFound());
            return;
        }
        this.statusCode = 301;
        this.respond(Utilities.serialize(track.getInfo()));
    }
    
    @SuppressWarnings("unchecked")
    private void play() throws IOException {
        var query = this.arguments.getOrDefault("query", "");
        var guild = this.arguments.getOrDefault("guild", "");
        if(query.isEmpty() || guild.isEmpty()) {
            this.respond(new HttpResponse.NotFound());
            return;
        }
        
        ElixirMusicManager.getInstance().loadAndPlay(ElixirClient.getJda().getGuildById(guild), Utilities.base64Decode(query), object -> {
            try {
                if(object == null) {
                    this.respond(new HttpResponse.NotFound());
                } else if(object instanceof AudioTrack) {
                    this.respond(Utilities.base64Encode(
                            Utilities.serialize(((AudioTrack) object).getInfo())
                    ));
                } else if(object instanceof List<?>) {
                    List<AudioTrack> tracks = (List<AudioTrack>) object;
                    this.respond(Utilities.base64Encode(
                            Utilities.serialize(tracks.stream().map(AudioTrack::getInfo).toArray())
                    ));
                } else if(object instanceof Throwable) {
                    this.respond(new HttpResponse.BadRequest());
                }
            } catch (IOException ignored) { }
        });
    }
}
