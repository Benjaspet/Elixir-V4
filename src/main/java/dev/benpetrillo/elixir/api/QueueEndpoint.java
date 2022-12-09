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

package dev.benpetrillo.elixir.api;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.Utilities;
import tech.xigam.express.Request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Includes:
 * - /queue
 */

@SuppressWarnings({"JavadocReference"})

public final class QueueEndpoint {

    /**
     * The base URL for the queue endpoint.
     * Ex: https://app.ponjo.club/v1/elixir/queue?guild=836268305211719690
     * @param guildId The guild ID of the queue you want to use.
     * @param action The action you want to perform.
     */
    
    public static void indexEndpoint(Request request) {
        final String guildId = request.requestArguments.getOrDefault("guildId", "");
        final String action = request.requestArguments.getOrDefault("action", "");
        if (guildId.isEmpty() || action.isEmpty()) {
            request.code(400).respond("Missing required arguments."); return;
        }
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(guildId);
        if (musicManager == null) {
            request.code(400).respond("Unable to find guild music manager."); return;
        }
        switch (action) {
            default -> request.code(400).respond("Invalid action.");
            case "queue" -> {
                final List<AudioTrack> tracks = musicManager.scheduler.queue.stream().toList();
                final List<AudioTrackInfo> trackInfo = tracks.stream().map(AudioTrack::getInfo).toList();
                request.respond(Utilities.base64Encode(Utilities.serialize(trackInfo)));
            }
            case "shuffle" -> {
                final List<AudioTrack> tracks = new ArrayList<>(musicManager.scheduler.queue);
                Collections.shuffle(tracks); musicManager.scheduler.queue.clear();
                musicManager.scheduler.queue.addAll(tracks);
                final List<AudioTrackInfo> trackInfo = tracks.stream().map(AudioTrack::getInfo).toList();
                request.respond(Utilities.base64Encode(Utilities.serialize(trackInfo)));
            }
        }
    }
}
