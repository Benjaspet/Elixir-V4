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

package dev.benpetrillo.elixir.events;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.managers.ApplicationCommandManager;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public final class ApplicationCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        String commandName = event.getName();
        ApplicationCommand command; ApplicationCommandManager manager = ElixirClient.applicationCommandManager;
        if ((command = manager.commands.get(commandName)) != null) {
            command.runCommand(event, event.getMember(), event.getGuild());
        }
    }
}
