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

package dev.benpetrillo.elixir.music;

import dev.benpetrillo.elixir.Config;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SpotifyURLConverter {

    private static SpotifyURLConverter instance;
    private SpotifyApi spotifyApi;


    public SpotifyURLConverter() {
        try {
            initialize();
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Initialize requests with the Spotify Web API and obtain an OAuth token.
     * @throws ParseException If invalid API credentials are passed.
     * @throws SpotifyWebApiException If an error occurs on Spotify's end.
     * @throws IOException If all else fails.
     */

    private void initialize() throws ParseException, SpotifyWebApiException, IOException {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(Config.get("SPOTIFY-CLIENT-ID"))
                .setClientSecret(Config.get("SPOTIFY-CLIENT-SECRET"))
                .build();
        ClientCredentialsRequest.Builder credRequest =
                new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
        ClientCredentials credentials = credRequest.grant_type("client_credentials").build().execute();
        spotifyApi.setAccessToken(credentials.getAccessToken());
    }

    public List<String> queueSpotifyTracks(String url) throws ParseException, SpotifyWebApiException, IOException {
        String[] firstSplit = url.split("/");
        String[] secondSplit;
        String type;
        String id;
        if (firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            type = firstSplit[5];
        } else {
            secondSplit = firstSplit[4].split("\\?");
            type = firstSplit[3];
        }
        id = secondSplit[0];
        List<String> listOfTracks = new ArrayList<>();
        if (type.contentEquals("track")) {
            listOfTracks.add(getArtistAndName(id));
            return listOfTracks;
        }
        if (type.contentEquals("playlist")) {
            String id1 = secondSplit[0];
            GetPlaylistRequest playlistRequest = spotifyApi.getPlaylist(id1).build();
            Playlist playlist = playlistRequest.execute();
            Paging<PlaylistTrack> playlistPaging = playlist.getTracks();
            PlaylistTrack[] playlistTracks = playlistPaging.getItems();
            for (PlaylistTrack track : playlistTracks) {
                listOfTracks.add("ytsearch:" + getArtistAndName(track.getTrack().getId()));
            }
            return listOfTracks;
        }
        return null;
    }

    /**
     * Get the artist and name of a track ID.
     * @param trackID The track ID whose data will be fetched.
     * @return String
     * @throws ParseException If an invalid ID was provided.
     * @throws SpotifyWebApiException If an error occurred on Spotify's end.
     * @throws IOException If all else fails.
     */

    private String getArtistAndName(String trackID) throws ParseException, SpotifyWebApiException, IOException {
        StringBuilder artistNameAndTrackName;
        GetTrackRequest trackRequest = spotifyApi.getTrack(trackID).build();
        Track track = trackRequest.execute();
        artistNameAndTrackName = new StringBuilder(track.getName() + " - ");
        ArtistSimplified[] artists = track.getArtists();
        for (ArtistSimplified i : artists) {
            artistNameAndTrackName.append(i.getName());
        }
        return artistNameAndTrackName.toString();
    }

    /**
     * Get an instance of this class.
     * @return SpotifyURLConverter
     */

    public static SpotifyURLConverter getInstance() {
        return instance;
    }
}