package dev.benpetrillo.elixir.music.laudiolin;

import ch.qos.logback.classic.Level;
import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerResumeEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.music.TrackScheduler;
import dev.benpetrillo.elixir.types.laudiolin.LaudiolinTrackInfo;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

@Getter
public final class LaudiolinInterface extends WebSocketClient {
    private final Logger logger;
    private final Guild guild;

    private final GuildMusicManager manager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;

    private final Timer timer = new Timer();

    private int lastVolume = 100;
    private boolean lastPaused = false;
    private AudioTrackInfo lastTrack = null;

    public LaudiolinInterface(GuildMusicManager manager, Guild guild) {
        super(ElixirConstants.LAUDIOLIN_API);

        this.guild = guild;
        this.manager = manager;
        this.player = manager.getAudioPlayer();
        this.scheduler = manager.getScheduler();

        this.logger = LoggerFactory.getLogger(guild.getName());

        if (ElixirConstants.DEBUG) {
            // Set the logger in debug mode.
            ((ch.qos.logback.classic.Logger) this.logger)
                    .setLevel(Level.DEBUG);
        }
    }

    @Override
    public void connect() {
        super.connect(); // Connect to the web socket.

        // Schedule a task to update the backend.
        this.getTimer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LaudiolinInterface.this.update();
            }
        }, 0, 100);

        // Add event listeners.
        var player = this.getPlayer();
        player.addListener(event -> {
            if (event instanceof PlayerPauseEvent) {
                this.send(new LaudiolinTypes.Paused(true));
            } else if (event instanceof PlayerResumeEvent) {
                this.send(new LaudiolinTypes.Paused(false));
            } else if (event instanceof TrackStartEvent startEvent) {
                this.send(new LaudiolinTypes.Playing(
                        LaudiolinTrackInfo.from(startEvent.track)));
            } else if (event instanceof TrackEndEvent endEvent) {
                if (endEvent.endReason.mayStartNext) return;
                this.send(new LaudiolinTypes.Playing(null));
            }
        });
    }

    @Override
    public void close() {
        super.close(); // Close the web socket.

        // Cancel the update task.
        this.getTimer().cancel();
    }

    /**
     * Updates the backend with useful information.
     */
    private void update() {
        var player = this.getPlayer();
        var track = player.getPlayingTrack();

        // Update the current track position.
        if (track != null) {
            this.send(new LaudiolinTypes.Seek(
                    track.getPosition() / 1000f));
        }

        // Update the current volume.
        if (player.getVolume() != this.getLastVolume()) {
            this.send(new LaudiolinTypes.Volume(player.getVolume()));
            this.lastVolume = player.getVolume();
        }
    }

    /**
     * Sends a serialized message to the Laudiolin backend.
     *
     * @param data The message to send.
     */
    public void send(Object data) {
        // Determine the type to send.
        var typeName = data.getClass().getSimpleName();
        // De-capitalize the first letter.
        typeName = typeName.substring(0, 1).toLowerCase()
                + typeName.substring(1);

        // Serialize the message.
        var tree = Utilities.tree(data).getAsJsonObject();
        tree.addProperty("type", typeName);

        if (this.isOpen()) {
            this.send(Utilities.serialize(tree));
        }
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        this.logger.debug("Guild '{}' ({}) connected to the Laudiolin backend.",
                this.guild.getName(), this.guild.getId());
    }

    @Override
    public void onMessage(String message) {
        var data = Utilities.deserialize(message, JsonObject.class);
        this.logger.debug("Received message: {}.", data);

        try {
            var type = data.get("type").getAsString();
            var handler = LaudiolinMessages.HANDLERS.get(type);
            if (handler == null) {
                throw new Exception("Invalid message type.");
            }

            handler.handle(this, data); // Handle the message.
        } catch (Exception exception) {
            this.logger.error("Guild '{}' ({}) received an invalid message from the Laudiolin backend.",
                    this.guild.getName(), this.guild.getId(), exception);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.logger.debug("Guild '{}' ({}) disconnected from the Laudiolin backend.",
                this.guild.getName(), this.guild.getId());
    }

    @Override
    public void onError(Exception ex) {
        this.logger.error("Guild '{}' ({}) disconnected from the Laudiolin backend.",
                this.guild.getName(), this.guild.getId(), ex);
    }
}
