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

package dev.benpetrillo.elixir.types;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.utilities.TrackUtil;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public final class CustomPlaylist {
    public static CustomPlaylist create(String playlistId, Member creator) {
        var playlist = new CustomPlaylist();
        playlist.info = new Info();
        playlist.tracks = new ArrayList<>();
        playlist.options = new Options();
        
        playlist.info.id = playlistId;
        playlist.info.name = creator.getEffectiveName() + "'s Playlist";
        playlist.info.description = "A cool playlist by " + creator.getEffectiveName() + "!";
        playlist.info.playlistCoverUrl = "https://cdn.discordapp.com/avatars/838118537276031006/e2e3d5f897b833632ecd3e90ab989949.webp?size=512";
        playlist.info.author = creator.getId();
        return playlist;
    }
    
    public Info info;
    public List<CustomPlaylistTrack> tracks;
    public Options options;
    
    public static class Info {
        public String id, name, description, playlistCoverUrl, author;
    }
    
    public static class CustomPlaylistTrack {
        public String title, url, artist, coverArt;
        public long duration;
        
        public static CustomPlaylistTrack from(AudioTrackInfo info) {
            var track = new CustomPlaylistTrack();
            track.title = info.title;
            track.url = info.uri.contains("spotify") ? "https://open.spotify.com/track/" + info.uri.split(":")[2] : info.uri;
            track.artist = info.author;
            track.coverArt = TrackUtil.getCoverArt(info);
            track.duration = info.length;
            return track;
        }
    }
    
    public static class Options {
        public boolean shuffle = false, repeat = false;
    }
}
