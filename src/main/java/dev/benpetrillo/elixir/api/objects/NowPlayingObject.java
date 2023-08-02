/*
 * Copyright © 2023 Ben Petrillo, KingRainbow44. All rights reserved.
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

package dev.benpetrillo.elixir.api.objects;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public final class NowPlayingObject {

    public final String title;
    public final String author;
    public final long length;
    public final String identifier;
    public final boolean isStream;
    public final String uri;
    public final long position;

    private NowPlayingObject(String title, String author, long length, String identifier, boolean isStream, String uri, long position) {
        this.title = title;
        this.author = author;
        this.length = length;
        this.identifier = identifier;
        this.isStream = isStream;
        this.uri = uri;
        this.position = position;
    }

    public static NowPlayingObject create(AudioTrack track) {
        final long position = track.getPosition();
        final AudioTrackInfo trackInfo = track.getInfo();
        return new NowPlayingObject(
                trackInfo.title, trackInfo.author, trackInfo.length,
                trackInfo.identifier, trackInfo.isStream, trackInfo.uri, position
        );
    }
}
