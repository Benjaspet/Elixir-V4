/*
 * Copyright © 2023 Ben Petrillo. All rights reserved.
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

package dev.benpetrillo.elixir;

import com.neovisionaries.ws.client.WebSocketFactory;
import dev.benpetrillo.elixir.api.APIHandler;
import dev.benpetrillo.elixir.events.*;
import dev.benpetrillo.elixir.managers.ApplicationCommandManager;
import dev.benpetrillo.elixir.managers.ConfigStartupManager;
import dev.benpetrillo.elixir.managers.DatabaseManager;
import dev.benpetrillo.elixir.managers.MusicGameManager;
import dev.benpetrillo.elixir.tasks.OAuthUpdateTask;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.xigam.cch.ComplexCommandHandler;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public final class ElixirClient {
    @Getter private static String envFile;
    private static ElixirClient instance;

    public static ComplexCommandHandler commandHandler;
    public static Logger logger
            = LoggerFactory.getLogger("Elixir");

    public JDA jda;

    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error("No environment file specified.");
            System.exit(0);
        }

        ElixirClient.envFile = args[0];

        try {
            ConfigStartupManager.checkAll(); APIHandler.initialize();
            instance = new ElixirClient(ElixirConstants.TOKEN);
        } catch (LoginException | IllegalArgumentException | IOException exception) {
            logger.error("Unable to initiate Elixir Music.", exception);
            System.exit(0);
        }
    }

    private ElixirClient(String token) throws LoginException, IllegalArgumentException, IOException {
        final boolean usePrefix = !ElixirConstants.COMMAND_PREFIX.isEmpty();
        commandHandler = new ComplexCommandHandler(usePrefix).setPrefix(ElixirConstants.COMMAND_PREFIX);

        logger.info("JDA Version: " + Utilities.getJDAVersion());
        final JDABuilder builder = JDABuilder.createDefault(token)
                .setActivity(Activity.listening(ElixirConstants.ACTIVITY))
                .setStatus(OnlineStatus.ONLINE)
                .setAutoReconnect(true)
                .setIdle(false)
                .setHttpClient(new OkHttpClient())
                .setBulkDeleteSplittingEnabled(true)
                .setWebsocketFactory(new WebSocketFactory())
                .addEventListeners(
                        new GuildListener(),
                        new ReadyListener(),
                        new ShutdownListener(),
                        new MusicGameManager.Listener()
                )
                .enableIntents(
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_WEBHOOKS
                );
        if (usePrefix) {
            builder.enableIntents(GatewayIntent.GUILD_MESSAGES);
            logger.info("Prefix support enabled! Prefix: " + ElixirConstants.COMMAND_PREFIX);
        } else {
            Message.suppressContentIntentWarning();
        }

        this.jda = builder.build();
        commandHandler.setJda(this.jda);

        ApplicationCommandManager.initialize();
        OAuthUpdateTask.schedule(); DatabaseManager.create();
    }

    public static Logger getLogger() {
        return logger;
    }

    public static JDA getJda() {
        return instance.jda;
    }

    public static ComplexCommandHandler getCommandHandler() {
        return commandHandler;
    }

    public static ElixirClient getInstance() {
        return instance;
    }
}
