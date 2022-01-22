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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.types.YTVideoData;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;

public final class TrackUtil {

    /**
     * Returns a URL of a track/video's cover art/thumbnail.
     * @param track The AudioTrack to fetch.
     * @return String
     */
    
    public static String getCoverArt(AudioTrack track) {
        String trackUri = track.getInfo().uri;
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
            final YTVideoData data = HttpUtil.getVideoData(Utilities.extractVideoId(track.getInfo().identifier));
            return data != null ? data.items.get(0).snippet.thumbnails.get("default").get("url") : null;
        }
        return null;
    }
}
