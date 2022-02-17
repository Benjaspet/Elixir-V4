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

package dev.benpetrillo.elixir.managers;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;

public final class ConfigStartupManager {

    public static void checkAll() {
        if (ElixirConstants.TOKEN == null) {
            ElixirClient.logger.error("Invalid bot token provided.");
            System.exit(0);
        } else if (ElixirConstants.YOUTUBE_API_KEY == null) {
            ElixirClient.logger.error("Invalid YouTube API key provided.");
            System.exit(0);
        } else if (ElixirConstants.MONGO_URI == null) {
            ElixirClient.logger.error("Invalid MongoDB URI provided.");
            System.exit(0);
        } else if (ElixirConstants.SPOTIFY_CLIENT_ID == null) {
            ElixirClient.logger.error("Invalid Spotify client ID provided.");
            System.exit(0);
        } else if (ElixirConstants.SPOTIFY_CLIENT_SECRET == null) {
            ElixirClient.logger.error("Invalid Spotify client secret provided.");
            System.exit(0);
        }
    }
}