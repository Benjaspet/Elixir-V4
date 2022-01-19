package dev.benpetrillo.elixir.commands;

import dev.benpetrillo.elixir.types.ApplicationCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public final class LoopCommand implements ApplicationCommand {

    private final String name = "loop";
    private final String description = "Loop a song or queue.";
    private final String[] options = {"mode"}; 
    private final String[] optionDescriptions = {"The type of loop to apply."};
    
    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        
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
