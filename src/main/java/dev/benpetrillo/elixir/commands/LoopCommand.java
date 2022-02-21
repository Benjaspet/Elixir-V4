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
import dev.benpetrillo.elixir.utilities.AudioUtil;
import dev.benpetrillo.elixir.utilities.DJUtil;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.List;

public final class LoopCommand extends Command implements Arguments {

    public LoopCommand() {
        super("loop", "Loop a song or queue.");
    }

    @Override
    public void execute(Interaction interaction) {
        var mode = (String) interaction.getArguments().getOrDefault("mode", "Disable Loop");
        if (!AudioUtil.audioCheck(interaction)) return;
        if (!AudioUtil.playerCheck(interaction, AudioUtil.ReturnMessage.NOT_PLAYING)) return;
        int continueExec; if((continueExec = DJUtil.continueExecution(interaction.getGuild(), interaction.getMember())) != -1) {
            interaction.reply(EmbedUtil.sendDefaultEmbed(continueExec + " more people is required to continue.")); return;
        }
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        final TrackScheduler scheduler = musicManager.scheduler;
        switch (mode) {
            case "Track Loop" -> {
                scheduler.repeating = TrackScheduler.LoopMode.TRACK;
                mode = "track";
            }
            case "Queue Loop" -> {
                scheduler.repeating = TrackScheduler.LoopMode.QUEUE;
                mode = "queue";
            }
            case "Disable Loop" -> {
                scheduler.repeating = TrackScheduler.LoopMode.NONE;
                interaction.reply(EmbedUtil.sendDefaultEmbed("Turned **off** repeat mode."));
                return;
            }
            default -> {
                interaction.reply(EmbedUtil.sendErrorEmbed("Invalid mode."));
                return;
            }
        }
        interaction.reply(EmbedUtil.sendDefaultEmbed("Set the loop mode to **%s**.".formatted(mode)));
    }

    @Override
    public Collection<Argument> getArguments() {
        var argument = Argument.createWithChoices(
                "mode", "Loop mode", "mode",
                OptionType.STRING, true, 0,
                "Track Loop", "Queue Loop", "Disable Loop"
        ); argument.trailing = true;
        return List.of(argument);
    }
}
