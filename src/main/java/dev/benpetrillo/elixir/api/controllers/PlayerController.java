/*
 * Copyright © 2024 Ben Petrillo, KingRainbow44.
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
 * All portions of this software are available for public use,
 * provided that credit is given to the original author(s).
 */

package dev.benpetrillo.elixir.api.controllers;

import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.api.APIError;
import dev.benpetrillo.elixir.api.response.JoinChannelResponse;
import dev.benpetrillo.elixir.api.response.StopPlayerResponse;
import dev.benpetrillo.elixir.api.types.NowPlayingObject;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utils.APIAuthUtil;
import dev.benpetrillo.elixir.utils.Utilities;
import io.javalin.http.Context;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;

public class PlayerController {

  public static Context postJoinChannel(Context ctx) {

    String guildId = ctx.pathParam("guild");
    Objects.requireNonNull(guildId, "No guild ID provided.");
    String authHeader = ctx.header("Authorization");
    Objects.requireNonNull(authHeader, "No authorization header provided.");
    String apiKey = authHeader.split(" ")[1];

    JsonObject body = Utilities.deserialize(ctx.body(), JsonObject.class);
    String channelId = body.get("channel").getAsString();
    String userId = body.get("user").getAsString();
    Objects.requireNonNull(channelId, "No voice channel provided.");
    Objects.requireNonNull(userId, "No user provided.");

    Guild guild = ElixirClient.getJda().getGuildById(guildId);
    Objects.requireNonNull(guild, "Guild not found.");
    VoiceChannel channel = guild.getVoiceChannelById(channelId);
    Objects.requireNonNull(channel, "Voice channel not found.");
    User user = ElixirClient.getJda().getUserById(userId);
    Objects.requireNonNull(user, "User not found.");

    if (!APIAuthUtil.isValidAPIKey(userId, guildId, apiKey)) {
      return ctx.status(401).json(APIError.from("Request not authorized."));
    }

    AudioManager audioManager = guild.getAudioManager();
    audioManager.openAudioConnection(guild.getVoiceChannelById(channelId));

    return ctx.status(200).json(
        JoinChannelResponse.create(
            guildId, channelId, userId, "Successfully joined the voice channel."));

  }

  public static Context postStopPlayer(Context ctx) {

    String guildId = ctx.pathParam("guild");
    Objects.requireNonNull(guildId, "No guild ID provided.");
    String authHeader = ctx.header("Authorization");
    Objects.requireNonNull(authHeader, "No authorization header provided.");
    String apiKey = authHeader.split(" ")[1];

    JsonObject body = Utilities.deserialize(ctx.body(), JsonObject.class);
    String userId = body.get("user").getAsString();
    Objects.requireNonNull(userId, "No user provided.");

    Guild guild = ElixirClient.getJda().getGuildById(guildId);
    Objects.requireNonNull(guild, "Guild not found.");
    User user = ElixirClient.getJda().getUserById(userId);
    Objects.requireNonNull(user, "User not found.");

    if (!APIAuthUtil.isValidAPIKey(userId, guildId, apiKey)) {
      return ctx.status(401).json(APIError.from("Request not authorized."));
    }

    ElixirMusicManager inst = ElixirMusicManager.getInstance();
    GuildMusicManager musicManager = inst.getMusicManager(guildId);
    Objects.requireNonNull(musicManager, "No music manager found.");

    try {
      musicManager.scheduler.queue.clear();
      musicManager.audioPlayer.destroy();
      if (guild.getAudioManager().isConnected()) {
        guild.getAudioManager().closeAudioConnection();
      }
    } catch (Exception e) {
      return ctx.status(500).json(APIError.from("An error occurred while stopping the player."));
    }

    return ctx.status(200).json(
        StopPlayerResponse.create(
            guildId, userId, "Successfully stopped the player."));

  }

  public static Context getNowPlaying(Context ctx) {
    String guildId = ctx.pathParam("guild");
    ElixirMusicManager inst = ElixirMusicManager.getInstance();
    JDA jda = ElixirClient.getJda();
    if (guildId.isEmpty()) {
      return ctx.status(400).result("No guild ID provided.");
    }
    Guild guild = jda.getGuildById(guildId);
    Objects.requireNonNull(guild, "Guild not found.");

    AudioManager audioManager = guild.getAudioManager();
    if (!audioManager.isConnected()) {
      return ctx.status(400).json(APIError.from("Elixir isn't connected to a voice channel."));
    }

    GuildMusicManager musicManager = inst.getMusicManager(guildId);
    Objects.requireNonNull(musicManager, "No music manager found.");

    AudioTrack track = musicManager.audioPlayer.getPlayingTrack();

    if (musicManager.audioPlayer.getPlayingTrack() == null) {
      return ctx.status(404).json(APIError.from("No track is currently playing."));
    }

    return ctx.status(200).json(Utilities.serialize(NowPlayingObject.create(track)));

  }
}