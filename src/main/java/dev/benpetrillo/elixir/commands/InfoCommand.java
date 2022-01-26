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

import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

import java.time.OffsetDateTime;

public final class InfoCommand extends Command {
    public InfoCommand() {
        super("info", "Get information about Elixir.");
    }
    
    @Override
    public void execute(Interaction interaction) {
        var streams = 0; for(GuildMusicManager musicManager : ElixirMusicManager.getInstance().getMusicManagers())
            streams += musicManager.audioPlayer.getPlayingTrack() != null ? 1 : 0;
        var servers = ElixirClient.getInstance().jda.getGuilds().size();
        var users = 0; for(Guild server : ElixirClient.getInstance().jda.getGuilds())
            users += server.getMemberCount();
        var uptime = Utilities.formatDuration(1000 * (OffsetDateTime.now().toEpochSecond() - ElixirClient.startTime.toEpochSecond()));

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedUtil.getDefaultEmbedColor())
                .setAuthor("Total Playing Streams: " + streams)
                .addField("Elixir | Information", """
                        %s Powered By: JDA 5.0.0-alpha.3
                        %s Server count: %s
                        %s User count: %s
                        %s Uptime: %s
                        """.formatted(
                        Config.get("EMOJI-LIBRARIES"), Config.get("EMOJI-SERVERS"), servers,
                        Config.get("EMOJI-USERS"), users, Config.get("EMOJI-UPTIME"), uptime
                ), false)
                .setFooter("Elixir Music", ElixirClient.getInstance().jda.getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(OffsetDateTime.now());
        interaction.reply(embed.build());
    }
}
