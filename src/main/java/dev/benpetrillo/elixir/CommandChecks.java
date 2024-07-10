package dev.benpetrillo.elixir;

import dev.benpetrillo.elixir.utilities.EmbedUtil;
import tech.xigam.cch.utils.Interaction;

public class CommandChecks {

  public static void runIsInGuildCheck(Interaction interaction) {
    if (!interaction.isFromGuild()) {
      interaction.reply(EmbedUtil.sendErrorEmbed("This command can only be used in a guild."));
    }
    assert interaction.getGuild() != null;
  }
}
