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
import dev.benpetrillo.elixir.music.TrackScheduler;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.AudioUtil;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Objects;

public final class LoopCommand implements ApplicationCommand {

    private final String name = "loop";
    private final String description = "Loop a song or queue.";
    private final String[] options = {"mode"}; 
    private final String[] optionDescriptions = {"The type of loop to apply."};
    
    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        if (!AudioUtil.audioCheck(event, guild, member)) return;
        if (!AudioUtil.playerCheck(event, guild, AudioUtil.ReturnMessage.NOT_PLAYING)) return;
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(guild);
        final TrackScheduler scheduler = musicManager.scheduler;
        String loop = Objects.requireNonNull(event.getOption("mode")).getAsString(); String mode;
        switch(loop) {
            case "Track Loop":
                scheduler.repeating = TrackScheduler.LoopMode.TRACK; mode = "track";
                break;
            case "Queue Loop":
                scheduler.repeating = TrackScheduler.LoopMode.QUEUE; mode = "queue";
                break;
            case "Disable Loop":
                scheduler.repeating = TrackScheduler.LoopMode.NONE;
                event.replyEmbeds(EmbedUtil.sendDefaultEmbed("Turned **off** repeat mode.")).queue();
                return;
            default:
                return;
        }
        event.replyEmbeds(EmbedUtil.sendDefaultEmbed("Set the loop mode to **%s**.".formatted(mode))).queue();
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
                .addOptions(new OptionData(OptionType.STRING, this.options[0], this.optionDescriptions[0], true)
                        .addChoice("Track Loop", "Track Loop")
                        .addChoice("Queue Loop", "Queue Loop")
                        .addChoice("Disable Loop", "Disable Loop"));
    }
}
