package dev.benpetrillo.elixir.events;

import dev.benpetrillo.elixir.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message.contains(String.format("<@%s>", Config.get("CLIENT-ID")))) {
            MessageEmbed embed1 = new EmbedBuilder()
                    .setTitle("Elixir Music")
                    .setDescription("")
                    .build();
            MessageEmbed embed2 = new EmbedBuilder()
                    .build();
            event.getMessage().replyEmbeds(embed1, embed2).queue();
            // TODO: send information/help embed.
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        
    }
}
