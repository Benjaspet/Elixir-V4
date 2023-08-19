package dev.benpetrillo.elixir.music.laudiolin;

import com.google.gson.JsonObject;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;

import java.util.HashMap;
import java.util.Map;

public interface LaudiolinMessages {
    Map<String, LaudiolinMessages> HANDLERS = new HashMap<>() {{
        this.put("initialize", LaudiolinMessages::initialize);
        this.put("playTrack", LaudiolinMessages::playTrack);
        this.put("resume", LaudiolinMessages::resume);
        this.put("pause", LaudiolinMessages::pause);
    }};

    /**
     * Event method.
     * Fires when the server sends a message.
     * This message is JSON-encoded, and should be decoded by the handler.
     *
     * @param handle The session that received the message.
     * @param message The message that was sent.
     */
    void handle(LaudiolinInterface handle, JsonObject message);

    /**
     * Handles the server's request to initialize the client.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void initialize(LaudiolinInterface handle, JsonObject content) {
        var guild = handle.getGuild();

        handle.send(new LaudiolinTypes.Initialize(
                ElixirConstants.LAUDIOLIN_TOKEN,
                guild.getId()
        ));

        handle.getLogger().debug("Guild '{}' ({}) has finished initializing.",
                guild.getName(), guild.getId());
    }

    /**
     * Handles the server's request to play/queue a track.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void playTrack(LaudiolinInterface handle, JsonObject content) {
        var message = Utilities.deserialize(content, LaudiolinTypes.PlayTrack.class);

        // Play the track in the guild.
        var track = message.getTrack().toAudioItem();
        handle.getManager().play(track);
    }

    /**
     * Handles the server's request to resume the player.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void resume(LaudiolinInterface handle, JsonObject content) {
        handle.getManager().getAudioPlayer().setPaused(false);
    }

    /**
     * Handles the server's request to pause the player.
     *
     * @param handle The session that received the message.
     * @param content The message that was sent.
     */
    static void pause(LaudiolinInterface handle, JsonObject content) {
        handle.getManager().getAudioPlayer().setPaused(true);
    }
}
