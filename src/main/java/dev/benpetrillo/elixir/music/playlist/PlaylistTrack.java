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

package dev.benpetrillo.elixir.music.playlist;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.music.spotify.SpotifyTrack;
import dev.benpetrillo.elixir.music.spotify.SpotifyTrackNotFoundException;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utilities.Utilities;

public final class PlaylistTrack extends DelegatedAudioTrack {

    private final String artworkUrl;
    private final AudioSourceManager sourceManager;
    private final CustomPlaylist.CustomPlaylistTrack trackObject;
    
    public PlaylistTrack(String title, CustomPlaylist.CustomPlaylistTrack from, AudioSourceManager sourceManager) {
        super(new AudioTrackInfo(
                title, from.artist, from.duration,
                from.url.contains("youtu") ? Utilities.extractVideoId(from.url) : Utilities.extractSongId(from.url),
                false, from.url
        ));
        this.artworkUrl = from.coverArt;
        this.sourceManager = sourceManager;
        this.trackObject = from;
    }
    
    public String getArtworkUrl() {
        return this.artworkUrl;
    }
    
    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        if (this.getInfo().uri.contains("youtu")) { // YouTube track.
            processDelegate(new YoutubeAudioTrack(
                    this.trackInfo, (YoutubeAudioSourceManager) this.sourceManager
            ), executor);
        } else { // Spotify track.
            processDelegate(new SpotifyTrack(
                    this.trackInfo, Utilities.extractSongId(this.getInfo().uri), getArtworkUrl(),
                    new SpotifySourceManager(this.sourceManager)
            ), executor);
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new PlaylistTrack(this.trackInfo.title, this.trackObject, this.sourceManager);
    }
}
