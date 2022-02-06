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

import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.PlaylistUtil;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.SubCommand;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.List;

public final class SettingSubCommand extends SubCommand implements Arguments {

    public SettingSubCommand() {
        super("setting", "Change playlist settings.");
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.deferReply();
        var playlistId = (String) interaction.getArguments().getOrDefault("id", "test");
        CustomPlaylist playlist = PlaylistUtil.findPlaylist(playlistId);
        if (playlist == null) {
            interaction.reply(EmbedUtil.sendErrorEmbed("Unable to find a playlist of id `" + playlistId + "`."));
            return;
        }
        if (!PlaylistUtil.isAuthor(playlist, interaction.getMember())) {
            interaction.reply(EmbedUtil.sendErrorEmbed("You are not the author of this playlist."));
            return;
        }
        String toChange = (String) interaction.getArguments().getOrDefault("setting", "name");
        String value = (String) interaction.getArguments().getOrDefault("value", "Default Name");
        switch (toChange) {
            default -> interaction.reply(EmbedUtil.sendErrorEmbed("Invalid setting `" + toChange + "`."));
            case "cover" -> {
                if (!Utilities.isValidURL(value)) {
                    interaction.reply(EmbedUtil.sendErrorEmbed("That isn't a valid URL!"));
                    return;
                }
                PlaylistUtil.setPlaylistCover(playlist, value);
                interaction.reply(new EmbedBuilder()
                        .setDescription("Successfully swapped the playlist cover!")
                        .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                        .setImage(value).build());
            }
            case "name" -> {
                PlaylistUtil.setPlaylistName(playlist, value);
                interaction.reply(EmbedUtil.sendDefaultEmbed("Successfully changed the playlist name to `" + value + "`."));
            }
            case "description" -> {
                PlaylistUtil.setPlaylistDescription(playlist, value);
                interaction.reply(new EmbedBuilder()
                        .setDescription("Successfully swapped the playlist description!")
                        .addField("New Description", value, false)
                        .setColor(ElixirConstants.DEFAULT_EMBED_COLOR).build());
            }
            case "shuffle" -> {
                PlaylistUtil.setPlaylistSetting(PlaylistUtil.Setting.SHUFFLE, playlist, Utilities.parseBoolean(value));
                interaction.reply(EmbedUtil.sendDefaultEmbed("Successfully changed the shuffle setting to `" + value + "`."));
            }
            case "repeat" -> {
                PlaylistUtil.setPlaylistSetting(PlaylistUtil.Setting.REPEAT, playlist, Utilities.parseBoolean(value));
                interaction.reply(EmbedUtil.sendDefaultEmbed("Successfully changed the repeat setting to `" + value + "`."));
            }
            case "volume" -> {
                int volume = Integer.parseInt(value);
                if (volume < 0 || volume > 150)
                    volume = 100;
                PlaylistUtil.setPlaylistVolume(playlist, volume);
                interaction.reply(EmbedUtil.sendDefaultEmbed("Successfully changed the volume to `" + volume + "`%."));
            }
        }
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
                Argument.create("id", "The playlist ID.", "id", OptionType.STRING, true, 0),
                Argument.createWithChoices("setting", "The setting to change.", "setting", OptionType.STRING, true, 1, "shuffle", "repeat", "volume", "name", "description", "cover", "volume"),
                Argument.create("value", "The new value.", "value", OptionType.STRING, true, 2)
        );
    }
}
