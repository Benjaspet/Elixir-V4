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

import com.google.gson.Gson;
import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.types.YTPlaylistData;
import dev.benpetrillo.elixir.types.YTSearchData;
import dev.benpetrillo.elixir.types.YTVideoData;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public final class HttpUtil {

    /**
     * Get a YouTube video URL from a search query.
     * @param query The search query.
     * @return String
     * @throws UnsupportedEncodingException If the input isn't encodable.
     */

    public static String getYouTubeURL(String query) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();
        String encodedQuery = Utilities.encodeURIComponent(query);
        String url = "https://www.googleapis.com/youtube/v3/search?key=" +
                Config.get("YOUTUBE-API-KEY") +
                "&type=video&part=snippet&max=5&q=" +
                encodedQuery;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            var requestData = new Gson().fromJson(response.body().string(), YTSearchData.class);
            return "https://www.youtube.com/watch?v=" + requestData.items.get(0).id.get("videoId");
        } catch (IOException ex) {
            ex.printStackTrace();
            return "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        }
    }

    /**
     * Get data on a particular YouTube video by ID.
     * @param videoId The video ID.
     * @return YTVideoData
     */
    
    @Nullable
    public static YTVideoData getVideoData(String videoId) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://www.googleapis.com/youtube/v3/videos?key=" +
                Config.get("YOUTUBE-API-KEY") +
                "&part=snippet%2CcontentDetails&id=" +
                videoId;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            return new Gson().fromJson(response.body().string(), YTVideoData.class);
        } catch (IOException ex) {
            ex.printStackTrace(); return null;
        }
    }

    /**
     * Get data from a YouTube playlist by its ID.
     * @param playlistId The playlist ID.
     * @return YTVideoData
     */
    
    public static YTVideoData getPlaylistData(String playlistId) {
        boolean lastPage = false; String nextPageToken = null;
        List<YTPlaylistData> totalData = new ArrayList<>();
        while (!lastPage) {
            OkHttpClient client = new OkHttpClient();
            String url = "https://www.googleapis.com/youtube/v3/playlistItems?key=" + ElixirConstants.YOUTUBE_API_KEY +
                    "&part=snippet%2CcontentDetails&maxResults=50&playlistId=" + playlistId;
            if (nextPageToken != null) url += "&pageToken=" + nextPageToken;
            Request request = new Request.Builder()
                    .url(url).build();
            try (Response response = client.newCall(request).execute()) {
                assert response.body() != null;
                var playlistData = new Gson().fromJson(response.body().string(), YTPlaylistData.class);
                totalData.add(playlistData);
                if(playlistData.nextPageToken != null) {
                    nextPageToken = playlistData.nextPageToken;
                } else lastPage = true;
            } catch (IOException ignored) { lastPage = true; }
        }

        var videoData = new YTVideoData();
        videoData.items = new ArrayList<>();
        for(var playlistData : totalData) {
            for(var playlistItem : playlistData.items) {
                //noinspection ConstantConditions
                videoData.items.add(HttpUtil.getVideoData(
                        playlistItem.snippet.resourceId.get("videoId"))
                        .items.get(0));
            }
        }
        return videoData;
    }
}