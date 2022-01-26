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

package dev.benpetrillo.elixir.commands.playlist;

import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.music.TrackScheduler;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.PlaylistUtil;
import dev.benpetrillo.elixir.utilities.TrackUtil;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.SubCommand;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QueueSubCommand extends SubCommand implements Arguments {
    public QueueSubCommand() {
        super("queue", "Queue a track to the playlist.");
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.deferReply();
        var member = interaction.getMember(); var guild = interaction.getGuild();

        var playlistId = (String) interaction.getArguments().getOrDefault("id", "test");
        CustomPlaylist playlist = PlaylistUtil.findPlaylist(playlistId);
        if(playlist == null) {
            interaction.reply(EmbedUtil.sendErrorEmbed("Unable to find a playlist of id `" + playlistId + "`."));
            return;
        }
        
        final GuildVoiceState memberVoiceState = member.getVoiceState(); assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            interaction.reply(EmbedUtil.sendErrorEmbed("You must be in a voice channel to queue tracks."));
            return;
        }
        final GuildVoiceState voiceState = guild.getSelfMember().getVoiceState(); assert voiceState != null;
        final AudioManager audioManager = guild.getAudioManager();
        final AudioChannel audioChannel = Objects.requireNonNull(member.getVoiceState()).getChannel();
        if (!voiceState.inAudioChannel()) {
            audioManager.openAudioConnection(audioChannel);
            audioManager.setSelfDeafened(true);
        }
        GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(guild);
        var tracks = PlaylistUtil.getTracks(playlist); TrackUtil.appendUser(member.getId(), tracks);
        if (musicManager.scheduler.queue.isEmpty() && musicManager.audioPlayer.getPlayingTrack() == null) {
            musicManager.scheduler.repeating = playlist.options.repeat
                    ? TrackScheduler.LoopMode.QUEUE : TrackScheduler.LoopMode.NONE;
            if (playlist.options.shuffle) Collections.shuffle(tracks);
        }
        musicManager.scheduler.getQueue().addAll(tracks);
        if (musicManager.audioPlayer.getPlayingTrack() == null) musicManager.scheduler.nextTrack();
        interaction.reply(EmbedUtil.sendDefaultEmbed("Queued **%s** tracks from %s.".formatted(playlist.tracks.size(), playlist.info.name)));
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
                Argument.create("id", "The playlist ID.", "id", OptionType.STRING, true, 0)
        );
    }
}
