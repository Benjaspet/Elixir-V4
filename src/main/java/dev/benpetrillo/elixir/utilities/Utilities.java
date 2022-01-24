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

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

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
        result = URLEncoder.encode(str, StandardCharsets.UTF_8);
        return result;
    }

    /**
     * Convert ms to a formatted timestamp.
     * @param ms The amount of milliseconds.
     * @return String
     */

    public static String formatDuration(long ms) {
        final Duration duration = Duration.ofMillis(ms);
        final int hours = duration.toHoursPart();
        final int minutes = duration.toMinutesPart();
        final int seconds = duration.toSecondsPart();
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Extracts the video ID from a given URL.
     * @param url The YouTube URL to extract the video ID from.
     * @return A video ID.
     */
    
    public static String extractVideoId(String url) {
        String[] segments = url.split("/");
        return url.contains("youtu.be") ? segments[3] : segments[3].split("v=")[1];
    }

    /**
     * Extracts the song ID from a given URL.
     * @param url The Spotify URL to extract the song ID from.
     * @return A song ID.
     */
    
    public static String extractSongId(String url) {
        String[] segments = url.split("/");
        return segments[4].split("\\?")[0];
    }

    /**
     * Pretty prints a given string.
     * @param toPrint The string to format.
     * @return A pretty/formatted string.
     */
    
    public static String prettyPrint(String toPrint) {
        String pass1 = toPrint.toLowerCase();
        String[] lower = pass1.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String string : lower) {
            String pass2 = string.substring(0, 1).toUpperCase() + string.substring(1);
            builder.append(pass2).append(" ");
        } return builder.toString();
    }

    /**
     * Parses other strings into a boolean value.
     * @param toParse The string to parse.
     * @return A boolean, or false if unable to parse.
     */
    
    public static boolean parseBoolean(String toParse) {
            return toParse.equalsIgnoreCase("true") || toParse.equalsIgnoreCase("yes") || toParse.equalsIgnoreCase("1");
    }

    /**
     * Convert a given object to a JSON string.
     * @param json The object to convert.
     * @param klass The class of the object.
     * @param <T> The type of the object.
     * @return A de-serialized object.
     */
    
    public static <T> T deserialize(String json, Class <T> klass) {
        return new Gson().fromJson(json, klass);
    }

    /**
     * Convert a given object to a JSON string.
     * @param object The object to convert.
     * @return A serialized object.
     */
    
    public static String serialize(Object object) {
        return new Gson().toJson(object);
    }

    /**
     * Encode a string to Base64
     * @param toEncode The string to encode.
     * @return A Base64 encoded string.
     */
    
    public static String base64Encode(String toEncode) {
        return Base64.getUrlEncoder().encodeToString(toEncode.getBytes());
    }

    /**
     * Decode a Base64 string.
     * @param toDecode The string to decode.
     * @return A decoded string.
     */
    
    public static String base64Decode(String toDecode) {
        return new String(Base64.getUrlDecoder().decode(toDecode));
    }
}
