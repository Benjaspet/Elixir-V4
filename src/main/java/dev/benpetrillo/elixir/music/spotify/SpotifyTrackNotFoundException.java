package dev.benpetrillo.elixir.music.spotify;

public final class SpotifyTrackNotFoundException extends RuntimeException {

    public SpotifyTrackNotFoundException() {
        super("No matching track found");
    }
}