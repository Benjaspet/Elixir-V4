/*
 *
 *  * Copyright © 2022 Ben Petrillo. All rights reserved.
 *  *
 *  * Project licensed under the MIT License: https://www.mit.edu/~amini/LICENSE.md
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 *  * OR OTHER DEALINGS IN THE SOFTWARE.
 *  *
 *  * All portions of this software are available for public use, provided that
 *  * credit is given to the original author(s).
 *  
 */

package dev.benpetrillo.elixir.utilities;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public final class Utilities {

    /**
     * Determine if a URL is valid.
     * @param input The URL to check.
     * @return boolean
     */

    public static boolean isValidURL(String input) {
        try {
            new URL(input);
            return true;
        }
        catch (MalformedURLException e){
            return false;
        }
    }

    /**
     * Encode a specific URI component.
     * @param str The string to encode.
     * @return String
     */

    public static String encodeURIComponent(String str) {
        String result;
        result = URLEncoder.encode(str, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\%7E", "~");
        return result;
    }

    /**
     * Convert ms to a formatted timestamp.
     * @param ms The amount of milliseconds.
     * @return String
     */

    public static String formatDuration(long ms) {
        final long hours = ms / TimeUnit.HOURS.toMillis(1);
        final long minutes = ms / TimeUnit.MINUTES.toMillis(1);
        final long seconds = ms / TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
