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

package dev.benpetrillo.elixir.commands;

import com.sun.management.OperatingSystemMXBean;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.OffsetDateTime;

public final class InfoCommand extends Command {

    public InfoCommand() {
        super("info", "Get information about Elixir.");
    }
    
    @Override
    public void execute(Interaction interaction) {
        var streams = 0; var users = 0;
        var servers = ElixirClient.getInstance().jda.getGuilds().size();
        for (GuildMusicManager musicManager : ElixirMusicManager.getInstance().getMusicManagers()) {
            streams += musicManager.audioPlayer.getPlayingTrack() != null ? 1 : 0;
        }
        for (Guild server : ElixirClient.getInstance().jda.getGuilds()) {
            users += server.getMemberCount();
        }
        // get java process memory usage
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        long memory = os.getTotalMemorySize() / 1024 / 1024;
        long freeMemory = os.getFreeMemorySize() / 1024 / 1024;
        // get java process cpu usage and round it to 4 decimal places
        double cpuUsage = Math.round(os.getProcessCpuLoad() * 100 * 10000.0) / 10000.0;
        // get cpu cores
        int cores = Runtime.getRuntime().availableProcessors();
        // get java process threads
        int threads = Thread.activeCount();
        //get java process uptime
        long uptime = runtime.getUptime();
        // get uptime in days, hours, minutes, and seconds
        long days = uptime / 86400000;
        long hours = (uptime % 86400000) / 3600000;
        long minutes = (uptime % 3600000) / 60000;
        long seconds = (uptime % 60000) / 1000;
        String uptimeString = "%dd %dh %dm %ds".formatted(days, hours, minutes, seconds);
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                .setAuthor("Total Playing Streams: " + streams)
                .setDescription("[Invite Elixir to your server!](" + ElixirConstants.INVITE + ")")
                .addField("Bot Information", """
                        • Powered by: JDA 5.0.0-alpha.6
                        • Developed by: Ponjo Studios
                        • Server count: %s
                        • User count: %s
                        • Uptime: %s
                        """.formatted(servers, users, uptimeString), false)
                .addField("Host Information", """
                        • CPU Usage: %s%%
                        • CPU Cores: %s
                        • Threads: %s
                        • Total Memory: %s MB
                        """.formatted(cpuUsage, cores, threads, memory), false)
                .setFooter("Elixir Music", ElixirClient.getInstance().jda.getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(OffsetDateTime.now());
        interaction.reply(embed.build(), false);
    }
}