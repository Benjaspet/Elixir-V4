package dev.benpetrillo.elixir.api.response;

public record GeneralPlayerResponse(String guild, String user, String message) {

  public static GeneralPlayerResponse create(String guild, String user, String message) {
    return new GeneralPlayerResponse(guild, user, message);
  }
}
