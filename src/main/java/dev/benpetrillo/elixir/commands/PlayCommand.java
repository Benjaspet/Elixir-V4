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
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.Cooldown;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.HttpUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;

public final class PlayCommand implements ApplicationCommand {

    private final String name = "play";
    private final String description = "Play a track with a link or query.";
    private final String[] options = {"query"};
    private final String[] optionDescriptions = {"The track to play, by URL or query."};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        final TextChannel channel = event.getTextChannel();
        final GuildVoiceState voiceState = channel.getGuild().getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        assert memberVoiceState != null;
        event.deferReply().queue(hook -> {
            if (!memberVoiceState.inAudioChannel()) {
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("You must be in a voice channel to run this command.")).queue();
                return;
            }
            String query = Objects.requireNonNull(event.getOption("query")).getAsString();
            final AudioManager audioManager = channel.getGuild().getAudioManager();
            final AudioChannel memberChannel = memberVoiceState.getChannel();
            assert voiceState != null;
            if (!voiceState.inAudioChannel()) {
                audioManager.openAudioConnection(memberChannel);
                audioManager.setSelfDeafened(true);
            }
            if (Cooldown.isInCooldown(hook.getInteraction().getUser().getId(), new Date().toInstant().getEpochSecond())) {
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("You're queueing tracks too fast!")).queue();
                return;
            }
            if (!Utilities.isValidURL(query)) {
                try {
                    ElixirMusicManager.getInstance().loadAndPlaySingleTrack(channel, HttpUtil.getYouTubeURL(query), hook);
                    Cooldown.add(hook.getInteraction().getUser().getId());
                    return;
                } catch (UnsupportedEncodingException ignored) {
                    hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("No search results found.")).queue();
                    return;
                }
            }
            if (Utilities.isValidURL(query) && query.contains("spotify")) {
                try {
                    SpotifySourceManager.authorize();
                } catch (IOException | ParseException | SpotifyWebApiException e) {
                    e.printStackTrace();
                }
            }
            ElixirMusicManager.getInstance().loadAndPlaySingleTrack(channel, query, hook);
        });
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
                .addOption(OptionType.STRING, this.options[0], this.optionDescriptions[0], true);
    }
}
