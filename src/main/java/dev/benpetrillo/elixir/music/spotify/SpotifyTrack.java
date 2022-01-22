/*
 * Copyright © 2022 Ben Petrillo. All rights reserved.
 *
 * Project licensed under the MIT License: https://www.mit.edu/~amini/LICENSE.md
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * All portions of this software are available for public use, provided that
 * credit is given to the original author(s).
 */

package dev.benpetrillo.elixir.music.spotify;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import se.michaelthelin.spotify.model_objects.specification.*;

public final class SpotifyTrack extends DelegatedAudioTrack {

    private final String isrc;
    private final String artworkURL;
    private final SpotifySourceManager spotifySourceManager;

    public SpotifyTrack(String title, String identifier, String isrc, Image[] images, String uri, ArtistSimplified[] artists, Integer trackDuration, SpotifySourceManager spotifySourceManager) {
        this(new AudioTrackInfo(title,
                artists.length == 0 ? "unknown" : artists[0].getName(),
                trackDuration.longValue(),
                identifier,
                false,
                "https://open.spotify.com/track/" + identifier
        ), isrc, images.length == 0 ? null : images[0].getUrl(), spotifySourceManager);
    }

    public SpotifyTrack(AudioTrackInfo trackInfo, String isrc, String artworkURL, SpotifySourceManager spotifySourceManager) {
        super(trackInfo);
        this.isrc = isrc;
        this.artworkURL = artworkURL;
        this.spotifySourceManager = spotifySourceManager;
    }
    
    public static SpotifyTrack of(TrackSimplified track, Album album, SpotifySourceManager spotifySourceManager) {
        return new SpotifyTrack(track.getName(), track.getId(), null, album.getImages(), track.getUri(), track.getArtists(), track.getDurationMs(), spotifySourceManager);
    }

    public static SpotifyTrack of(Track track, SpotifySourceManager spotifySourceManager) {
        return new SpotifyTrack(track.getName(), track.getId(), track.getExternalIds().getExternalIds().getOrDefault("isrc", null), track.getAlbum().getImages(), track.getUri(), track.getArtists(), track.getDurationMs(), spotifySourceManager);
    }

    public String getISRC() {
        return this.isrc;
    }

    public String getArtworkURL() {
        return this.artworkURL;
    }

    private String getQuery() {
        var query = "ytsearch:" + trackInfo.title;
        if (!trackInfo.author.equals("unknown")) {
            query += " " + trackInfo.author;
        }
        return query;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        AudioItem track = null;
        if (this.isrc != null) {
            track = this.spotifySourceManager.getSearchSourceManager().loadItem(null, new AudioReference("ytsearch:\"" + this.isrc + "\"", null));
        }
        if (track == null) {
            track = this.spotifySourceManager.getSearchSourceManager().loadItem(null, new AudioReference(getQuery(), null));
        }

        if (track instanceof AudioPlaylist) {
            track = ((AudioPlaylist) track).getTracks().get(0);
        }
        if (track instanceof InternalAudioTrack) {
            processDelegate((InternalAudioTrack) track, executor);
            return;
        }
        throw new SpotifyTrackNotFoundException();
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return this.spotifySourceManager;
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new SpotifyTrack(this.trackInfo, this.isrc, this.artworkURL, this.spotifySourceManager);
    }
}
