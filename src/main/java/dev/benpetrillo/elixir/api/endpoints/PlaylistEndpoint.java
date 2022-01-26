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
import dev.benpetrillo.elixir.types.CustomPlaylist;
import dev.benpetrillo.elixir.utilities.PlaylistUtil;
import dev.benpetrillo.elixir.utilities.Utilities;

import java.io.IOException;

public final class PlaylistEndpoint extends HttpEndpoint {
    private CustomPlaylist playlist;
    
    @Override
    public void get() throws IOException {
        var playlistId = this.arguments.getOrDefault("playlistId", "");
        var action = this.arguments.getOrDefault("action", "");
        if(playlistId.isEmpty() || action.isEmpty()) {
            this.respond(new HttpResponse.NotFound()); return;
        }

        this.playlist = PlaylistUtil.findPlaylist(playlistId);
        if(this.playlist == null) {
            this.respond(new HttpResponse.NotFound()); return;
        }
        
        switch(action) {
            default -> this.respond(new HttpResponse.NotFound());
            case "fetch" -> this.fetch();
            case "addtrack" -> this.addTrack();
        }
    }
    
    private void fetch() throws IOException {
        this.respond(Utilities.base64Encode(Utilities.serialize(this.playlist)));
    }
    
    private void addTrack() throws IOException {
        var track = this.arguments.getOrDefault("track", "");
        var position = Integer.parseInt(this.arguments.getOrDefault("position", "1"));
        if(track.isEmpty()) {
            this.respond(new HttpResponse.BadRequest()); return;
        }
        
//        PlaylistUtil.addTrackToList();
    }
}
