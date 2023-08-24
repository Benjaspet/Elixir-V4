package dev.benpetrillo.elixir.music.laudiolin;

import dev.benpetrillo.elixir.types.laudiolin.LaudiolinTrackInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public interface LaudiolinTypes {
    @AllArgsConstructor
    final class Initialize {
        // This message is client -> server.
        private final String token;
        private final String botId;
        private final String guildId;
    }

    @Getter
    final class PlayTrack {
        // This message is server -> client.
        private String data;
    }

    @Getter
    @AllArgsConstructor
    final class Volume {
        // This message is both ways.
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

    @AllArgsConstructor
    final class Playing {
        // This message is client -> server.
        private final LaudiolinTrackInfo track;
    }

    @AllArgsConstructor
    final class Paused {
        // This message is client -> server.
        private boolean pause;
    }

    @Getter
    @AllArgsConstructor
    final class Loop {
        // This message is both ways.
        private int loopMode;
    }

    @AllArgsConstructor
    final class Guilds {
        // This message is client -> server.
        // This is sent over HTTP.
        private String botId;
        private List<String> inGuilds;
    }

    @Getter
    @Builder
    final class Synchronize {
        // This message is both ways.
        private Boolean doAll;
        private LaudiolinTrackInfo playingTrack; // This can be a 'blank track'.
        private Boolean paused;
        private Integer volume;
        private List<LaudiolinTrackInfo> queue;
        private Integer loopMode;
        private Float position;
        private Boolean shuffle;
    }
}
