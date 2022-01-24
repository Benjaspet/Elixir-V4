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
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.music.TrackScheduler;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utilities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.*;

import static dev.benpetrillo.elixir.utilities.EmbedUtil.getDefaultEmbedColor;

public final class PlaylistCommand implements ApplicationCommand {

    private final String name = "playlist";
    private final String description = "Manage your custom playlists.";
    private final String[] options = {"id", "track", "index", "setting", "value"};
    private final String[] optionDescriptions = {"The playlist ID.", "The track to add.", "The index of the track.", "The setting to change.", "The new value."};
    private final String[] subCommands = {"addtrack", "removetrack", "queue", "create", "delete", "fetch", "setting"};
    private final String[] subCommandDescriptions = {"Add a track to the playlist.", "Remove a track from the playlist.", "Queue a track to the playlist.", "Create a new playlist.", "Delete a playlist.", "Fetch a playlist.", "Change playlist settings."};
    private final String[] choices = {"cover", "name", "description", "shuffle", "repeat"};
    
    private final List<String> ignore = List.of("create");

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        String subCommand = event.getSubcommandName(); assert subCommand != null;
        String playlistId = Objects.requireNonNull(event.getOption("id")).getAsString();
        event.deferReply().queue(hook -> {
            CustomPlaylist playlist = PlaylistUtil.findPlaylist(playlistId);
            String track; int index; List<PlaylistTrack> tracks;
            if (playlist == null && !this.ignore.contains(subCommand)) {
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("Unable to find a playlist of id `" + playlistId + "`.")).queue();
                return;
            }
            switch (subCommand) {
                case "addtrack":
                    assert playlist != null;
                    if (!PlaylistUtil.isAuthor(playlist, member)) {
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("You are not the author of this playlist.")).queue();
                        return;
                    }
                    track = Objects.requireNonNull(event.getOption("track")).getAsString();
                    OptionMapping indexMapping = event.getOption("index");
                    index = indexMapping == null ? -1 : (int) indexMapping.getAsLong();
                    if (!Utilities.isValidURL(track)) {
                        try {
                            track = HttpUtil.getYouTubeURL(track);
                        } catch (Exception ignored) { return; }
                    }
                    var trackInfo = TrackUtil.getTrackInfoFromUrl(track);
                    if (trackInfo == null) {
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("Unable to find a track with the URL `" + track + "`.")).queue();
                        return;
                    }
                    PlaylistUtil.addTrackToList(trackInfo, playlist, index);
                    hook.editOriginalEmbeds(EmbedUtil.sendDefaultEmbed("Successfully added [%s](%s) to playlist.".formatted(trackInfo.title, trackInfo.uri))).queue();
                    break;
                case "removetrack":
                    assert playlist != null;
                    if (!PlaylistUtil.isAuthor(playlist, member)) {
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("You are not the author of this playlist.")).queue();
                        return;
                    }
                    index = (int) Objects.requireNonNull(event.getOption("index")).getAsLong();
                    try {
                        PlaylistUtil.removeTrackFromList(index, playlist);
                        hook.editOriginalEmbeds(EmbedUtil.sendDefaultEmbed("Successfully removed track from playlist.")).queue();
                    } catch (IndexOutOfBoundsException ignored) { 
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("That track doesn't exist.")).queue();
                    }
                    break;
                case "queue":
                    final GuildVoiceState memberVoiceState = member.getVoiceState(); assert memberVoiceState != null;
                    if (!memberVoiceState.inAudioChannel()) {
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("You must be in a voice channel to queue tracks.")).queue();
                        return;
                    }
                    final GuildVoiceState voiceState = guild.getSelfMember().getVoiceState(); assert voiceState != null;
                    final AudioManager audioManager = guild.getAudioManager();
                    final AudioChannel audioChannel = Objects.requireNonNull(member.getVoiceState()).getChannel();
                    if (!voiceState.inAudioChannel()) {
                        audioManager.openAudioConnection(audioChannel);
                        audioManager.setSelfDeafened(true);
                    }
                    assert playlist != null;
                    GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(guild);
                    tracks = PlaylistUtil.getTracks(playlist); TrackUtil.appendUser(member.getId(), tracks);
                    if (musicManager.scheduler.queue.isEmpty() && musicManager.audioPlayer.getPlayingTrack() == null) {
                        musicManager.scheduler.repeating = playlist.options.repeat
                                ? TrackScheduler.LoopMode.QUEUE : TrackScheduler.LoopMode.NONE;
                        if (playlist.options.shuffle) Collections.shuffle(tracks);
                    }
                    musicManager.scheduler.getQueue().addAll(tracks);
                    if (musicManager.audioPlayer.getPlayingTrack() == null) musicManager.scheduler.nextTrack();
                    hook.editOriginalEmbeds(EmbedUtil.sendDefaultEmbed("Queued **%s** tracks from %s.".formatted(playlist.tracks.size(), playlist.info.name))).queue();
                    break;
                case "create":
                    if (!PlaylistUtil.createPlaylist(playlistId, member)) {
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("A playlist with id `" + playlistId + "` already exists.")).queue();
                        return;
                    }
                    hook.editOriginalEmbeds(EmbedUtil.sendDefaultEmbed("Successfully created a playlist with id `" + playlistId + "`.")).queue();
                    break;
                case "delete":
                    if (playlist == null) {
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("A playlist with id `" + playlistId + "` doesn't exist.")).queue();
                        return;
                    }
                    if (!PlaylistUtil.isAuthor(playlist, member)) {
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("You are not the author of this playlist.")).queue();
                        return;
                    }
                    PlaylistUtil.deletePlaylist(playlistId);
                    hook.editOriginalEmbeds(EmbedUtil.sendDefaultEmbed("Successfully deleted playlist " + playlist.info.name + ".")).queue();
                    break;
                case "fetch":
                    assert playlist != null;
                    tracks = PlaylistUtil.getTracks(playlist);
                    StringBuilder description = new StringBuilder();
                    int maxAmount = Math.min(tracks.size(), 12);
                    final String thumbnail = playlist.info.playlistCoverUrl;
                    for (int i = 0; i < maxAmount; i++) {
                        final AudioTrack playlistTrack = tracks.get(i);
                        final AudioTrackInfo info = playlistTrack.getInfo();
                        String title = info.title.length() > 55 ? info.title.substring(0, 52) + "..." : info.title;
                        String formattedString = String.format("**#%s** - [%s](%s)", i + 1, title, info.uri);
                        description.append(formattedString).append("\n");
                    }
                    if (tracks.size() > maxAmount) {
                        description.append("\n").append(String.format("...and %s more tracks.", tracks.size() - maxAmount));
                    }
                    final String settings = """
                            Shuffle: %s
                            Repeat: %s
                            """.formatted(playlist.options.shuffle ? "Yes" : "No", playlist.options.repeat ? "Yes" : "No");
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle(playlist.info.name)
                            .setColor(getDefaultEmbedColor())
                            .setThumbnail(thumbnail)
                            .setDescription("Author: <@%s>".formatted(playlist.info.author))
                            .addField("Description", playlist.info.description, false)
                            .addField("Queue Settings", String.valueOf(settings), false)
                            .addField("Sample Tracks", String.valueOf(description), false)
                            .setFooter("Elixir Music", event.getJDA().getSelfUser().getAvatarUrl())
                            .setTimestamp(new Date().toInstant())
                            .build();
                    hook.editOriginalEmbeds(embed).queue();
                    break;
                case "setting":
                    assert playlist != null;
                    if (!PlaylistUtil.isAuthor(playlist, member)) {
                        hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("You are not the author of this playlist.")).queue();
                        return;
                    }
                    
                    String toChange = Objects.requireNonNull(event.getOption("setting")).getAsString();
                    String value = Objects.requireNonNull(event.getOption("value")).getAsString();

                    switch (toChange) {
                        default -> {
                            hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("Invalid setting `" + toChange + "`.")).queue();
                            return;
                        }
                        case "cover" -> {
                            if (!Utilities.isValidURL(value)) {
                                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("That isn't a valid URL!")).queue();
                                return;
                            }
                            PlaylistUtil.setPlaylistCover(playlist, value);
                            hook.editOriginalEmbeds(new EmbedBuilder()
                                    .setDescription("Successfully swapped the playlist cover!")
                                    .setColor(getDefaultEmbedColor())
                                    .setImage(value).build()).queue();
                        }
                        case "name" -> {
                            PlaylistUtil.setPlaylistName(playlist, value);
                            hook.editOriginalEmbeds(EmbedUtil.sendDefaultEmbed("Successfully changed the playlist name to `" + value + "`.")).queue();
                        }
                        case "description" -> {
                            PlaylistUtil.setPlaylistDescription(playlist, value);
                            hook.editOriginalEmbeds(new EmbedBuilder()
                                    .setDescription("Successfully swapped the playlist description!")
                                    .addField("New Description", value, false)
                                    .setColor(getDefaultEmbedColor()).build()).queue();
                        }
                        case "shuffle" -> {
                            PlaylistUtil.setPlaylistSetting(PlaylistUtil.Setting.SHUFFLE, playlist, Utilities.parseBoolean(value));
                            hook.editOriginalEmbeds(EmbedUtil.sendDefaultEmbed("Successfully changed the shuffle setting to `" + value + "`.")).queue();
                        }
                        case "repeat" -> {
                            PlaylistUtil.setPlaylistSetting(PlaylistUtil.Setting.REPEAT, playlist, Utilities.parseBoolean(value));
                            hook.editOriginalEmbeds(EmbedUtil.sendDefaultEmbed("Successfully changed the repeat setting to `" + value + "`.")).queue();
                        }
                    }
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
                                .addOption(OptionType.STRING, this.options[0], this.optionDescriptions[0], true),
                        new SubcommandData(this.subCommands[6], this.subCommandDescriptions[6])
                                .addOption(OptionType.STRING, this.options[0], this.optionDescriptions[0], true)
                                .addOptions(new OptionData(OptionType.STRING, this.options[3], this.optionDescriptions[3], true)
                                        .addChoice(this.choices[0], this.choices[0])
                                        .addChoice(this.choices[1], this.choices[1])
                                        .addChoice(this.choices[2], this.choices[2])
                                        .addChoice(this.choices[3], this.choices[3])
                                        .addChoice(this.choices[4], this.choices[4])
                                )
                                .addOption(OptionType.STRING, this.options[4], this.optionDescriptions[4], true)
                );
    }
}
