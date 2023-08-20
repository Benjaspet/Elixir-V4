package dev.benpetrillo.elixir.music.laudiolin;

import ch.qos.logback.classic.Level;
import com.google.gson.JsonObject;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public final class LaudiolinInterface extends WebSocketClient {
    private final Logger logger;
    private final Guild guild;
    private final GuildMusicManager manager;

    public LaudiolinInterface(GuildMusicManager manager, Guild guild) {
        super(ElixirConstants.LAUDIOLIN_API);

        this.guild = guild;
        this.manager = manager;
        this.logger = LoggerFactory.getLogger(guild.getName());

        if (ElixirConstants.DEBUG) {
            // Set the logger in debug mode.
            ((ch.qos.logback.classic.Logger) this.logger)
                    .setLevel(Level.DEBUG);
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

        this.send(Utilities.serialize(tree));
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
