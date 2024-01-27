package dev.benpetrillo.elixir.music.laudiolin;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.objects.LoadArguments;
import dev.benpetrillo.elixir.types.laudiolin.LaudiolinTrackInfo;
import dev.benpetrillo.elixir.types.laudiolin.Source;
import dev.benpetrillo.elixir.utilities.LaudiolinUtil;
import dev.benpetrillo.elixir.utilities.SourceUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;

public final class LaudiolinSourceManager implements AudioSourceManager {
    @Getter private final HttpAudioSourceManager httpAudioSource = new HttpAudioSourceManager();
    private final YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager();
    private final SpotifySourceManager spotifyAudioSourceManager = new SpotifySourceManager(this.youtubeAudioSourceManager);
    private final SoundCloudAudioSourceManager soundCloudAudioSourceManager = SoundCloudAudioSourceManager.createDefault();

    /**
     * Loads an audio track.
     *
     * @param source The audio source.
     * @param query The audio query.
     * @param manager The audio player manager.
     * @param reference The audio reference.
     * @return The loaded audio track.
     */
    private AudioItem loadTrack(
            Source source, String query,
            AudioPlayerManager manager, AudioReference reference
    ) throws Exception {
        // Check if the source is SoundCloud.
        if (source == Source.SOUNDCLOUD)
            return this.soundCloudAudioSourceManager
                    .loadItem(manager, reference);

        // Parse the track data.
        var trackId = query;
        LaudiolinTrackInfo trackInfo = null;
        switch (source) {
            case YOUTUBE -> trackId = SourceUtil.pullYouTubeId(query);
            case SPOTIFY, LAUDIOLIN -> trackId = SourceUtil.pullGenericId(query);
            case UNKNOWN -> {
                // Search for the query.
                var results = LaudiolinUtil.search(query);
                if (results == null) throw new Exception("No results found.");

                // Get the top result.
                trackInfo = results.getTop();
                if (trackInfo == null) throw new Exception("No results found.");
                trackId = trackInfo.getId();
            }
        }

        // Fetch the information for the track.
        if (trackInfo == null) trackInfo = LaudiolinUtil.fetch(trackId);
        if (trackInfo == null) throw new Exception("Invalid track info.");

        return new LaudiolinAudioTrack(this.httpAudioSource, trackInfo.toLavaplayer(),
                new LoadArguments(manager, reference, source), trackId);
    }

    /**
     * Loads an audio playlist.
     *
     * @param source The audio source.
     * @param query The audio query.
     * @param manager The audio player manager.
     * @param reference The audio reference.
     * @return The loaded audio playlist.
     */
    private AudioItem loadPlaylist(
            Source source, String query,
            AudioPlayerManager manager, AudioReference reference
    ) throws Exception {
        return switch (source) {
            case YOUTUBE -> this.youtubeAudioSourceManager.loadItem(manager, reference);
            case SPOTIFY -> this.spotifyAudioSourceManager.loadItem(manager, reference);
            case SOUNDCLOUD -> this.soundCloudAudioSourceManager.loadItem(manager, reference);
            case LAUDIOLIN -> {
                // Pull the playlist ID.
                var playlistId = SourceUtil.pullPlaylistId(query);
                // Get the playlist.
                var playlist = LaudiolinUtil.fetchPlaylist(playlistId);
                if (playlist == null) throw new Exception("Invalid playlist.");

                // Create a collection of tracks.
                var tracks = new ArrayList<AudioTrack>();
                playlist.getTracks().forEach(track -> {
                    var trackReference = new AudioReference(track.getId(), track.getTitle());
                    tracks.add(new LaudiolinAudioTrack(this.httpAudioSource, track.toLavaplayer(),
                            new LoadArguments(manager, trackReference, source), track.getId()));
                });

                // Return the playlist.
                yield new BasicAudioPlaylist(playlist.getName(),
                        tracks, null, false);
            }
            case UNKNOWN -> throw new Exception("Invalid playlist source.");
        };
    }

    @Override
    public String getSourceName() {
        return "laudiolin";
    }

    @Override
    @SneakyThrows
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        // Get the query.
        var query = reference.identifier.trim();
        if (query.contains("<") && query.contains(">"))
            query = query.substring(1, query.length() - 1);
        // Find the source.
        var source = SourceUtil.identify(query);
        if (source == Source.UNKNOWN) return null;

        return switch (SourceUtil.identifyType(query)) {
            case TRACK -> this.loadTrack(source, query, manager, reference);
            case PLAYLIST -> this.loadPlaylist(source, query, manager, reference);
        };
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {

    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return null;
    }

    @Override
    public void shutdown() {

    }
}
