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
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.*;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public final class SkipCommand extends Command implements Arguments {

    public SkipCommand() {
        super("skip", "Skip to a specified track in the queue.");
    }

    @Override
    public void execute(Interaction interaction) {
        if (!interaction.isFromGuild()) {
            interaction.reply(EmbedUtil.sendErrorEmbed("This command can only be used in a guild."));
            return;
        }
        if (AudioUtil.audioCheck(interaction)) return;
        assert interaction.getGuild() != null;
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        final AudioManager audioManager = interaction.getGuild().getAudioManager();
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        if (musicManager.scheduler.queue.isEmpty()) {
            audioManager.closeAudioConnection();
            musicManager.scheduler.queue.clear();
            musicManager.audioPlayer.destroy();
            final MessageEmbed embed = EmbedUtil.sendDefaultEmbed("There were no tracks left in the queue, so I left.");
            interaction.reply(embed, false);
            return;
        }
        if (audioPlayer.getPlayingTrack() == null) {
            final MessageEmbed embed = EmbedUtil.sendErrorEmbed("There is no track currently playing.");
            interaction.reply(embed, false);
        }
        int continueExec; if ((continueExec = DJUtil.continueExecution(interaction.getGuild(), interaction.getMember())) != -1) {
            interaction.reply(EmbedUtil.sendDefaultEmbed(continueExec + " more people is required to continue."), false);
            return;
        }
        final long skipTo = (long) interaction.getArguments().getOrDefault("track", 1L);
        assert musicManager.scheduler.queue.peek() != null;
        final AudioTrack upNext = (AudioTrack) musicManager.scheduler.queue.toArray()[(int) (skipTo - 1)];
        for (int i = 0; i < skipTo; i++) {
            musicManager.scheduler.nextTrack();
        }
        final String title = upNext.getInfo().title.length() > 60 ? upNext.getInfo().title.substring(0, 60) + "..." : upNext.getInfo().title;
        final String duration = Utilities.formatDuration(upNext.getDuration());
        final String isLive = upNext.getInfo().isStream ? "yes" : "no";
        final String artist = upNext.getInfo().author;
        final String url = upNext.getInfo().uri;
        final String requestedBy = "<@" + upNext.getUserData(String.class) + ">";
        final String contents = """
                            • Artist: %s
                            • Requested by: %s
                            • Duration: %s
                            • Livestream: %s
                            """.formatted(artist, requestedBy, duration, isLive);
        final MessageEmbed embed = new EmbedBuilder()
                .setTitle("Up Next")
                .setDescription("[%s](%s)".formatted(title, url))
                .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                .setThumbnail(TrackUtil.getCoverArt(upNext.getInfo()))
                .addField("Track Data", contents, false)
                .setFooter("Elixir Music", ElixirClient.getJda().getSelfUser().getAvatarUrl())
                .setTimestamp(new Date().toInstant())
                .build();
        interaction.reply(embed, false);
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
                Argument.create("track", "The track to skip to.", "track", OptionType.INTEGER, false, 0)
        );
    }
}
