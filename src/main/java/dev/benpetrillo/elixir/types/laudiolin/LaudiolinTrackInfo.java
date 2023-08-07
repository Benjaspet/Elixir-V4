package dev.benpetrillo.elixir.types.laudiolin;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.utilities.Utilities;
import lombok.Getter;

@Getter
public final class LaudiolinTrackInfo {
    private String title, artist, icon, url, id;
    private int duration; // in seconds

    /**
     * Converts this object into a Lavaplayer object.
     * @return A Lavaplayer object.
     */
    public AudioTrackInfo toLavaplayer() {
        return new AudioTrackInfo(this.title, this.artist,
            this.duration, this.id, false, this.url);
    }

    @Override
    public String toString() {
        return Utilities.serialize(this);
    }
}
