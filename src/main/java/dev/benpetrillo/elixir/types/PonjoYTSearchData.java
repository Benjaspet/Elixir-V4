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

package dev.benpetrillo.elixir.types;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public final class PonjoYTSearchData {
    public Map<String, String> id = Map.of();
    public String url = "";
    public String title = "";
    public String description = "";
    public String duration_raw = "";
    public Snippet snippet = new Snippet();
    public String views = "";

    public static class Snippet {
        public String url = "";
        public String duration = "";
        public String publishedAt = "";
        public Thumbnails thumbnails = new Thumbnails();
        public String title = "";
        public String views = "";
    }

    public static class Thumbnails {
        public String id = "";
        public String url = "";
        @SerializedName("default") public Thumbnail default_ = new Thumbnail();
        public Thumbnail high = new Thumbnail();
        public int width = 0;
        public int height = 0;

        /**
         * Returns the better thumbnail.
         * @return The thumbnail's URL.
         */
        public String getBetterThumbnail() {
            if(this.high.url.isEmpty()) {
                return this.default_.url;
            } else {
                return this.high.url;
            }
        }
    }

    public static class Thumbnail {
        public String url = "";
        public int width = 0;
        public int height = 0;
    }
}