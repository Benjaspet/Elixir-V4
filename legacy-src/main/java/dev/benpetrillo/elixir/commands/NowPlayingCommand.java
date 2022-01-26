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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.TrackUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Date;

public final class NowPlayingCommand implements ApplicationCommand {

    private final String name = "nowplaying";
    private final String description = "View information on the track currently playing.";
    private final String[] options = {};
    private final String[] optionDescriptions = {};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        event.deferReply().queue(hook -> {
            try {
                final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(member.getGuild());
                final AudioPlayer audioPlayer = musicManager.audioPlayer;
                final AudioTrack track = audioPlayer.getPlayingTrack();
                if (track == null) {
                    hook.editOriginalEmbeds(
                            EmbedUtil.sendErrorEmbed("There is no track playing at the moment.")
                    ).queue();
                } else {
                    final AudioTrackInfo info = track.getInfo();
                    final String thumbnail = TrackUtil.getCoverArt(track.getInfo());
                    final String title = info.title.length() > 60 ? info.title.substring(0, 60) + "..." : info.title;
                    final String duration = Utilities.formatDuration(track.getPosition()) + "/" + Utilities.formatDuration(track.getDuration());
                    final String isLive = info.isStream ? "yes" : "no";
                    final String artist = info.author;
                    final String url = info.uri;
                    final String requestedBy = "<@" + track.getUserData(String.class) + ">";
                    final String contents = """
                            • Artist: %s
                            • Requested by: %s
                            • Duration: %s
                            • Livestream: %s
                            """.formatted(artist, requestedBy, duration, isLive);
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Currently Playing")
                            .setDescription(String.format("[%s](%s)", title, url))
                            .setColor(EmbedUtil.getDefaultEmbedColor())
                            .addField("Track Data", contents, false)
                            .setFooter("Elixir Music", event.getJDA().getSelfUser().getAvatarUrl())
                            .setTimestamp(new Date().toInstant())
                            .setThumbnail(thumbnail)
                            .build();
                    hook.editOriginalEmbeds(embed).queue();
                }
            } catch (PermissionException ignored) {
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("I do not have permission to do this.")).queue();
            } catch (Exception exception) {
                ElixirClient.logger.error("Error occurred while getting the currently playing track.", exception);
                Utilities.throwThrowable(new ElixirException(guild, member).exception(exception).additionalInformation("Not a permission exception."));
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("An unknown error occurred.")).queue();
            }
        });
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