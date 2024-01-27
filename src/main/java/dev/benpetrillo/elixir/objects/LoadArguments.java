package dev.benpetrillo.elixir.objects;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import dev.benpetrillo.elixir.types.laudiolin.Source;

public record LoadArguments(
        AudioPlayerManager manager,
        AudioReference reference,
        Source originalSource
) {
}
