package dev.benpetrillo.elixir.music.laudiolin;

import dev.benpetrillo.elixir.types.laudiolin.LaudiolinTrackInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
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
        // This message is both ways.
        private long position; // This is milliseconds.
        private float seek; // This is in seconds.

        /**
         * This constructor is used for the server -> client message.
         *
         * @param position The position in milliseconds.
         */
        public Seek(float position) {
            this.seek = position;
        }
    }

    @AllArgsConstructor
    final class Queue {
        // This message is client -> server.
        private final List<LaudiolinTrackInfo> queue;
    }
}
