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

package dev.benpetrillo.elixir.utilities;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.managers.DatabaseManager;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.music.playlist.PlaylistTrack;
import dev.benpetrillo.elixir.types.CustomPlaylist;
import net.dv8tion.jda.api.entities.Member;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class PlaylistUtil {

    /**
     * Create a custom playlist.
     * @param playlistId The playlist ID.
     * @return If the playlist was created.
     */
    
    public static boolean createPlaylist(String playlistId, Member member) {
        if(findPlaylist(playlistId) != null)
            return false;
        
        MongoCollection<Document> dbCollection = DatabaseManager.getPlaylistCollection();
        dbCollection.insertOne(new Document("playlistId", playlistId)
                .append("creatorId", member.getId())
                .append("playlistData", Utilities.base64Encode(Utilities.serialize(
                        CustomPlaylist.create(playlistId, member)
                ))));
        return true;
    }
    
    /**
     * Deletes a custom playlist.
     * @param playlistId The playlist ID.
     * @return If the playlist was deleted.
     */
    
    public static boolean deletePlaylist(String playlistId) {
        if(findPlaylist(playlistId) == null)
            return false;

        MongoCollection<Document> dbCollection = DatabaseManager.getPlaylistCollection();
        dbCollection.deleteOne(new Document("playlistId", playlistId));
        return true;
    }

    /**
     * Get a custom playlist object by ID.
     * @param playlistId The playlist ID to get.
     * @return The playlist object or null if one cannot be found.
     */

    @Nullable
    public static CustomPlaylist findPlaylist(String playlistId) {
        MongoCollection<Document> dbCollection = DatabaseManager.getPlaylistCollection();
        BasicDBObject object = new BasicDBObject("playlistId", playlistId);
        Document document = dbCollection.find(object).first();
        if(document == null)
            return null;
        
        String serializedPlaylistData = document.getString("playlistData");
        return Utilities.deserialize(Utilities.base64Decode(serializedPlaylistData), CustomPlaylist.class);
    }

    /**
     * Determines if the member is an author of the playlist.
     * @param playlist The playlist to check.
     * @param member The member to check.
     * @return The result of the check.
     */

    public static boolean isAuthor(CustomPlaylist playlist, Member member) {
        return playlist.info.author.matches(member.getId());
    }

    /**
     * Get a collection of tracks in a custom playlist.
     * @param playlist The playlist to get tracks from.
     * @return A collection of playable tracks.
     */
    
    public static List<PlaylistTrack> getTracks(CustomPlaylist playlist) {
        final List<PlaylistTrack> tracks = new ArrayList<>();
        
        for(CustomPlaylist.CustomPlaylistTrack track : playlist.tracks) {
            TrackUtil.TrackType trackType = TrackUtil.determineTrackType(track.url);
            AudioSourceManager source = switch (trackType) {
                case YOUTUBE -> ElixirMusicManager.getInstance().youtubeSource;
                case SPOTIFY -> ElixirMusicManager.getInstance().spotifySource;
                default -> throw new IllegalArgumentException("Unsupported track URL: " + track.url);
            };

            tracks.add(new PlaylistTrack(track.title, track, source));
        }
        
        return tracks;
    }

    /**
     * Adds a track to a custom playlist.
     * @param track The track to add.
     * @param playlist The playlist to add the track to.
     */
    
    public static void addTrackToList(AudioTrackInfo track, CustomPlaylist playlist, int index) {
        var newTrack = CustomPlaylist.CustomPlaylistTrack.from(track);
        if(index == -1)
            playlist.tracks.add(newTrack);
        else try {
            playlist.tracks.add(index, newTrack);
        } catch (IndexOutOfBoundsException ignored) { }

        updatePlaylist(playlist);
    }

    /**
     * Removes a track from a custom playlist.
     * @param index The track to remove.
     * @param playlist The playlist to remove the track from.
     */
    
    public static void removeTrackFromList(int index, CustomPlaylist playlist) throws IndexOutOfBoundsException {
        playlist.tracks.remove(index);
        updatePlaylist(playlist);
    }
    
    /*
     * Internal Methods
     */

    /**
     * Updates a custom playlist.
     * @param playlist The playlist to update.
     */
    
    private static void updatePlaylist(CustomPlaylist playlist) {
        MongoCollection<Document> dbCollection = DatabaseManager.getPlaylistCollection();
        Bson dbObject = new BasicDBObject("playlistId", playlist.info.id);
        Bson playlistObject = dbCollection.find(dbObject).first();
        if(playlistObject == null)
            return;
        
        var newData = Utilities.base64Encode(Utilities.serialize(playlist));
        var update = Updates.set("playlistData", newData);
        
        try {
            dbCollection.updateOne(dbObject, update, new UpdateOptions().upsert(true));
        } catch (MongoException ignored) { }
    }
}
