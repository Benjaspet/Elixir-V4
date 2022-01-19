package dev.benpetrillo.elixir;

import core.GLA;
import dev.benpetrillo.elixir.events.ApplicationCommandListener;
import dev.benpetrillo.elixir.events.ReadyListener;
import dev.benpetrillo.elixir.managers.ApplicationCommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

public final class ElixirClient {

    public static ApplicationCommandManager applicationCommandManager;
    public static Logger logger = LoggerFactory.getLogger(ElixirClient.class);
    public static GLA gla = new GLA();

    public static void main(String[] args) {
        try {
            new ElixirClient(Config.get("TOKEN"));
        } catch (LoginException | IllegalArgumentException | InterruptedException | IOException | ParseException | SpotifyWebApiException ignored) {
            logger.error("Unable to log into the bot. Is the token valid?");
        }
    }

    private ElixirClient(String token) throws LoginException, IllegalArgumentException, InterruptedException, IOException, ParseException, SpotifyWebApiException {
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