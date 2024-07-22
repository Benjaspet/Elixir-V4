/*
 * Copyright © 2024 Ben Petrillo, KingRainbow44.
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
 * All portions of this software are available for public use,
 * provided that credit is given to the original author(s).
 */

package dev.benpetrillo.elixir.events;

import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.ElixirConstants;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Slf4j
public final class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        final JDA jda = event.getJDA();
        final String username = jda.getSelfUser().getEffectiveName();

        log.info("{} has logged in.", username);

        boolean deployGuild = Boolean.parseBoolean(Config.get("DEPLOY-APPLICATION-COMMANDS-GUILD"));
        boolean deleteGuild = Boolean.parseBoolean(Config.get("DELETE-APPLICATION-COMMANDS-GUILD"));

        if (deployGuild && deleteGuild) {
            log.warn("Cannot deploy and delete guild commands at the same time.");
        } else if (!deployGuild && !deleteGuild) {
            log.warn("No action was specified for guild commands.");
        } else {

            for (String guildId : ElixirConstants.GUILDS) {
                final Guild guild = jda.getGuildById(guildId);
                if (guild != null) {
                    if (deployGuild) {
                        ElixirClient.getCommandHandler().deployAll(guild);
                        log.info("Guild slash commands deployed to: {}", guild.getId());
                    } else {
                        ElixirClient.getCommandHandler().downsert(guild);
                        log.info("Guild slash commands deleted from: {}", guild.getId());
                    }
                } else {
                    log.warn("Could not deploy guild commands for guild with ID: {}.", guildId);
                }
            }
        }
    }
}
