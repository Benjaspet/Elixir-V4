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
import dev.benpetrillo.elixir.music.spotify.SpotifySourceManager;
import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.HttpUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

public final class PlayCommand extends Command implements Arguments {

    public PlayCommand() {
        super("play", "Play a track with a link or query.");
    }

    @Override
    public void execute(Interaction interaction) {
        final MessageChannel channel = interaction.getChannel();
        final GuildVoiceState voiceState = interaction.getGuild().getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = interaction.getMember().getVoiceState();
        assert memberVoiceState != null; interaction.deferReply();
        if (!memberVoiceState.inAudioChannel()) {
            interaction.reply(EmbedUtil.sendErrorEmbed("You must be in a voice channel to run this command."));
            return;
        }
        String query = (String) interaction.getArguments().getOrDefault("query", "https://youtube.com/watch?v=dQw4w9WgXcQ");
        final AudioManager audioManager = interaction.getGuild().getAudioManager();
        final AudioChannel memberChannel = memberVoiceState.getChannel();
        assert voiceState != null;
        if (!voiceState.inAudioChannel()) {
            audioManager.openAudioConnection(memberChannel);
            audioManager.setSelfDeafened(true);
        }
        if (!Utilities.isValidURL(query)) {
            try {
                ElixirMusicManager.getInstance().loadAndPlay(HttpUtil.getYouTubeURL(query), interaction, "https://www.youtube.com/");
                return;
            } catch (UnsupportedEncodingException exception) {
                interaction.reply(EmbedUtil.sendErrorEmbed("No search results found."));
                Utilities.throwThrowable(new ElixirException(interaction.getGuild(), interaction.getMember()).exception(exception).additionalInformation("Encoding was not supported."));
                return;
            }
        }
        if (Utilities.isValidURL(query) && query.contains("spotify")) {
            try {
                SpotifySourceManager.authorize();
            } catch (IOException | ParseException | SpotifyWebApiException exception) {
                Utilities.throwThrowable(new ElixirException(interaction.getGuild(), interaction.getMember()).exception(exception).additionalInformation("Spotify authorization exception."));
            }
        }
        ElixirMusicManager.getInstance().loadAndPlay(query, interaction, query);
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
                Argument.create("query", "The track to play, by URL or query.", "query", OptionType.STRING, true, 0)
        );
    }
}
