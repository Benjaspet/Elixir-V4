package dev.benpetrillo.elixir.types.laudiolin;

import lombok.AllArgsConstructor;

/**
 * Audio sources.
 */
@AllArgsConstructor
public enum Source {
    YOUTUBE("https://youtu.be/"),
    SPOTIFY("https://open.spotify.com/track/"),
    LAUDIOLIN("https://laudiol.in/track/"),
    SOUNDCLOUD("https://www.soundcloud.com/"),
    UNKNOWN("");

    final String baseUrl;
}
