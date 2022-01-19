package dev.benpetrillo.elixir.commands;

import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.music.TrackScheduler;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Objects;

public final class LoopCommand implements ApplicationCommand {

    private final String name = "loop";
    private final String description = "Loop a song or queue.";
    private final String[] options = {"mode"}; 
    private final String[] optionDescriptions = {"The type of loop to apply."};
    
    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(EmbedUtil.sendErrorEmbed("I must be in a voice channel.")).queue();
            return;
        }
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.replyEmbeds(EmbedUtil.sendErrorEmbed("You must be in a voice channel.")).queue();
            return;
        }
        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            event.replyEmbeds(EmbedUtil.sendErrorEmbed("You need to be in my voice channel.")).queue();
            return;
        }
        final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(member.getGuild());
        if(musicManager.audioPlayer.getPlayingTrack() == null) {
            event.replyEmbeds(EmbedUtil.sendErrorEmbed("There's no queue in this server.")).queue();
            return;
        }   
        final TrackScheduler scheduler = musicManager.scheduler;
        
        String loop = Objects.requireNonNull(event.getOption("mode")).getAsString(); String mode;
        switch(loop) {
            case "Track Loop":
                scheduler.repeating = TrackScheduler.LoopMode.TRACK; mode = "track";
                break;
            case "Queue Loop":
                scheduler.repeating = TrackScheduler.LoopMode.QUEUE; mode = "queue";
                break;
            case "Autoplay":
                mode = "autoplay"; // TODO: Implement autoplay.
                break;
            case "Disable Loop":
                scheduler.repeating = TrackScheduler.LoopMode.NONE;
                event.replyEmbeds(EmbedUtil.sendDefaultEmbed("Turned **off** repeat mode.")).queue();
                return;
            default:
                return;
        }
        event.replyEmbeds(EmbedUtil.sendDefaultEmbed("Set the loop mode to **%s**.".formatted(mode))).queue();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData(this.name, this.description)
                .addOptions(new OptionData(OptionType.STRING, this.options[0], this.optionDescriptions[0], true)
                        .addChoice("Track Loop", "Track Loop")
                        .addChoice("Queue Loop", "Queue Loop")
                        .addChoice("Autoplay", "Autoplay")
                        .addChoice("Disable Loop", "Disable Loop"));
    }
}
