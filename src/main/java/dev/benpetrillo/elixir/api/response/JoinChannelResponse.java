package dev.benpetrillo.elixir.api.response;

public record JoinChannelResponse(String guild, String channel, String user, String message) {

  public static JoinChannelResponse create(String guild, String channel, String user, String message) {
    return new JoinChannelResponse(guild, channel, user, message);
  }
}
