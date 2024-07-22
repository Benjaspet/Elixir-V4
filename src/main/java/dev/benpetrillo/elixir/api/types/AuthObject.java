package dev.benpetrillo.elixir.api.types;

public record AuthObject(String user, String guild) {

  public static AuthObject create(String user, String guild) {
    return new AuthObject(user, guild);
  }
}
