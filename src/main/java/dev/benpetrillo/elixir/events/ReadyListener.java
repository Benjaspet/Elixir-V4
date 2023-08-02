/*
 * Copyright © 2023 Ben Petrillo, KingRainbow44. All rights reserved.
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

package dev.benpetrillo.elixir.events;

import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        final JDA jda = event.getJDA();
        final String username = jda.getSelfUser().getEffectiveName();
        ElixirClient.getLogger().info("{} has logged in.", username);
        if (Boolean.parseBoolean(Config.get("DEPLOY-APPLICATION-COMMANDS-GUILD"))) {
            for (String id : ElixirConstants.GUILDS) {
                final Guild guild = jda.getGuildById(id);
                if (guild != null) {
                    ElixirClient.getCommandHandler().deployAll(guild);
                    ElixirClient.getLogger().info("All guild slash commands have been deployed.");
                } else {
                    ElixirClient.getLogger().error("An error occurred while deploying guild slash commands.");
                }
            }
        }




        for (String id : ElixirConstants.GUILDS) {
            final Guild guild = jda.getGuildById(id);
            if (guild != null) {
                if (Boolean.parseBoolean(Config.get("DEPLOY-APPLICATION-COMMANDS-GUILD"))) {
                    ElixirClient.getCommandHandler().deployAll(guild);
                    ElixirClient.getLogger().info("All guild slash commands have been deployed.");
                } else if (Boolean.parseBoolean(Config.get("DELETE-APPLICATION-COMMANDS-GUILD"))) {
                    ElixirClient.getCommandHandler().downsert(guild);
                    ElixirClient.getLogger().info("All guild slash commands have been deleted.");
                }
            } else {
                ElixirClient.getLogger().error("An error occurred while deploying guild slash commands.");
            }
        }
    }
}