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

package dev.benpetrillo.elixir.api.endpoints;

import dev.benpetrillo.elixir.api.HttpEndpoint;
import dev.benpetrillo.elixir.api.HttpResponse;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.Utilities;

import java.io.IOException;

public final class PlayerEndpoint extends HttpEndpoint {
    private GuildMusicManager musicManager;
    
    @Override
    public void get() throws IOException {
        var guildId = this.arguments.getOrDefault("guildId", "");
        var action = this.arguments.getOrDefault("action", "");
        if(guildId.isEmpty() || action.isEmpty()) {
            this.respond(new HttpResponse.NotFound()); return;
        }

        this.musicManager = ElixirMusicManager.getInstance().getMusicManager(guildId);
        if(this.musicManager == null) {
            this.respond(new HttpResponse.NotFound()); return;
        }
        
        switch(action) {
            default -> this.respond(new HttpResponse.NotFound());
            case "nowplaying" -> this.nowPlaying();
            case "pause" -> this.musicManager.audioPlayer.setPaused(true);
            case "resume" -> this.musicManager.audioPlayer.setPaused(false);
            case "skip" -> this.musicManager.scheduler.nextTrack();
        }
    }
    
    private void nowPlaying() throws IOException {
        var track = this.musicManager.audioPlayer.getPlayingTrack();
        this.statusCode = 301; // Found.
        this.respond(Utilities.serialize(track.getInfo()));
    }
}
