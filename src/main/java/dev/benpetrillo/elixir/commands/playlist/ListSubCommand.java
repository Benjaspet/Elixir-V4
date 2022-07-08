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
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import dev.benpetrillo.elixir.utilities.PlaylistUtil;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.SubCommand;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public final class ListSubCommand extends SubCommand implements Arguments {
    public ListSubCommand() {
        super("list", "Lists all Elixir playlists created.");
    }

    @Override
    public void execute(Interaction interaction) {
        interaction.deferReply();

        // Get the page to fetch.
        int page = interaction.getArgument("page", 1L, Long.class).intValue();
        if (page == 0) page = 1; // Fallback the page to 1 if it's 0.

        // Fetch all playlists from the database.
        var playlists = PlaylistUtil.getAllPlaylists();
        // Get the maximum amount of playlists to display.
        var maxAmount = Math.min(playlists.size(), 12);
        // Get the total pages.
        var totalPages = (int) Math.ceil(playlists.size() / maxAmount);

        // Check if the page is valid.
        if (page > totalPages) {
            interaction.reply(EmbedUtil.sendErrorEmbed("There is not a page `" + page + "`!"), false);
            return;
        }

        // Create an embed for the playlist.
        var embed = new EmbedBuilder()
                .setTitle("Elixir Playlists")
                .setColor(ElixirConstants.DEFAULT_EMBED_COLOR)
                .setFooter("Elixir Music", ElixirClient.getJda().getSelfUser().getAvatarUrl())
                .setTimestamp(OffsetDateTime.now());

        // Add the playlists to the embed.
        var description = new StringBuilder();
        for(int i = (page - 1) * maxAmount; i < page * maxAmount && i < playlists.size(); i++) {
            var playlist = playlists.get(i);
            var formatted = "**%s**: `%s`".formatted(playlist.info.name, playlist.info.id);
            description.append(formatted).append("\n");
        }

        // Check if there are more pages.
        if(playlists.size() > maxAmount) {
            description.append("\n").append("Page %d of %d".formatted(page, totalPages));
        }

        // Set the description of the embed.
        embed.setDescription(description.toString());
        // Reply with the embed.
        interaction.reply(embed.build());
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(
                Argument.create("page", "The page number to fetch.", "page", OptionType.INTEGER, false, 0)
                        .range(1, 100)
        );
    }
}