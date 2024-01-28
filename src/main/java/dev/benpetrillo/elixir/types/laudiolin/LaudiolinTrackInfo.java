package dev.benpetrillo.elixir.types.laudiolin;

import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.music.laudiolin.LaudiolinAudioTrack;
import dev.benpetrillo.elixir.objects.LoadArguments;
import dev.benpetrillo.elixir.utilities.Utilities;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class LaudiolinTrackInfo {
    private String title, artist, icon, url, id;
    private int duration; // in seconds

    /**
     * @param track The Lavaplayer AudioTrack object.
     * @return A LaudiolinTrackInfo object.
     */
    public static LaudiolinTrackInfo from(AudioTrack track) {
        return LaudiolinTrackInfo.from(track.getInfo());
    }

    /**
     * @param info The Lavaplayer AudioTrackInfo object.
     * @return A LaudiolinTrackInfo object.
     */
    public static LaudiolinTrackInfo from(AudioTrackInfo info) {
        return LaudiolinTrackInfo.builder()
                .title(info.title)
                .artist(info.author)
                .icon(info.artworkUrl)
                .url(info.uri)
                .id(info.identifier)
                .duration((int) Math.floor(info.length / 1000f))
                .build();
    }

    /**
     * Converts this object into a Lavaplayer object.
     * @return A Lavaplayer object.
     */
    public AudioTrackInfo toLavaplayer() {
        return new AudioTrackInfo(this.title, this.artist,
                Math.round(this.duration * 1000f),
                this.id, false, this.url,
                this.icon, this.id);
    }

    /**
     * Converts this object into an AudioItem.
     *
     * @see LaudiolinAudioTrack
     * @return An AudioItem.
     */
    public LaudiolinAudioTrack toAudioItem() {
        return new LaudiolinAudioTrack(
                ElixirMusicManager.getInstance().laudiolinSource.getHttpAudioSource(),
                this.toLavaplayer(), new LoadArguments(
                    ElixirMusicManager.getInstance().getAudioPlayerManager(),
                    new AudioReference(this.id, this.title), Source.LAUDIOLIN), this.id);
    }

    @Override
    public String toString() {
        return Utilities.serialize(this);
    }
}
