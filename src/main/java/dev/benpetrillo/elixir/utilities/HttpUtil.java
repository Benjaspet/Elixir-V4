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

import dev.benpetrillo.elixir.Config;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class HttpUtil {

    public static String getYouTubeURL(String query) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();
        String encodedQuery = Utilities.encodeURIComponent(query);
        StringBuilder url = new StringBuilder()
                .append("https://www.googleapis.com/youtube/v3/search?key=")
                .append(Config.get("YOUTUBE-API-KEY"))
                .append("&type=video&part=snippet&max=5&q=")
                .append(encodedQuery);
        Request request = new Request.Builder()
                .url(String.valueOf(url))
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            JSONObject body = new JSONObject(response.body().string());
            JSONArray array = body.getJSONArray("items");
            JSONObject obj = (JSONObject) array.get(0);
            JSONObject obj2 = obj.getJSONObject("id");
            String obj3 = obj2.get("videoId").toString();
            return "https://www.youtube.com/watch?v=" + obj3;
        } catch (IOException ex) {
            ex.printStackTrace();
            return "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        }
    }

    private static String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
