package dev.benpetrillo.elixir.music.laudiolin;

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
    final class PlayTrack {
        // This message is server -> client.
        private String data;
    }

    @Getter
    final class Volume {
        // This message is server -> client.
        private int volume;
    }

    @Getter
    final class Skip {
        // This message is server -> client.
        private int track;
    }

    @Getter
    final class Seek {
        // This message is server -> client.
        private long position; // This is milliseconds.
    }
}
