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
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.api.APIError;
import dev.benpetrillo.elixir.api.response.JoinChannelResponse;
import dev.benpetrillo.elixir.api.response.GeneralPlayerResponse;
import dev.benpetrillo.elixir.api.response.NowPlayingResponse;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.utils.APIAuthUtil;
import dev.benpetrillo.elixir.utils.Utilities;
import io.javalin.http.Context;
import net.dv8tion.jda.api.managers.AudioManager;

import static dev.benpetrillo.elixir.utils.Utilities.deserialize;
import static java.util.Objects.requireNonNull;

public class PlayerController {

  public static Context postJoinChannel(Context ctx) {

    var guildId = requireNonNull(ctx.pathParam("guild"), "No guild ID provided.");
    var authHeader = requireNonNull(ctx.header("Authorization"), "No authorization header provided.");
    var apiKey = requireNonNull(authHeader.split(" ")[1], "No API key provided.");
    var body = requireNonNull(deserialize(ctx.body(), JsonObject.class), "No body provided.");
    var channelId = requireNonNull(body.get("channel").getAsString(), "No voice channel provided.");
    var userId = requireNonNull(body.get("user").getAsString(), "No user provided.");
    var guild = requireNonNull(ElixirClient.getJda().getGuildById(guildId), "Guild not found.");

    requireNonNull(guild.getVoiceChannelById(channelId), "Voice channel not found.");
    requireNonNull(ElixirClient.getJda().getUserById(userId), "User not found.");

    if (APIAuthUtil.isValidAPIKey(userId, guildId, apiKey)) {
      return ctx.status(401).json(APIError.from("Request not authorized."));
    }

    AudioManager audioManager = guild.getAudioManager();
    audioManager.openAudioConnection(guild.getVoiceChannelById(channelId));

    return ctx.status(200).json(
        JoinChannelResponse.create(
            guildId, channelId, userId, "Successfully joined the voice channel."));

  }

  public static Context postStopPlayer(Context ctx) {

    var guildId = requireNonNull(ctx.pathParam("guild"), "No guild ID provided.");
    var authHeader = requireNonNull(ctx.header("Authorization"), "No authorization header provided.");
    var apiKey = requireNonNull(authHeader.split(" ")[1], "No API key provided.");
    var body = requireNonNull(deserialize(ctx.body(), JsonObject.class), "No body provided.");
    var userId = requireNonNull(body.get("user").getAsString(), "No user provided.");
    var guild = requireNonNull(ElixirClient.getJda().getGuildById(guildId), "Guild not found.");
    requireNonNull(ElixirClient.getJda().getUserById(userId), "User not found.");

    if (APIAuthUtil.isValidAPIKey(userId, guildId, apiKey)) {
      return ctx.status(401).json(APIError.from("Request not authorized."));
    }

    ElixirMusicManager inst = ElixirMusicManager.getInstance();
    var musicManager = requireNonNull(inst.getMusicManager(guildId), "No music manager found.");

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
        GeneralPlayerResponse.create(
            guildId, userId, "Successfully stopped the player."));

  }

  public static Context postVolume(Context ctx) {
    try {
      var guildId = requireNonNull(ctx.pathParam("guild"), "No guild ID provided.");
      var authHeader = requireNonNull(ctx.header("Authorization"), "Missing authorization header.");
      var apiKey = requireNonNull(authHeader.split(" ")[1], "Invalid API key format.");
      var body = requireNonNull(deserialize(ctx.body(), JsonObject.class), "No body provided.");
      var userId = requireNonNull(body.get("user").getAsString(), "No user provided.");
      var volumeString = requireNonNull(body.get("volume").getAsString(), "No volume provided.");
      requireNonNull(ElixirClient.getJda().getGuildById(guildId), "Guild not found.");
      requireNonNull(ElixirClient.getJda().getUserById(userId), "User not found.");

      int volume = Integer.parseInt(volumeString);
      if (volume < 0 || volume > 100) {
        return ctx.status(400).json(APIError.from("Volume must be between 0 and 100."));
      }

      if (APIAuthUtil.isValidAPIKey(userId, guildId, apiKey)) {
        return ctx.status(401).json(APIError.from("Request not authorized."));
      }

      var inst = ElixirMusicManager.getInstance();
      var musicManager = requireNonNull(inst.getMusicManager(guildId), "No music manager found.");

      musicManager.audioPlayer.setVolume(volume);

      return ctx.status(200).json(
          GeneralPlayerResponse.create(guildId, userId,
              "Successfully changed the volume to %s.".formatted(volume)));

    } catch (Exception e) {
      return ctx.status(500).json(APIError.from("An error occurred while setting the volume."));
    }
  }

  public static Context getNowPlaying(Context ctx) {

    var jda = ElixirClient.getJda();
    var inst = ElixirMusicManager.getInstance();

    var guildId = requireNonNull(ctx.pathParam("guild"), "No guild ID provided.");
    var guild = requireNonNull(jda.getGuildById(guildId), "Guild not found.");

    var audioManager = guild.getAudioManager();
    if (!audioManager.isConnected()) {
      return ctx.status(400).json(APIError.from("Elixir isn't connected to a voice channel."));
    }

    var musicManager = requireNonNull(inst.getMusicManager(guildId), "No music manager found.");
    var track = requireNonNull(musicManager.audioPlayer.getPlayingTrack(), "No track is currently playing.");

    return ctx.status(200).json(Utilities.serialize(NowPlayingResponse.create(track)));

  }
}