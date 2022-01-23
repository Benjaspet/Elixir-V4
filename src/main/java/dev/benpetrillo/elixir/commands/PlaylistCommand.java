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

import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utilities.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Objects;

public final class PlaylistCommand implements ApplicationCommand {

    private final String name = "playlist";
    private final String description = "Manage your custom playlists.";
    private final String[] options = {"id", "track", "index"};
    private final String[] optionDescriptions = {"The playlist ID.", "The track to add.", "The index of the track."};
    private final String[] subCommands = {"addtrack", "removetrack", "queue", "create", "delete", "fetch"};
    private final String[] subCommandDescriptions = {"Add a track to the playlist.", "Remove a track from the playlist.", "Queue a track to the playlist.", "Create a new playlist.", "Delete a playlist.", "Fetch a playlist."};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        String subCommand = event.getSubcommandName(); assert subCommand != null;
        String playlistId = Objects.requireNonNull(event.getOption("id")).getAsString();
        
        event.deferReply().queue(hook -> {
            CustomPlaylist playlist = PlaylistUtil.findPlaylist(playlistId);

            if(playlist == null) {
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("Unable to find a playlist of id `" + playlistId + "`.")).queue();
                return;
            }

            switch(subCommand) {
                case "addtrack":
                    if(!PlaylistUtil.isAuthor(playlist, member)) {
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("You are not the author of this playlist.")).queue();
                        return;
                    }

                    String track = Objects.requireNonNull(event.getOption("track")).getAsString();
                    OptionMapping indexMapping = event.getOption("index");
                    long index = indexMapping == null ? -1 : indexMapping.getAsLong();

                    if(!Utilities.isValidURL(track)) {
                        try {
                            track = HttpUtil.getYouTubeURL(track);
                        } catch (Exception ignored) { return; }
                    }

                    PlaylistUtil.addTrackToList(TrackUtil.getTrackInfoFromUrl(track), playlist, (int) index);
                    hook.editOriginalEmbeds(EmbedUtil.sendDefaultEmbed("Successfully added track to playlist.")).queue();
                    break;
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
                .addSubcommands(
                        new SubcommandData(this.subCommands[0], this.subCommandDescriptions[0])
                                .addOption(OptionType.STRING, this.options[0], this.optionDescriptions[0], true)
                                .addOption(OptionType.STRING, this.options[1], this.optionDescriptions[1], true)
                                .addOption(OptionType.INTEGER, this.options[2], this.optionDescriptions[2], false),
                        new SubcommandData(this.subCommands[1], this.subCommandDescriptions[1])
                                .addOption(OptionType.STRING, this.options[0], this.optionDescriptions[0], true)
                                .addOption(OptionType.INTEGER, this.options[2], this.optionDescriptions[2], true),
                        new SubcommandData(this.subCommands[2], this.subCommandDescriptions[2])
                                .addOption(OptionType.STRING, this.options[0], this.optionDescriptions[0], true),
                        new SubcommandData(this.subCommands[3], this.subCommandDescriptions[3])
                                .addOption(OptionType.STRING, this.options[0], this.optionDescriptions[0], true),
                        new SubcommandData(this.subCommands[4], this.subCommandDescriptions[4])
                                .addOption(OptionType.STRING, this.options[0], this.optionDescriptions[0], true),
                        new SubcommandData(this.subCommands[5], this.subCommandDescriptions[5])
                                .addOption(OptionType.STRING, this.options[0], this.optionDescriptions[0], true)
                );
    }
}
