/*
 * Copyright © 2023 Ben Petrillo, KingRainbow44. All rights reserved.
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

package dev.benpetrillo.elixir.api;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.music.TrackScheduler;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utilities.HttpUtil;
import dev.benpetrillo.elixir.utilities.PlaylistUtil;
import dev.benpetrillo.elixir.utilities.TrackUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import tech.xigam.express.Request;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

/**
 * Includes:
 * - /playlist
 */

@SuppressWarnings({"JavadocReference"})

public final class PlaylistEndpoint {

    /**
     * The base URL for the playlist endpoint.
     * Ex: https://app.ponjo.club/v1/elixir/playlist?id=magix
     * @param playlistId The playlist ID.
     * @param action What to do with the playlist.
     *               
     * @param track (optional) The track to add to the playlist.
     * @param position (optional) The position to add the track to.
     * @param guildId (optional) The guild ID of the guild you want to queue the playlist in.
     * @param channelId (optional) The channel ID of the channel you want to queue the playlist in.
     */
    
    public static void indexEndpoint(Request request) {
        String playlistId = request.requestArguments.getOrDefault("playlistId", "");
        String action = request.requestArguments.getOrDefault("action", "");
        if (playlistId.isEmpty() || action.isEmpty()) {
            request.code(400).respond("Missing required arguments."); return;
        }
        CustomPlaylist playlist = PlaylistUtil.findPlaylist(playlistId);
        if (playlist == null) {
            request.code(404).respond("Playlist not found."); return;
        }
        switch(action) {
            default -> request.code(400).respond("Invalid action.");
            case "fetch" -> request.respond(Utilities.base64Encode(Utilities.serialize(playlist)));
            case "addtrack" -> {
                try {
                    addTrack(request, playlist);
                } catch(UnsupportedEncodingException exception) {
                    Utilities.throwThrowable(new ElixirException().exception(exception));
                }
            }
            case "queue" -> queue(request, playlist);
        }
    }

    /*
     * Below are endpoint methods, they are not endpoints.
     */
    
    private static void addTrack(Request request, CustomPlaylist playlist) throws UnsupportedEncodingException {
        String track = request.requestArguments.getOrDefault("track", "");
        int position = Integer.parseInt(request.requestArguments.getOrDefault("position", "-1"));
        if (track.isEmpty()) {
            request.code(400).respond("Missing required arguments."); return;
        }
        track = Utilities.base64Decode(track);
        if (!Utilities.isValidURL(track)) {
            track = HttpUtil.searchForVideo(track);
        }
        AudioTrackInfo trackInfo = TrackUtil.getTrackInfoFromUrl(track);
        if (trackInfo == null) {
            request.code(400).respond("Unable to get track info."); return;
        }
        PlaylistUtil.addTrackToList(trackInfo, playlist, position);
        request.respond("Track added.");
    }
    
    private static void queue(Request request, CustomPlaylist playlist) {
        final String guildId = request.requestArguments.getOrDefault("guildId", "");
        final String channelId = request.requestArguments.getOrDefault("channelId", "");
        if (guildId.isEmpty() || channelId.isEmpty()) {
            request.code(400).respond("Missing required arguments."); return;
        }
        final Guild guild = ElixirClient.getJda().getGuildById(guildId);
        if (guild == null) {
            request.code(400).respond("Unable to find guild."); return;
        }
        final VoiceChannel voiceChannel = guild.getVoiceChannelById(channelId);
        if (voiceChannel == null) {
            request.code(400).respond("Unable to find voice channel."); return;
        }
        final GuildVoiceState voiceState = guild.getSelfMember().getVoiceState();
        assert voiceState != null;
        if (!voiceState.inAudioChannel()) {
            guild.getAudioManager().openAudioConnection(voiceChannel);
            guild.getAudioManager().setSelfDeafened(true);
        }
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(guild);
        final List<PlaylistTrack> tracks = PlaylistUtil.getTracks(playlist); TrackUtil.appendUser("838118537276031006", tracks);
        if (playlist.options.shuffle) Collections.shuffle(tracks);
        if (musicManager.scheduler.queue.isEmpty() && musicManager.audioPlayer.getPlayingTrack() == null) {
            musicManager.scheduler.repeating = playlist.options.repeat ? TrackScheduler.LoopMode.QUEUE : TrackScheduler.LoopMode.NONE;
            musicManager.audioPlayer.setVolume(playlist.info.volume);
        }
        musicManager.scheduler.getQueue().addAll(tracks);
        if (musicManager.audioPlayer.getPlayingTrack() == null) musicManager.scheduler.nextTrack();
        request.respond("Queued.");
    }
}
