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

public final class VolumeCommand extends Command implements Arguments {

    public VolumeCommand() {
        super("volume", "Set the volume of the player.");
    }

    @Override
    public void execute(Interaction interaction) {
        if (!AudioUtil.playerCheck(interaction, AudioUtil.ReturnMessage.NOT_PLAYING)) return;
        if (!AudioUtil.audioCheck(interaction)) return;
        int continueExec; if ((continueExec = DJUtil.continueExecution(interaction.getGuild(), interaction.getMember())) != -1) {
            interaction.reply(EmbedUtil.sendDefaultEmbed(continueExec + " more people is required to continue.")); return;
        }
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        final int volume = ((Number) interaction.getArguments().get("volume")).intValue();
        musicManager.audioPlayer.setVolume(volume);
        interaction.reply(EmbedUtil.sendDefaultEmbed("Volume set to **" + volume + "**."));
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
                Argument.create("volume", "Set the volume of the player.", "volume",
                        OptionType.INTEGER, true, 0).range(0, 150)
        );
    }
}
