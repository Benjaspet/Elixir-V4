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
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.AudioUtil;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;

public final class SkipCommand implements ApplicationCommand {

    private final String name = "skip";
    private final String description = "Skip to the next track in the queue.";
    private final String[] options = {};
    private final String[] optionDescriptions = {};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        final TextChannel channel = event.getTextChannel();
        if(!AudioUtil.audioCheck(event, guild, member)) return;
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(member.getGuild());
        final AudioManager audioManager = channel.getGuild().getAudioManager();
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        if (musicManager.scheduler.queue.isEmpty()) {
            audioManager.closeAudioConnection();
            MessageEmbed embed = EmbedUtil.sendDefaultEmbed("There were no tracks left in the queue, so I left.");
            event.replyEmbeds(embed).queue();
            return;
        }
        if (audioPlayer.getPlayingTrack() == null) {
            MessageEmbed embed = EmbedUtil.sendErrorEmbed("There is no track currently playing.");
            event.replyEmbeds(embed).queue();
        }
        musicManager.scheduler.nextTrack();
        MessageEmbed embed = EmbedUtil.sendDefaultEmbed("Skipping to the next track...");
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
        return new CommandData(this.name, this.description);
    }
}
