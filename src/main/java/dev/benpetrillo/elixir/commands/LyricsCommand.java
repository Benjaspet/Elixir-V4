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

import dev.benpetrillo.elixir.managers.LyricManager;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import genius.SongSearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class LyricsCommand implements ApplicationCommand {

    private final String name = "lyrics";
    private final String description = "Obtain the lyrics of a track.";
    private final String[] options = {"track"};
    private final String[] optionDescriptions = {"The track to search for."};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        event.deferReply().queue(hook -> {
            try {
                SongSearch result = LyricManager.getTrackData(Objects.requireNonNull(event.getOption("track")).getAsString());
                SongSearch.Hit shortened = result.getHits().get(0);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle(shortened.getTitle())
                        .setThumbnail(shortened.getThumbnailUrl())
                        .setDescription(shortened.fetchLyrics())
                        .setColor(EmbedUtil.getDefaultEmbedColor())
                        .setFooter("Elixir Music", event.getJDA().getSelfUser().getAvatarUrl())
                        .setTimestamp(new Date().toInstant())
                        .build();
                hook.editOriginalEmbeds(embed).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
