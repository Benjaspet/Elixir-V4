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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public final class EmbedUtil {

    public static Color getErrorEmbedColor() {
        return Color.decode("#fc5f53");
    }

    public static Color getDefaultEmbedColor() {
        return Color.decode("#a043de");
    }

    public static MessageEmbed sendErrorEmbed(String description) {
        return new EmbedBuilder()
                .setDescription(description)
                .setColor(getErrorEmbedColor())
                .build();
    }

    public static MessageEmbed sendDefaultEmbed(String description) {
        return new EmbedBuilder()
                .setDescription(description)
                .setColor(getDefaultEmbedColor())
                .build();
    }
}
