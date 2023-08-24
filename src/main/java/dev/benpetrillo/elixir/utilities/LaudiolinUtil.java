package dev.benpetrillo.elixir.utilities;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.types.laudiolin.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;

/**
 * Methods to interface with the Laudiolin backend.
 */
public interface LaudiolinUtil {
    /* HTTP client for this utility. */
    String ENDPOINT = "http://localhost:3001";
    OkHttpClient CLIENT = HttpUtil.getClient();

    /**
     * Fetches information about a track.
     * @param trackId The ID of the track.
     * @return The track information.
     */
    static LaudiolinTrackInfo fetch(String trackId) {
        var request = new Request.Builder()
            .url(ENDPOINT + "/fetch/" + trackId)
            .build();

        try (var response = CLIENT.newCall(request).execute()) {
            var body = response.body();
            if (body == null)
                throw new IOException("No response body.");

            return Utilities.deserialize(body.string(), LaudiolinTrackInfo.class);
        } catch (IOException ex) {
            ElixirClient.getLogger().warn("An error occurred while trying to query the backend.", ex);
        }

        return null;
    }

    /**
     * Fetches a Laudiolin playlist.
     *
     * @param playlistId The ID of the playlist.
     * @return The playlist.
     */
    static LaudiolinPlaylist fetchPlaylist(String playlistId) {
        var request = new Request.Builder()
            .url(ENDPOINT + "/playlist/" + playlistId)
            .build();

        try (var response = CLIENT.newCall(request).execute()) {
            var body = response.body();
            if (body == null)
                throw new IOException("No response body.");

            return Utilities.deserialize(body.string(), LaudiolinPlaylist.class);
        } catch (IOException ex) {
            ElixirClient.getLogger().warn("An error occurred while trying to query the backend.", ex);
        }

        return null;
    }

    /**
     * Performs a search for the specified track.
     * @param query The query to search for.
     * @return The search results.
     */
    static LaudiolinSearchResults search(String query) {
        var request = new Request.Builder()
            .url(ENDPOINT + "/search/" + query + "?engine=All")
            .build();

        try (var response = CLIENT.newCall(request).execute()) {
            var body = response.body();
            if (body == null)
                throw new IOException("No response body.");

            return Utilities.deserialize(body.string(), LaudiolinSearchResults.class);
        } catch (IOException ex) {
            ElixirClient.getLogger().warn("An error occurred while trying to query the backend.", ex);
        }

        return null;
    }
}
