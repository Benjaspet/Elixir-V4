package dev.benpetrillo.elixir.music.laudiolin;

import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.objects.LoadArguments;
import dev.benpetrillo.elixir.utilities.LaudiolinUtil;

public final class LaudiolinAudioTrack extends DelegatedAudioTrack {

    private final HttpAudioSourceManager httpAudioSource;
    private final LoadArguments fallback;
    private final String trackId;

    private int loadDepth = 0;

    /**
     * @param fallback Fallback data for if the track fails to load.
     */
    public LaudiolinAudioTrack(
        HttpAudioSourceManager httpAudioSource,
        AudioTrackInfo trackInfo,
        LoadArguments fallback,
        String trackId
    ) {
        super(trackInfo);

        this.httpAudioSource = httpAudioSource;
        this.fallback = fallback;
        this.trackId = trackId;
    }

    /**
     * Creates a URL for the track.
     * @return A URL for the track.
     */
    private String createUrl() {
        return LaudiolinUtil.ENDPOINT + "/download?id=" + this.trackId;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        if (this.loadDepth++ > 5) {
            // Attempt to load from the original source.
            var original = this.fallback;
            var musicManager = ElixirMusicManager.getInstance();
            var sourceManager = switch (original.originalSource()) {
                case YOUTUBE -> musicManager.youtubeSource;
                case SPOTIFY -> musicManager.spotifySource;
                case SOUNDCLOUD -> musicManager.soundCloudSource;
                default -> null;
            };

            if (sourceManager != null) {
                var item = sourceManager.loadItem(
                        original.manager(), original.reference());
                if (item instanceof InternalAudioTrack track) {
                    this.processDelegate(track, executor);
                    return;
                }
            }
        }

        try {
            this.processDelegate((InternalAudioTrack) this.httpAudioSource.loadItem(null,
                    new AudioReference(this.createUrl(), null)), executor);
        } catch (FriendlyException ignored) {
            this.process(executor);
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new LaudiolinAudioTrack(this.httpAudioSource, this.trackInfo, this.fallback, this.trackId);
    }
}
