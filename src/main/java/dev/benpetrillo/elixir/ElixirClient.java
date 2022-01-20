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

import dev.benpetrillo.elixir.events.ApplicationCommandListener;
import dev.benpetrillo.elixir.events.ReadyListener;
import dev.benpetrillo.elixir.managers.ApplicationCommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

import java.io.IOException;

public final class ElixirClient {

    public static ApplicationCommandManager applicationCommandManager;
    public static Logger logger = LoggerFactory.getLogger(ElixirClient.class);

    public static void main(String[] args) {
        try {
            new ElixirClient(Config.get("TOKEN"));
        } catch (LoginException | IllegalArgumentException | IOException ignored) {
            logger.error("Unable to log into the bot. Is the token valid?");
        }
    }

    private ElixirClient(String token) throws LoginException, IllegalArgumentException, IOException {
        JDA jda = JDABuilder.createDefault(token)
                .setActivity(Activity.listening("lofi hiphop!"))
                .addEventListeners(
                        new ReadyListener(),
                        new ApplicationCommandListener()
                )
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .build();
        AllowedMentions.setDefaultMentionRepliedUser(false);
        applicationCommandManager = ApplicationCommandManager.initialize(jda);
    }

    public ElixirClient getInstance() {
        return this;
    }
}