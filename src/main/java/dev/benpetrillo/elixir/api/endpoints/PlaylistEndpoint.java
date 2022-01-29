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

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.api.HttpEndpoint;
import dev.benpetrillo.elixir.api.HttpResponse;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.music.TrackScheduler;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utilities.HttpUtil;
import dev.benpetrillo.elixir.utilities.PlaylistUtil;
import dev.benpetrillo.elixir.utilities.TrackUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.io.IOException;
import java.util.Collections;

public final class PlaylistEndpoint extends HttpEndpoint {

    private CustomPlaylist playlist;
    
    @Override
    public void get() throws IOException {
        var playlistId = this.arguments.getOrDefault("playlistId", "");
        var action = this.arguments.getOrDefault("action", "");
        if (playlistId.isEmpty() || action.isEmpty()) {
            this.respond(new HttpResponse.NotFound()); return;
        }
        this.playlist = PlaylistUtil.findPlaylist(playlistId);
        if (this.playlist == null) {
            this.respond(new HttpResponse.NotFound()); return;
        }
        switch (action) {
            default -> this.respond(new HttpResponse.NotFound());
            case "fetch" -> this.fetch();
            case "addtrack" -> this.addTrack();
            case "queue" -> this.queue();
        }
    }
    
    private void fetch() throws IOException {
        this.respond(Utilities.base64Encode(Utilities.serialize(this.playlist)));
    }
    
    private void addTrack() throws IOException {
        var track = this.arguments.getOrDefault("track", "");
        var position = Integer.parseInt(this.arguments.getOrDefault("position", "-1"));
        if (track.isEmpty()) {
            this.respond(new HttpResponse.BadRequest()); return;
        }
        track = Utilities.base64Decode(track);
        if (!Utilities.isValidURL(track)) {
            track = HttpUtil.getYouTubeURL(track);
        }
        var trackInfo = TrackUtil.getTrackInfoFromUrl(track);
        if (trackInfo == null) {
            this.respond(new HttpResponse.BadRequest()); return;
        }
        PlaylistUtil.addTrackToList(trackInfo, this.playlist, position);
        this.respond(new HttpResponse.Success());
    }
    
    private void queue() throws IOException {
        var guildId = this.arguments.getOrDefault("guildId", "");
        var channelId = this.arguments.getOrDefault("channelId", "");
        if(guildId.isEmpty() || channelId.isEmpty()) {
            this.respond(new HttpResponse.BadRequest()); return;
        }
        final Guild guild = ElixirClient.getJda().getGuildById(guildId);
        if(guild == null) {
            this.respond(new HttpResponse.BadRequest()); return;
        }
        final AudioChannel voiceChannel = guild.getVoiceChannelById(channelId);
        if(voiceChannel == null) {
            this.respond(new HttpResponse.BadRequest()); return;
        }
        final GuildVoiceState voiceState = guild.getSelfMember().getVoiceState();
        assert voiceState != null;
        if(!voiceState.inAudioChannel()) {
            guild.getAudioManager()
                    .openAudioConnection(voiceChannel);
            guild.getAudioManager()
                    .setSelfDeafened(true);
        }
        GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(guild);
        var tracks = PlaylistUtil.getTracks(playlist); TrackUtil.appendUser("838118537276031006", tracks);
        if (playlist.options.shuffle) Collections.shuffle(tracks);
        if (musicManager.scheduler.queue.isEmpty() && musicManager.audioPlayer.getPlayingTrack() == null) {
            musicManager.scheduler.repeating = playlist.options.repeat
                    ? TrackScheduler.LoopMode.QUEUE : TrackScheduler.LoopMode.NONE;
            musicManager.audioPlayer.setVolume(playlist.info.volume);
        }
        musicManager.scheduler.getQueue().addAll(tracks);
        if (musicManager.audioPlayer.getPlayingTrack() == null) musicManager.scheduler.nextTrack();
        this.respond(new HttpResponse.Success());
    }
}
