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
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
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

    /**
     *
     * @param url The playlist URL to fetch.
     * @return List<String>
     * @throws ParseException If the URL is invalid.
     * @throws SpotifyWebApiException If something goes wrong on Spotify's side.
     * @throws IOException If all else fails.
     */

    public List<String> queueSpotifyTracks(String url) throws ParseException, SpotifyWebApiException, IOException {
        String[] firstSplit = url.split("/");
        String[] secondSplit;
        String type;
        if (firstSplit.length > 5) {
            secondSplit = firstSplit[6].split("\\?");
            type = firstSplit[5];
        } else {
            secondSplit = firstSplit[4].split("\\?");
            type = firstSplit[3];
        }
        String id = secondSplit[0];
        List<String> listOfTracks = new ArrayList<>();
        if (type.contentEquals("track")) {
            listOfTracks.add(getTrackData(id));
            return listOfTracks;
        }
        if (type.contentEquals("playlist")) {
            GetPlaylistsItemsRequest playlistRequest = spotifyApi.getPlaylistsItems(id).build();
            final Paging<PlaylistTrack> playlist = playlistRequest.execute();
            PlaylistTrack[] playlistTracks = playlist.getItems();
            for (PlaylistTrack track : playlistTracks) {
                Track fullTrack = (Track) track.getTrack();
                String trackName = fullTrack.getName();
                String trackArtist = fullTrack.getArtists()[0].getName();
                String searchQuery = trackArtist + " - " + trackName;
                listOfTracks.add("ytsearch:" + searchQuery);
            }
            final int playlistLength = playlist.getTotal();
            if (playlistLength < 100) return listOfTracks;
            if (playlist.getTotal() > 100) {
                int offsetAmount = 100;
                int tracker = 100;
                while (true) {
                    GetPlaylistsItemsRequest secondPlaylistRequest = spotifyApi.getPlaylistsItems(id).offset(offsetAmount).build();
                    final Paging<PlaylistTrack> secondPlaylist = secondPlaylistRequest.execute();
                    PlaylistTrack[] additionalTracks = secondPlaylist.getItems();
                    for (PlaylistTrack track : additionalTracks) {
                        Track fullTrack = (Track) track.getTrack();
                        String trackName = fullTrack.getName();
                        String trackArtist = fullTrack.getArtists()[0].getName();
                        String searchQuery = trackArtist + " - " + trackName;
                        listOfTracks.add("ytsearch:" + searchQuery);
                    }
                    offsetAmount += 100; tracker += additionalTracks.length;
                    if (tracker >= playlistLength) {
                        return listOfTracks;
                    }
                }
            }
            return listOfTracks;
        }
        return null;
    }

    /**
     * Get the track name, artist, and album name as a string.
     * @param trackID The ID of the track to fetch.
     * @return String
     * @throws ParseException If the ID is invalid.
     * @throws SpotifyWebApiException If an error occurs on Spotify's end.
     * @throws IOException If all else fails.
     */

    private String getTrackData(String trackID) throws ParseException, SpotifyWebApiException, IOException {
        StringBuilder fullSearchQuery;
        GetTrackRequest trackRequest = spotifyApi.getTrack(trackID).build();
        Track track = trackRequest.execute();
        String trackName = track.getName();
        String artistName = track.getArtists()[0].getName();
        String albumName = track.getAlbum().getName();
        fullSearchQuery = new StringBuilder(trackName + " - " + artistName + " " + albumName);
        ArtistSimplified[] artists = track.getArtists();
        for (ArtistSimplified i : artists) {
            fullSearchQuery.append(i.getName()).append(" ");
        }
        return fullSearchQuery.toString();
    }

    public static SpotifyURLConverter getInstance() {
        return instance;
    }
}