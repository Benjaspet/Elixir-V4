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

package dev.benpetrillo.elixir.utilities;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.types.ExtendedAudioTrackInfo;
import dev.benpetrillo.elixir.types.YTVideoData;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class TrackUtil {

    /**
     * Returns a URL of a track/video's cover art/thumbnail.
     * @param track The AudioTrack to fetch.
     * @return String
     */
    
    public static String getCoverArt(AudioTrackInfo track) {
        var trackUri = track.uri;
        if (trackUri.contains("spotify") && trackUri.contains("/track/")) {
            try {
                String[] firstSplit = trackUri.split("/");
                String[] secondSplit; String id;
                if (firstSplit.length > 5) {
                    secondSplit = firstSplit[6].split("\\?");
                } else {
                    secondSplit = firstSplit[4].split("\\?");
                }
                id = secondSplit[0];
                GetTrackRequest trackRequest = SpotifySourceManager.getSpotify().getTrack(id).build();
                Track spotifyTrack = trackRequest.execute();
                Image thumbnail = spotifyTrack.getAlbum().getImages()[0];
                return thumbnail.getUrl();
            } catch (SpotifyWebApiException | IOException | ParseException | NullPointerException exception) {
                exception.printStackTrace();
                return null;
            }
        } else if (trackUri.contains("www.youtube.com") || trackUri.contains("youtu.be")) {
            final YTVideoData data = HttpUtil.getVideoData(Utilities.extractVideoId(track.uri));
            return data != null ? data.items.get(0).snippet.thumbnails.get("default").get("url") : null;
        }
        return null;
    }

    /**
     * Get track data from a Spotify URL.
     * @param url The Spotify URL.
     * @return Track
     */
    
    public static Track getTrackDataFromSpotifyURL(String url) {
        try {
            String[] firstSplit = url.split("/");
            String[] secondSplit; String id;
            if (firstSplit.length > 5) {
                secondSplit = firstSplit[6].split("\\?");
            } else {
                secondSplit = firstSplit[4].split("\\?");
            }
            id = secondSplit[0];
            GetTrackRequest trackRequest = SpotifySourceManager.getSpotify().getTrack(id).build();
            return trackRequest.execute();
        } catch (SpotifyWebApiException | IOException | ParseException | NullPointerException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Creates an {@link AudioTrackInfo} object from a given URL.
     * @param url The URL to fetch information from.
     * @return An AudioTrackInfo object.
     */
    
    @Nullable
    public static AudioTrackInfo getTrackInfoFromUrl(String url) {
        TrackType type = TrackUtil.determineTrackType(url);
        switch (type) {
            case SPOTIFY -> {
                Track track = TrackUtil.getTrackDataFromSpotifyURL(url);
                if (track == null) return null;
                
                var trackInfo = new ExtendedAudioTrackInfo(
                        track.getName(), track.getArtists()[0].getName(),
                        track.getDurationMs(), track.getId(), false, 
                        track.getHref()
                ); 
                trackInfo.isrc = track.getExternalIds().getExternalIds().getOrDefault("isrc", null);
                return trackInfo;
            }
            case YOUTUBE -> {
                YTVideoData searchData = HttpUtil.getVideoData(Utilities.extractVideoId(url));
                if (searchData == null) return null;
                YTVideoData.Item.Snippet query = searchData.items.get(0).snippet;
                long length = Utilities.cleanYouTubeFormat(searchData.items.get(0).contentDetails.duration);
                return new AudioTrackInfo(
                        query.title, query.channelTitle, length,
                        searchData.items.get(0).id, false,
                        "https://youtu.be/" + searchData.items.get(0).id
                );
            }
            default -> {
                return null;
            }
        }
    }
    
    /**
     * Appends a user ID to a track.
     * @param userId The user ID to add to the track.
     * @param tracks The track(s) to append the user ID to.
     */
    
    public static void appendUser(String userId, List<PlaylistTrack> tracks) {
        for(AudioTrack track : tracks)
            track.setUserData(userId);
    }
    
    /**
     * Determines the source for a given URL.
     * @param url The URL to find the source of.
     * @return A {@link TrackType} representing the source of the URL.
     */
    
    public static TrackType determineTrackType(String url) {
        if (url.contains("youtu")) return TrackType.YOUTUBE;
        if (url.contains("spotify")) return TrackType.SPOTIFY;
        return TrackType.UNKNOWN;
    }
    
    public enum TrackType {
        YOUTUBE,
        SPOTIFY,
        UNKNOWN
    }
}
