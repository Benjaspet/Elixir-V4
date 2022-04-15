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

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.List;

public final class MoveQueueCommand extends Command implements Arguments {
    public MoveQueueCommand() {
        super("movequeue", "Moves the server's existing queue to another server.");
    }

    @Override
    public void execute(Interaction interaction) {
        var guildId = interaction.getArgument("guild", String.class);
        var guild = ElixirClient.getJda().getGuildById(guildId);
        if(guild == null) {
            interaction.reply(EmbedUtil.sendErrorEmbed("The guild with the specified ID doesn't exist."));
            return;
        }
        var targetAudioManager = guild.getAudioManager();
        if(!targetAudioManager.isConnected()) {
            interaction.reply(EmbedUtil.sendErrorEmbed("Connect the bot to a voice channel before moving queues."));
            return;
        }
        var targetMusicManager = ElixirMusicManager.getInstance().getMusicManager(guild);
        var targetQueue = targetMusicManager.scheduler.queue;
        var targetPlayer = targetMusicManager.audioPlayer;
        if(!targetQueue.isEmpty() || targetPlayer.getPlayingTrack() != null) {
            interaction.reply(EmbedUtil.sendErrorEmbed(guild.getName() + "'s Elixir is in use!"));
            return;
        }
        var sourceMusicManager = ElixirMusicManager.getInstance().getMusicManager(interaction.getGuild());
        var sourceQueue = sourceMusicManager.scheduler.queue;
        var sourcePlayer = sourceMusicManager.audioPlayer;
        targetQueue.addAll(sourceQueue); 
        targetPlayer.setVolume(sourcePlayer.getVolume());
        var newTrack = sourcePlayer.getPlayingTrack().makeClone();
        newTrack.setPosition(sourcePlayer.getPlayingTrack().getPosition());
        targetPlayer.playTrack(newTrack);
        var sourceAudioManager = interaction.getGuild().getAudioManager();
        if(sourceAudioManager.isConnected()) {
            sourceMusicManager.stop();
            sourceAudioManager.closeAudioConnection();
        }
        interaction.reply(EmbedUtil.sendDefaultEmbed("Moved the queue to `" + guild.getName() + "`."));
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
                Argument.create("guild", "The guild to move the queue to.", "guild", OptionType.STRING, true, 0)
        );
    }
}
