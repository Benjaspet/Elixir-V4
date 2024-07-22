package dev.benpetrillo.elixir.api.response;

public record StopPlayerResponse(String guild, String user, String message) {

  public static StopPlayerResponse create(String guild, String user, String message) {
    return new StopPlayerResponse(guild, user, message);
  }
}
