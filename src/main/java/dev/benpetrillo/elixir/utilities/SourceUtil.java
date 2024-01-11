package dev.benpetrillo.elixir.utilities;

import dev.benpetrillo.elixir.types.laudiolin.Audio;
import dev.benpetrillo.elixir.types.laudiolin.Source;

/**
 * Playing audio methods.
 */
public interface SourceUtil {
    /**
     * Identifies a source from a query.
     * @param query The query.
     * @return The source.
     */
    static Source identify(String query) {
        if (query.contains("youtu.be") ||
            query.contains("youtube.com"))
            return Source.YOUTUBE;

        if (query.contains("open.spotify.com"))
            return Source.SPOTIFY;

        if (query.contains("laudiolin.seikimo.moe"))
            return Source.LAUDIOLIN;

        return Source.UNKNOWN;
    }

    /**
     * Identifies the type of audio from a query.
     *
     * @param query The query.
     * @return The audio type.
     */
    static Audio identifyType(String query) {
        if (query.contains("playlist"))
            return Audio.PLAYLIST;
        if (query.contains("&list="))
            return Audio.PLAYLIST;

        return Audio.TRACK;
    }

    /**
     * Attempts to pull a YouTube video ID.
     * @param query The query.
     * @return The video ID.
     */
    static String pullYouTubeId(String query) {
        if (query.contains("youtu.be"))
            return query.split("youtu.be/")[1];
        return query.split("v=")[1];
    }

    /**
     * Attempts to pull a generic track ID.
     * @param query The query.
     * @return The track ID.
     */
    static String pullGenericId(String query) {
        var id = query.split("/track/")[1];
        if (id.contains("?")) {
            id = id.split("\\?")[0];
        }
        return id;
    }

    /**
     * Attempts to pull a generic playlist ID.
     *
     * @param query The query.
     * @return The playlist ID.
     */
    static String pullPlaylistId(String query) {
        return query.split("/playlist/")[1];
    }
}
