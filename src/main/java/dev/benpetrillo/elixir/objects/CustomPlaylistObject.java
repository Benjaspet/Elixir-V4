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

package dev.benpetrillo.elixir.objects;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;

import java.util.List;

public class CustomPlaylistObject {

    private List<PlaylistTrack> tracks;
    private String author;

    public CustomPlaylistObject() {}

    public void setTracks(List<PlaylistTrack> tracks) {
        this.tracks = tracks;
    }

    public void addTrack(PlaylistTrack track) {
        this.tracks.add(track);
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<PlaylistTrack> getTracks() {
        return tracks;
    }

    public String getAuthor() {
        return author;
    }

    public static DBObject convert(CustomPlaylistObject object) {
        return new BasicDBObject("creator", object.getAuthor())
                .append("tracks", object.getTracks());
    }
}