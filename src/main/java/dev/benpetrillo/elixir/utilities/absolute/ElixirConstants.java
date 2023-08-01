/*
 * Copyright © 2023 Ben Petrillo. All rights reserved.
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

package dev.benpetrillo.elixir.utilities.absolute;

import dev.benpetrillo.elixir.Config;

import java.awt.*;

public final class ElixirConstants {

    public static String TOKEN = Config.get("TOKEN");
    public static String ACTIVITY = Config.get("ACTIVITY");
    public static String BOT_ID = Config.get("BOT-ID");
    public static String INVITE = Config.get("INVITE");
    public static String YOUTUBE_API_KEY = Config.get("YOUTUBE-API-KEY");
    public static String MONGO_URI = Config.get("MONGO-URI");
    public static String SPOTIFY_CLIENT_ID = Config.get("SPOTIFY-CLIENT-ID");
    public static String SPOTIFY_CLIENT_SECRET = Config.get("SPOTIFY-CLIENT-SECRET");
    public static String API_ADDRESS = Config.get("API-ADDRESS");
    public static String API_PORT = Config.get("API-PORT");
    public static String IPV6_BLOCK = Config.get("IPV6-BLOCK");
    public static String COMMAND_PREFIX = Config.get("COMMAND-PREFIX");
    public static String[] GUILDS = Config.get("GUILDS").split(",");
    public static Color DEFAULT_EMBED_COLOR = Color.decode(Config.get("DEFAULT-EMBED-COLOR"));
    public static Color ERROR_EMBED_COLOR = Color.decode(Config.get("ERROR-EMBED-COLOR"));
}
