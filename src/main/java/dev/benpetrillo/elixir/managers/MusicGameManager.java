package dev.benpetrillo.elixir.managers;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MusicGameManager {
    private static final Map<Guild, MusicGame> instances
            = new ConcurrentHashMap<>();

    /**
     * Creates a new instance of the music game.
     *
     * @param guild The guild to create the instance for.
     */
    public static MusicGame newGame(Guild guild, AudioChannel channel, String playlist) {
        var game = new MusicGame(guild, channel, playlist);
        instances.put(guild, game);
        return game;
    }

    /**
     * Handles a message sent by a user.
     *
     * @param user The user who sent the message.
     * @param message The message sent.
     */
    public static void handleMessage(User user, String message) {
        // Check if a user is in a game.
        var game = instances.values().stream()
                .filter(g -> g.getPlayingUsers().contains(user))
                .findFirst().orElse(null);
        if (game == null) return;

        // Handle the message.
        game.handleMessage(user, message);
    }

    /**
     * @param guild The guild to get the instance for.
     * @return If the guild has a game instance or not.
     */
    public static boolean hasGame(Guild guild) {
        return instances.containsKey(guild);
    }

    public static final class Listener extends ListenerAdapter {
        @Override
        public void onMessageReceived(
                @NotNull MessageReceivedEvent event
        ) {
            MusicGameManager.handleMessage(
                    event.getAuthor(),
                    event.getMessage().getContentRaw());
        }
    }

    @RequiredArgsConstructor
    public static final class Task extends TimerTask {
        private final MusicGame instance;

        @Override
        public void run() {
            this.instance.end();
        }
    }

    @Data
    public static final class MusicGame {
        private static final MessageEmbed START_EMBED = EmbedUtil.sendDefaultEmbed(
                """
                        Welcome to the music game!
                        The bot will begin to play a song, and you have to guess the name of the song.
                        To submit a guess, send a message here with your song guess!
                        """
        );

        private final Guild guild;
        private final AudioChannel channel;
        private final AudioManager manager;
        private final String playlist;

        private final List<Member> players = new ArrayList<>();
        private final List<User> playingUsers = new ArrayList<>();

        private final Map<User, Integer> guesses = new ConcurrentHashMap<>();

        private AudioTrackInfo currentTrack = null;
        private String trackTitle = null;

        public MusicGame(Guild guild, AudioChannel channel, String playlist) {
            this.guild = guild;
            this.channel = channel;
            this.playlist = playlist;

            this.manager = guild.getAudioManager();
        }

        /**
         * Starts the game.
         */
        public void start(Collection<Member> players) {
            this.players.addAll(players);
            this.playingUsers.addAll(this.players.stream()
                    .map(Member::getUser)
                    .toList());

            // Message all members.
            this.players.stream()
                    .map(Member::getUser)
                    .map(User::openPrivateChannel)
                    .map(RestAction::complete)
                    .forEach(channel -> channel
                            .sendMessageEmbeds(START_EMBED).queue());

            // Get the audio player.
            var musicManager = ElixirMusicManager.getInstance()
                    .getMusicManager(this.getGuild());
            var player = musicManager.audioPlayer;
            var scheduler = musicManager.scheduler;
            ElixirMusicManager.getInstance().loadAndPlay(
                    guild, this.getPlaylist(), object -> {
                        // Pick the next song.
                        player.setPaused(true);
                        scheduler.shuffle();
                        scheduler.nextTrack();
                        player.setPaused(false);

                        // Set the track.
                        this.currentTrack = player.getPlayingTrack().getInfo();
                        this.trackTitle = this.currentTrack.title.toLowerCase();
                    }
            );
        }

        /**
         * Ends the game.
         */
        public void end() {

        }

        /**
         * Handles a message sent by a user.
         *
         * @param user The user who sent the message.
         * @param message The message sent.
         */
        public void handleMessage(User user, String message) {
            // Check the length of the message.
            if (message.length() > this.trackTitle.length()) return;

            // Compute how many words in the title are in the message.
            var title = this.trackTitle.split(" ");
            var count = 0;
            for (var word : title) {
                if (message.contains(word)) count++;
            }

            // Update the user's score.
            this.guesses.put(user, count);
        }
    }
}
