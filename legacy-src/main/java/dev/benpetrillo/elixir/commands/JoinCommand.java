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

import dev.benpetrillo.elixir.types.ApplicationCommand;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;

public final class JoinCommand implements ApplicationCommand {

    private final String name = "join";
    private final String description = "Bind Elixir to your voice channel.";
    private final String[] options = {};
    private final String[] optionDescriptions = {};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        final TextChannel channel = event.getTextChannel();
        final GuildVoiceState voiceState = channel.getGuild().getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        assert memberVoiceState != null;
        event.deferReply().queue(hook -> {
            if (!memberVoiceState.inAudioChannel()) {
                hook.editOriginalEmbeds(EmbedUtil.sendErrorEmbed("You must be in a voice channel to run this command.")).queue();
                return;
            }
            final AudioManager audioManager = channel.getGuild().getAudioManager();
            final AudioChannel memberChannel = memberVoiceState.getChannel();
            assert voiceState != null;
            if (!voiceState.inAudioChannel()) {
                audioManager.openAudioConnection(memberChannel);
                audioManager.setSelfDeafened(true);
                assert memberChannel != null;
                String name = memberChannel.getName();
                MessageEmbed embed = EmbedUtil.sendDefaultEmbed(String.format("I've connected to **%s** successfully.", name));
                hook.editOriginalEmbeds(embed).queue();
            }
        });
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
        return new CommandData(this.name, this.description);
    }
}
