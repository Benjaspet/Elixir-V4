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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ApplicationCommandManager {

    public final Map<String, ApplicationCommand> commands = new ConcurrentHashMap<>();

    public static ApplicationCommandManager initialize(JDA jda) {
        ApplicationCommandManager manager = new ApplicationCommandManager();
        if (Boolean.parseBoolean(Config.get("DEPLOY-APPLICATION-COMMANDS-GLOBAL"))) {
            CommandListUpdateAction commands = jda.updateCommands();
            for (ApplicationCommand command : manager.commands.values()) {
                commands.addCommands(command.getCommandData()).queue();
            }
            ElixirClient.logger.info("All global slash commands have been deployed.");
        } else if (Boolean.parseBoolean(Config.get("DELETE-APPLICATION-COMMANDS-GLOBAL"))) {
            CommandListUpdateAction commands = jda.updateCommands();
            commands.addCommands().queue();
            ElixirClient.logger.info("All global slash commands have been deleted.");
        }
        return manager;
    }
    
    private ApplicationCommandManager() {
        registerCommand(
                new JoinCommand(),
                new LoopCommand(),
                new NowPlayingCommand(),
                new PauseCommand(),
                new PlayCommand(),
                new QueueCommand(),
                new ResumeCommand(),
                new SkipCommand(),
                new StopCommand(),
                new VolumeCommand()
        );
    }
    
    private void registerCommand(ApplicationCommand... commands) {
        for (ApplicationCommand cmd : commands) {
            this.commands.put(cmd.getCommandData().getName(), cmd);
        }
    }
}
