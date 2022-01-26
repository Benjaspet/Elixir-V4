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
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.OffsetDateTime;

public final class InfoCommand implements ApplicationCommand {

    private final String name = "info";
    private final String description = "Get information about Elixir.";
    private final String[] options = {};
    private final String[] optionDescriptions = {};
    
    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        var streams = 0; for(GuildMusicManager musicManager : ElixirMusicManager.getInstance().getMusicManagers())
            streams += musicManager.audioPlayer.getPlayingTrack() != null ? 1 : 0;
        var servers = event.getJDA().getGuilds().size();
        var users = 0; for(Guild server : event.getJDA().getGuilds()) 
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
                .setFooter("Elixir Music", event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setTimestamp(OffsetDateTime.now());
        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData(this.name, this.description);
    }
}
