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
import tech.xigam.cch.ComplexCommandHandler;
import tech.xigam.cch.command.BaseCommand;

import java.util.ArrayList;

public final class ApplicationCommandManager {

    public static void initialize() {
        new ApplicationCommandManager(ElixirClient.getCommandHandler());
        if (Boolean.parseBoolean(Config.get("DEPLOY-APPLICATION-COMMANDS-GLOBAL"))) {
            ElixirClient.getCommandHandler().deployAll(null);
            ElixirClient.logger.info("All global slash commands have been deployed.");
        } else if (Boolean.parseBoolean(Config.get("DELETE-APPLICATION-COMMANDS-GLOBAL"))) {
            ElixirClient.getCommandHandler().downsert(null);
            ElixirClient.logger.info("All global slash commands have been deleted.");
        }
    }
    
    private ApplicationCommandManager(ComplexCommandHandler handler) {
        registerCommand(handler, 
                new InfoCommand(),
                new JoinCommand(),
                new LoopCommand(),
                new LyricsCommand(),
                new NowPlayingCommand(),
                new PauseCommand(),
                new PlayCommand(),
                new PlaylistCommand(),
                new QueueCommand(),
                new ResumeCommand(),
                new ShuffleCommand(),
                new SkipCommand(),
                new StopCommand(),
                new VolumeCommand(),
                new UserInfoCommand()
        );
    }
    
    private void registerCommand(ComplexCommandHandler handler, BaseCommand... commands) {
        ArrayList<String> commandNames = new ArrayList<>();
        for (BaseCommand command : commands) {
            handler.registerCommand(command);
            commandNames.add(command.getLabel());
        }
        String commandNamesString = String.join(", ", commandNames);
        ElixirClient.logger.info("Registered commands: " + commandNamesString);
    }
}