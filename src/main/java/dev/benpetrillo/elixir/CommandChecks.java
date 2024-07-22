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

package dev.benpetrillo.elixir;

import dev.benpetrillo.elixir.utils.EmbedUtil;
import tech.xigam.cch.utils.Interaction;

public class CommandChecks {

  public static void runIsInGuildCheck(Interaction interaction) {
    if (!interaction.isFromGuild()) {
      interaction.reply(EmbedUtil.sendErrorEmbed("This command can only be used in a guild."));
    }
    assert interaction.getGuild() != null;
  }
}
