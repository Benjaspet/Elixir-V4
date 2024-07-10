/*
 * Copyright © 2022 Ben Petrillo, KingRanbow44. All rights reserved.
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

package dev.benpetrillo.elixir.objects;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public final class OAuthUpdateTask extends TimerTask {

    public static void schedule() {
        var delay = TimeUnit.MINUTES.toMillis(45);
        new Timer().schedule(new OAuthUpdateTask(), delay, delay);
    }

    @Override
    public void run() {
        try {
            SpotifySourceManager.authorize();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            ElixirClient.logger.debug(e.getMessage());
        }
    }
}
