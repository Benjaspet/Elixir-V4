package dev.benpetrillo.elixir.types.laudiolin;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.music.laudiolin.LaudiolinAudioTrack;
import dev.benpetrillo.elixir.utilities.Utilities;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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

    /**
     * Converts this object into an AudioItem.
     *
     * @see LaudiolinAudioTrack
     * @return An AudioItem.
     */
    public LaudiolinAudioTrack toAudioItem() {
        return new LaudiolinAudioTrack(
                ElixirMusicManager.getInstance()
                        .laudiolinSource
                        .getHttpAudioSource(),
                this.toLavaplayer(), this.getId()
        );
    }

    @Override
    public String toString() {
        return Utilities.serialize(this);
    }
}
