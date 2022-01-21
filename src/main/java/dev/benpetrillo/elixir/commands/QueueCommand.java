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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public final class QueueCommand implements ApplicationCommand {
    
    private final String name = "queue";
    private final String description = "View all songs in the queue.";
    private final String[] options = {};
    private final String[] optionDescriptions = {};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(member.getGuild());
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if (musicManager.scheduler.queue.isEmpty()) {
            event.replyEmbeds(EmbedUtil.sendErrorEmbed("There are no songs in the queue.")).queue();
            return;
        }
        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.replyEmbeds(EmbedUtil.sendErrorEmbed("You must be in a voice channel to run this command.")).queue();
            return;
        }
        event.deferReply().queue(hook -> {
            try {
                final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
                final List<AudioTrack> arrayQueue = new ArrayList<>(queue);
                StringBuilder description = new StringBuilder();
                int maxAmount = Math.min(arrayQueue.size(), 12);
                for (int i = 0; i < maxAmount; i++) {
                    final AudioTrack track = arrayQueue.get(i);
                    final AudioTrackInfo info = track.getInfo();
                    String title = info.title.length() > 60 ? info.title.substring(0, 57) + "..." : info.title;
                    String formattedString = String.format("**#%s** - [%s](%s)", i + 1, title, info.uri);
                    description
                            .append(formattedString)
                            .append("\n");
                }
                if (arrayQueue.size() > maxAmount) {
                    description
                            .append("\n")
                            .append(String.format("...and %s more tracks.", arrayQueue.size() - maxAmount));
                }
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Guild Queue")
                        .setColor(EmbedUtil.getDefaultEmbedColor())
                        .setDescription(description)
                        .addField("Queue Data", String.format("• Tracks queued: %s", queue.size()), false)
                        .setFooter("Elixir Music", event.getJDA().getSelfUser().getAvatarUrl())
                        .setTimestamp(new Date().toInstant())
                        .build();
                hook.editOriginalEmbeds(embed).queue();
            } catch (PermissionException ignored) {
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("An error occurred while running this command.")).queue();
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
