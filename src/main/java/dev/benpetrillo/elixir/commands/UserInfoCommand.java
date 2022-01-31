/*
 * Copyright © 2022 Ben Petrillo. All rights reserved.
 *
 * Project licensed under the MIT License: https://www.mit.edu/~amini/LICENSE.md
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * All portions of this software are available for public use, provided that
 * credit is given to the original author(s).
 */

package dev.benpetrillo.elixir.commands;

import dev.benpetrillo.elixir.ElixirClient;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import tech.xigam.cch.command.Arguments;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Argument;
import tech.xigam.cch.utils.Interaction;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public final class UserInfoCommand extends Command implements Arguments {

    public UserInfoCommand() {
        super("userinfo", "Get information about a member.");
    }

    @Override
    public void execute(Interaction interaction) {
        User user = interaction.getArgument("member", User.class);
        Member member = interaction.getGuild().getMember(user);
        if (member != null) {
            final long epoc = member.getTimeJoined().toInstant().getEpochSecond();
            final long createdAt = member.getUser().getTimeCreated().toInstant().getEpochSecond();
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(EmbedUtil.getDefaultEmbedColor())
                    .setAuthor(member.getUser().getAsTag())
                    .setThumbnail(member.getUser().getAvatarUrl())
                    .setDescription("Created: <t:" + createdAt + ":R>" + "\n" + "Joined: " + "<t:" + epoc + ":R>")
                    .addField("User ID", member.getUser().getId(), false)
                    .setFooter("Elixir Music", ElixirClient.getJda().getSelfUser().getAvatarUrl())
                    .setTimestamp(new Date().toInstant())
                    .build();
            interaction.reply(embed);
        } else {
            interaction.setEphemeral()
                    .reply(EmbedUtil.sendErrorEmbed("Due to Discord's API limitations, I cannot get information about this user."));
        }
    }

    @Override
    public Collection<Argument> getArguments() {
        return List.of(Argument.create("member", "The member to view.",
                "member", OptionType.USER, true, 0));
    }
}
