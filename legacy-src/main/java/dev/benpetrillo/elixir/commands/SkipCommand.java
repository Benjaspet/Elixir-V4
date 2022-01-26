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
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.AudioUtil;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.TrackUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Date;
import java.util.Objects;

public final class SkipCommand implements ApplicationCommand {

    private final String name = "skip";
    private final String description = "Skip to a specified track in the queue.";
    private final String[] options = {"track"};
    private final String[] optionDescriptions = {"The track to skip to."};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        final TextChannel channel = event.getTextChannel();
        if (!AudioUtil.audioCheck(event, guild, member)) return;
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(member.getGuild());
        final AudioManager audioManager = channel.getGuild().getAudioManager();
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        if (musicManager.scheduler.queue.isEmpty()) {
            audioManager.closeAudioConnection();
            musicManager.scheduler.queue.clear();
            musicManager.audioPlayer.destroy();
            MessageEmbed embed = EmbedUtil.sendDefaultEmbed("There were no tracks left in the queue, so I left.");
            event.replyEmbeds(embed).queue();
            return;
        }
        if (audioPlayer.getPlayingTrack() == null) {
            MessageEmbed embed = EmbedUtil.sendErrorEmbed("There is no track currently playing.");
            event.replyEmbeds(embed).queue();
        }
        final OptionMapping mapping = event.getOption("track");
        final long skipTo = mapping == null ? 1 : mapping.getAsLong();
        assert musicManager.scheduler.queue.peek() != null;
        AudioTrack upNext = (AudioTrack) musicManager.scheduler.queue.toArray()[(int) (skipTo - 1)];
        for (int i = 0; i < skipTo; i++) {
            musicManager.scheduler.nextTrack();
        }
        if (audioPlayer.getPlayingTrack().getInfo().isStream) {
            musicManager.forceSkippedLivestream = true;
        }
        final String title = upNext.getInfo().title.length() > 60 ? upNext.getInfo().title.substring(0, 60) + "..." : upNext.getInfo().title;
        final String duration = Utilities.formatDuration(upNext.getDuration());
        final String isLive = upNext.getInfo().isStream ? "yes" : "no";
        final String artist = upNext.getInfo().author;
        final String url = upNext.getInfo().uri;
        final String requestedBy = member.getAsMention();
        final String contents = """
                            • Artist: %s
                            • Requested by: %s
                            • Duration: %s
                            • Livestream: %s
                            """.formatted(artist, requestedBy, duration, isLive);
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Up Next")
                .setDescription("[%s](%s)".formatted(title, url))
                .setColor(EmbedUtil.getDefaultEmbedColor())
                .setThumbnail(TrackUtil.getCoverArt(upNext.getInfo()))
                .addField("Track Data", contents, false)
                .setFooter("Elixir Music", event.getJDA().getSelfUser().getAvatarUrl())
                .setTimestamp(new Date().toInstant())
                .build();
        event.replyEmbeds(embed).queue();
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
        return new CommandData(this.name, this.description)
                .addOption(OptionType.INTEGER, this.options[0], this.optionDescriptions[0], false);
    }
}