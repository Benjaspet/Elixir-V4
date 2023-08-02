/*
 * Copyright © 2023 Ben Petrillo, KingRainbow44. All rights reserved.
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

package dev.benpetrillo.elixir.commands.music;

import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.AudioUtil;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.managers.AudioManager;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

public final class StopCommand extends Command {

    public StopCommand() {
        super("stop", "Stop the current track & clear the queue.");
    }

    @Override
    public void execute(Interaction interaction) {
        if (!interaction.isFromGuild()) {
            interaction.reply(EmbedUtil.sendErrorEmbed("This command can only be used in a guild."));
            return;
        }
        assert interaction.getMember() != null;
        final GuildVoiceState selfVoiceState = interaction.getMember().getVoiceState();
        assert selfVoiceState != null;
        if (AudioUtil.audioCheck(interaction)) return;
        assert interaction.getGuild() != null;
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        final AudioManager audioManager = interaction.getGuild().getAudioManager();
        if (selfVoiceState.inAudioChannel()) {
            audioManager.closeAudioConnection();
        }
        musicManager.stop();
        interaction.reply(EmbedUtil.sendDefaultEmbed("The queue has been cleared and the player has been stopped."));
    }
}
