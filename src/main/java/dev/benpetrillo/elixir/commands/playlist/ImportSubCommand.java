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

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.PlaylistUtil;
import dev.benpetrillo.elixir.utilities.TrackUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.SubCommand;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.List;

public final class ImportSubCommand extends SubCommand implements Arguments {
    public ImportSubCommand() {
        super("import", "Import a playlist from Spotify or YouTube.");
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.deferReply();
        var playlistId = interaction.getArgument("id", String.class);
        CustomPlaylist playlist = PlaylistUtil.findPlaylist(playlistId);
        if (playlist == null) {
            interaction.reply(EmbedUtil.sendErrorEmbed("Unable to find a playlist with ID `" + playlistId + "`."));
            return;
        }
        if (!PlaylistUtil.isAuthor(playlist, interaction.getMember())) {
            interaction.reply(EmbedUtil.sendErrorEmbed("You are not the author of this playlist."));
            return;
        }
        var sourcePlaylist = interaction.getArgument("playlist", String.class);
        if(!Utilities.isValidURL(sourcePlaylist)) {
            interaction.reply(EmbedUtil.sendErrorEmbed("That isn't a valid playlist!"));
            return;
        }
        var playlistInfo = TrackUtil.getPlaylistInfoFromUrl(sourcePlaylist);
        if(playlistInfo == null) {
            interaction.reply(EmbedUtil.sendErrorEmbed("Unable to get playlist info from that URL."));
            return;
        }
        for(var track : playlistInfo) {
            PlaylistUtil.addTrackToList(track, playlist, -1);
        }
        interaction.reply(EmbedUtil.sendDefaultEmbed("Successfully imported `" + playlistInfo.size() + "` tracks from playlist."));
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
                Argument.create("id", "The playlist ID.", "id", OptionType.STRING, true, 0),
                Argument.create("playlist", "The playlist to append to the Elixir playlist.", "playlist", OptionType.STRING, true, 1)
        );
    }
}
