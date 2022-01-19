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

import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Objects;

public final class ResumeCommand implements ApplicationCommand {

    private final String name = "resume";
    private final String description = "Resume the queue playback.";
    private final String[] options = {};
    private final String[] optionDescriptions = {};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        final TextChannel channel = event.getTextChannel();
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(EmbedUtil.sendErrorEmbed("I must be in a voice channel.")).queue();
            return;
        }
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.replyEmbeds(EmbedUtil.sendErrorEmbed("You must be in a voice channel.")).queue();
            return;
        }
        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            event.replyEmbeds(EmbedUtil.sendErrorEmbed("You need to be in my voice channel.")).queue();
            return;
        }
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(member.getGuild());
        if (musicManager.scheduler.player.isPaused()) {
            musicManager.scheduler.player.setPaused(false);
            MessageEmbed embed = EmbedUtil.sendDefaultEmbed("Successfully resumed the queue.");
            event.replyEmbeds(embed).queue();
        } else {
            MessageEmbed embed = EmbedUtil.sendErrorEmbed("The queue is already playing.");
            event.replyEmbeds(embed).queue();
        }
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
