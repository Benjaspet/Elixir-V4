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

package dev.benpetrillo.elixir.managers;

import dev.benpetrillo.elixir.Config;
import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.commands.*;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.concurrent.ConcurrentHashMap;

public final class ApplicationCommandManager {

    public static ConcurrentHashMap<String, ApplicationCommand> commands;

    public ApplicationCommandManager(JDA jda) {
        commands = new ConcurrentHashMap<>();
        commands.put(new JoinCommand().getName(), new JoinCommand());
        commands.put(new LyricsCommand().getName(), new LyricsCommand());
        commands.put(new NowPlayingCommand().getName(), new NowPlayingCommand());
        commands.put(new PlayCommand().getName(), new PlayCommand());
        commands.put(new PauseCommand().getName(), new PauseCommand());
        commands.put(new ResumeCommand().getName(), new ResumeCommand());
        commands.put(new SkipCommand().getName(), new SkipCommand());
        commands.put(new StopCommand().getName(), new StopCommand());
        if (Boolean.parseBoolean(Config.get("DEPLOY-GLOBAL"))) {
            CommandListUpdateAction commands = jda.updateCommands();
            commands.addCommands(
                    new PlayCommand().getCommandData()
            ).queue();
        } else if (Boolean.parseBoolean(Config.get("DELETE-GLOBAL"))) {
            CommandListUpdateAction commands = jda.updateCommands();
            commands.addCommands().queue();
            ElixirClient.logger.info("All global slash commands have been deleted.");
        }
    }
}
