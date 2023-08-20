package dev.benpetrillo.elixir.music.laudiolin;

import dev.benpetrillo.elixir.types.laudiolin.LaudiolinTrackInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface LaudiolinTypes {
    @AllArgsConstructor
    final class Initialize {
        // This message is client -> server.
        private final String token;
        private final String guildId;
    }

    @Getter
    @AllArgsConstructor
    final class PlayTrack {
        // This message is server -> client.
        private final LaudiolinTrackInfo track;
    }
}
