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

package dev.benpetrillo.elixir.api;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.api.objects.NowPlayingObject;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import tech.xigam.express.Request;

import java.util.List;

/**
 * Includes:
 * - /player
 */

@SuppressWarnings({"JavadocReference"})

public final class PlayerEndpoint {

    /**
     * The base URL for the player endpoint.
     * Ex: https://app.ponjo.club/v1/elixir/player/
     * @param guildId The guild ID.
     * @param action The action to execute.
     *               
     * @param query (optional) The query to play.
     */
    
    public static void indexEndpoint(Request request) {
        final String guildId = request.requestArguments.getOrDefault("guildId", "");
        final String action = request.requestArguments.getOrDefault("action", "");
        if (guildId.isEmpty() || action.isEmpty()) {
            request.code(400).respond("Missing required arguments."); return;
        }
        final Guild guild = ElixirClient.getJda().getGuildById(guildId);
        if (guild == null) {
            request.code(404).respond("Guild not found."); return;
        }
        if (!guild.getAudioManager().isConnected()) {
            request.code(410).respond("The bot isn't connected to a voice channel, maybe try /player/join first?"); return;
        }
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(guild);
        switch (action) {
            default -> {
                request.code(400).respond("Invalid action.");
                return;
            }
            case "pause" -> musicManager.audioPlayer.setPaused(true);
            case "resume" -> musicManager.audioPlayer.setPaused(false);
            case "skip" -> musicManager.scheduler.nextTrack();
            case "stop" -> {
                stop(request, guild, musicManager); return;
            }
            case "nowplaying" -> {
                nowPlaying(request, musicManager); return;
            }
            case "play" -> {
                play(request, guild); return;
            }
        }
        request.respond("Success.");
    }

    /**
     * Has the bot join a specified voice channel.
     * Ex: https://app.ponjo.club/v1/elixir/player/join/
     * @param guildId The guild ID with the voice channel.
     * @param channelId The voice channel ID in the specified guild.
     */
    
    public static void joinEndpoint(Request request) {
        final String guildId = request.requestArguments.getOrDefault("guildId", "");
        final String channelId = request.requestArguments.getOrDefault("channelId", "");
        if (guildId.isEmpty() || channelId.isEmpty()) {
            request.code(400).respond("Missing required arguments."); return;
        }
        final Guild guild = ElixirClient.getJda().getGuildById(guildId);
        if (guild == null) {
            request.code(404).respond("Guild not found."); return;
        }
        if (guild.getAudioManager().isConnected()) {
            request.code(409).respond("The bot is already connected to a voice channel."); return;
        }
        final VoiceChannel channel = guild.getVoiceChannelById(channelId);
        if (channel == null) {
            request.code(404).respond("Voice channel not found."); return;
        }
        guild.getAudioManager().openAudioConnection(channel);
        request.respond("Connected to voice channel.");
    }
    
    /*
     * Below are endpoint methods, they are not endpoints.
     */
    
    private static void stop(Request request, Guild guild, GuildMusicManager musicManager) {
        musicManager.scheduler.queue.clear(); // Clear the queue.
        musicManager.audioPlayer.destroy(); // Destroy the player.
        if (guild.getAudioManager().isConnected()) {
            guild.getAudioManager().closeAudioConnection(); // Disconnect from the voice channel.
        }
        request.respond("Success.");
    }
    
    private static void nowPlaying(Request request, GuildMusicManager musicManager) {
        final AudioTrack audioTrack = musicManager.audioPlayer.getPlayingTrack();
        if (audioTrack == null) {
            request.code(410).respond("The bot isn't playing anything."); return;
        }
        request.respond(Utilities.base64Encode(Utilities.serialize(NowPlayingObject.create(audioTrack))));
    }
    
    @SuppressWarnings("unchecked")
    private static void play(Request request, Guild guild) {
        final String query = request.requestArguments.getOrDefault("query", "");
        if (query.isEmpty()) {
            request.code(400).respond("Missing required arguments."); return;
        }
        String decodedQuery = Utilities.base64Decode(query);
        if (!Utilities.isValidURL(decodedQuery)) {
            decodedQuery = "ytsearch:" + decodedQuery;
        }
        ElixirMusicManager.getInstance().loadAndPlay(guild, decodedQuery, object -> {
            if (object == null) {
                request.code(404).respond("No results found.");
            } else if (object instanceof AudioTrack) {
                request.respond(Utilities.base64Encode(
                        Utilities.serialize(((AudioTrack) object).getInfo())
                ));
            } else if (object instanceof List<?>) {
                final List<AudioTrack> tracks = (List<AudioTrack>) object;
                request.respond(Utilities.base64Encode(Utilities.serialize(tracks.stream().map(AudioTrack::getInfo).toArray())));
            } else if(object instanceof Throwable) {
                request.code(400).respond("The bot encountered an error while trying to play the track.");
            }
        });
    }
}
