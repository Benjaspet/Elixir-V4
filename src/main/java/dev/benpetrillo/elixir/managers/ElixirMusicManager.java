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

package dev.benpetrillo.elixir.managers;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.NanoIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.hc.core5.annotation.Internal;
import org.jetbrains.annotations.Nullable;
import tech.xigam.cch.utils.Interaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ElixirMusicManager {

    private static ElixirMusicManager instance;
    private final Map<String, GuildMusicManager> musicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    
    public final YoutubeAudioSourceManager youtubeSource = new YoutubeAudioSourceManager(true);
    public final SpotifySourceManager spotifySource = new SpotifySourceManager(youtubeSource);

    public ElixirMusicManager() {
        this.audioPlayerManager.registerSourceManager(this.spotifySource);
        this.audioPlayerManager.registerSourceManager(this.youtubeSource);
        this.audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        this.audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(new GetyarnAudioSourceManager());
        this.audioPlayerManager.registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        
        // IPv6 rotation setup.
        // If the bot receives a 429 from YouTube, it will rotate the IP address to another, provided that
        // an entire 64-bit block is provided in the config.

        String ipBlock; if(!(ipBlock = ElixirConstants.IPV6_BLOCK).isEmpty()) {
            new YoutubeIpRotatorSetup(new NanoIpRoutePlanner(Collections.singletonList(
                    new Ipv6Block(ipBlock)), true
            )).forSource(this.youtubeSource).setup();
            ElixirClient.logger.info("IPv6 rotator block set to " + ipBlock + ".");
        } else ElixirClient.logger.warn("You are not using an IPv6 rotator. This may cause issues with YouTube and rate-limiting.");
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getId(), (guildId) -> {
            GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, guild);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void removeGuildMusicManager(Guild guild) {
        this.musicManagers.remove(guild.getId());
    }
    
    @Nullable
    public GuildMusicManager getMusicManager(String guildId) {
        return this.musicManagers.get(guildId);
    }
    
    public GuildMusicManager[] getMusicManagers() {
        return this.musicManagers.values().toArray(new GuildMusicManager[0]);
    }

    public void loadAndPlay(String track, Interaction interaction, String url) {
        final GuildMusicManager musicManager = this.getMusicManager(interaction.getGuild());
        this.audioPlayerManager.loadItemOrdered(musicManager, track, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(interaction.getMember().getId());
                musicManager.scheduler.queue(track);
                final String title = track.getInfo().title;
                final String shortenedTitle = title.length() > 60 ? title.substring(0, 60) + "..." : title;
                MessageEmbed embed = new EmbedBuilder()
                        .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                        .setDescription(String.format("**Queued:** [%s](%s)", shortenedTitle, track.getInfo().uri))
                        .build();
                interaction.reply(embed);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                if (tracks.size() > 300) {
                    interaction.reply(EmbedUtil.sendErrorEmbed("Playlists that exceed 300 tracks cannot be played."));
                    return;
                }
                if (playlist.isSearchResult()) {
                    tracks.get(0).setUserData(interaction.getMember().getId());
                    final String title = tracks.get(0).getInfo().title;
                    final String shortenedTitle = title.length() > 60 ? title.substring(0, 60) + "..." : title;
                    MessageEmbed embed = new EmbedBuilder()
                            .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                            .setDescription(String.format("**Queued:** [%s](%s)", shortenedTitle, tracks.get(0).getInfo().uri))
                            .build();
                    interaction.reply(embed);
                    musicManager.scheduler.queue(tracks.get(0));
                } else {
                    final String success = String.format("Queued **%s** tracks from [%s](%s).", tracks.size(), playlist.getName(), url);
                    MessageEmbed embed = new EmbedBuilder()
                            .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                            .setDescription(success)
                            .build();
                    interaction.reply(embed);
                    for (final AudioTrack track : tracks) {
                        track.setUserData(interaction.getMember().getId());
                        musicManager.scheduler.queue(track);
                    }
                }
            }

            @Override
            public void noMatches() {
                interaction.reply(EmbedUtil.sendErrorEmbed("Nothing found by that search term."));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                Utilities.throwThrowable(new ElixirException(interaction.getGuild(), interaction.getMember()).exception(exception));
                interaction.reply(EmbedUtil.sendErrorEmbed("An error occurred while attempting to play that track."));
            }
        });
    }
    
    @Internal public void loadAndPlay(Guild guild, String track, Consumer<Object> callback) {
        final GuildMusicManager musicManager = this.getMusicManager(guild);
        this.audioPlayerManager.loadItemOrdered(musicManager, track, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                ElixirClient.logger.debug("Track loaded: " + audioTrack.getInfo().title);
                audioTrack.setUserData(ElixirConstants.BOT_ID);
                musicManager.scheduler.queue(audioTrack);
                callback.accept(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                ElixirClient.logger.debug("Playlist loaded: " + audioPlaylist.getName());
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (audioPlaylist.isSearchResult()) {
                    this.trackLoaded(tracks.get(0));
                } else {
                    for (final AudioTrack audioTrack : tracks) {
                        audioTrack.setUserData(ElixirConstants.BOT_ID);
                        musicManager.scheduler.queue(audioTrack);
                    }
                    callback.accept(tracks);
                }
            }

            @Override
            public void noMatches() {
                ElixirClient.logger.debug("No matches found for: " + track);
                callback.accept(null);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                ElixirClient.logger.debug("Failed to load: " + track);
                callback.accept(e);
                Utilities.throwThrowable(new ElixirException().guild(guild).exception(e));
            }
        });
    }

    public static ElixirMusicManager getInstance() {
        if (instance == null) instance = new ElixirMusicManager();
        return instance;
    }
}