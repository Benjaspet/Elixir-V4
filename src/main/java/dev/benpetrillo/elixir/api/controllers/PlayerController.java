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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.api.APIError;
import dev.benpetrillo.elixir.api.types.NowPlayingObject;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utils.Utilities;
import io.javalin.http.Context;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;

public class PlayerController {

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
    Objects.requireNonNull(track, "No track is currently playing.");

    return ctx.status(200).json(Utilities.serialize(NowPlayingObject.create(track)));

  }
}