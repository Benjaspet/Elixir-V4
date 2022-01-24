package dev.benpetrillo.elixir.audio;

import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import org.jetbrains.annotations.NotNull;

public final class ElixirVoiceDispatchInterceptor implements VoiceDispatchInterceptor {

    public ElixirVoiceDispatchInterceptor() {
        super();
    }

    @Override
    public void onVoiceServerUpdate(@NotNull VoiceDispatchInterceptor.VoiceServerUpdate voiceServerUpdate) {}

    @Override
    public boolean onVoiceStateUpdate(@NotNull VoiceDispatchInterceptor.VoiceStateUpdate voiceStateUpdate) {
        return true;
    }
}