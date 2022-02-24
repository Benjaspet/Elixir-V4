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
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.AudioUtil;
import dev.benpetrillo.elixir.utilities.DJUtil;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ShuffleCommand extends Command {

    public ShuffleCommand() {
        super("shuffle", "Shuffle the current queue.");
    }

    @Override
    public void execute(Interaction interaction) {
        if (!interaction.isFromGuild()) {
            interaction.reply(EmbedUtil.sendErrorEmbed("This command can only be used in a guild."));
            return;
        }
        if (!AudioUtil.audioCheck(interaction)) return;
        int continueExec; if ((continueExec = DJUtil.continueExecution(interaction.getGuild(), interaction.getMember())) != -1) {
            interaction.reply(EmbedUtil.sendDefaultEmbed(continueExec + " more people are required to continue."), false);
            return;
        }
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        List<AudioTrack> tracks = new ArrayList<>(musicManager.scheduler.queue);
        Collections.shuffle(tracks);
        musicManager.scheduler.queue.clear();
        musicManager.scheduler.queue.addAll(tracks);
        interaction.reply(EmbedUtil.sendDefaultEmbed("Shuffled the queue."), false);
    }
}
