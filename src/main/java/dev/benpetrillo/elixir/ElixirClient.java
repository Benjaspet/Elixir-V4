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

package dev.benpetrillo.elixir;

import com.neovisionaries.ws.client.WebSocketFactory;
import dev.benpetrillo.elixir.events.ApplicationCommandListener;
import dev.benpetrillo.elixir.events.MessageListener;
import dev.benpetrillo.elixir.events.ReadyListener;
import dev.benpetrillo.elixir.events.ShutdownListener;
import dev.benpetrillo.elixir.managers.ApplicationCommandManager;
import dev.benpetrillo.elixir.managers.ConfigStartupManager;
import dev.benpetrillo.elixir.managers.DatabaseManager;
import dev.benpetrillo.elixir.audio.ElixirVoiceDispatchInterceptor;
import dev.benpetrillo.elixir.tasks.OAuthUpdateTask;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public final class ElixirClient {
    
    public static ApplicationCommandManager applicationCommandManager;
    public static Logger logger = LoggerFactory.getLogger(ElixirClient.class);

    public static void main(String[] args) {
        try {
            new ConfigStartupManager();
            new ElixirClient(ElixirConstants.TOKEN);
        } catch (LoginException | IllegalArgumentException | IOException exception) {
            logger.error("Unable to initiate Elixir Music.", exception);
            System.exit(0);
        }
    }

    private ElixirClient(String token) throws LoginException, IllegalArgumentException, IOException {
        JDA jda = JDABuilder.createDefault(token)
                .setActivity(Activity.listening(ElixirConstants.ACTIVITY))
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .setAutoReconnect(true)
                .setIdle(false)
                .setHttpClient(new OkHttpClient())
                .setVoiceDispatchInterceptor(new ElixirVoiceDispatchInterceptor())
                .setBulkDeleteSplittingEnabled(true)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .setWebsocketFactory(new WebSocketFactory())
                .addEventListeners(
                        new ApplicationCommandListener(),
                        new ReadyListener(),
                        new MessageListener(),
                        new ShutdownListener()
                )
                .enableIntents(
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.GUILD_EMOJIS,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_WEBHOOKS
                )
                .build();
        AllowedMentions.setDefaultMentionRepliedUser(false);
        applicationCommandManager = ApplicationCommandManager.initialize(jda);
        OAuthUpdateTask.schedule(); DatabaseManager.create();
    }

    public static Logger getLogger() {
        return logger;
    }

    public ElixirClient getInstance() {
        return this;
    }
}