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

package dev.benpetrillo.elixir.tasks;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/*
 * Developer note: this task exists because of a bug that exists within Discord
 * bot application activities; they tend to disappear after extended periods of
 * time (i.e. two weeks). This bug may or may not still exist™.
 */

public final class StatusSetTask extends TimerTask {

    public static void schedule() {
        new Timer().schedule(new StatusSetTask(), 0L, TimeUnit.MINUTES.toMillis(120));
    }

    @Override
    public void run() {
        try {
            ElixirClient.getJda().getPresence().setActivity(Activity.listening(ElixirConstants.ACTIVITY));
        } catch (Error e) {
            ElixirClient.logger.debug(e.getMessage());
        }
    }
}

