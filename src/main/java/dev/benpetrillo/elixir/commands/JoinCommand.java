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

import dev.benpetrillo.elixir.utilities.DJUtil;
import dev.benpetrillo.elixir.utilities.EmbedUtil;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.managers.AudioManager;
import tech.xigam.cch.command.Command;
import tech.xigam.cch.utils.Interaction;

public final class JoinCommand extends Command {

    public JoinCommand() {
        super("join", "Bind Elixir to your voice channel.");
    }
    
    @Override
    public void execute(Interaction interaction) {
        if(!interaction.isFromGuild()) {
            interaction.reply(EmbedUtil.sendErrorEmbed("This command can only be used in a guild."));
            return;
        }
        final GuildVoiceState voiceState = interaction.getGuild().getSelfMember().getVoiceState();
        final GuildVoiceState memberVoiceState = interaction.getMember().getVoiceState();
        assert memberVoiceState != null; interaction.deferReply();
        if (!memberVoiceState.inAudioChannel()) {
            interaction.reply(EmbedUtil.sendErrorEmbed("You must be in a voice channel to run this command."), false);
            return;
        }
        int continueExec; if ((continueExec = DJUtil.continueExecution(interaction.getGuild(), interaction.getMember())) != -1) {
            interaction.reply(EmbedUtil.sendDefaultEmbed(continueExec + " more people are required to continue."), false);
            return;
        }
        final AudioManager audioManager = interaction.getGuild().getAudioManager();
        final AudioChannel memberChannel = memberVoiceState.getChannel();
        assert voiceState != null;
        if (!voiceState.inAudioChannel()) {
            audioManager.openAudioConnection(memberChannel);
            audioManager.setSelfDeafened(true);
            assert memberChannel != null;
            String name = memberChannel.getName();
            MessageEmbed embed = EmbedUtil.sendDefaultEmbed(String.format("I've connected to **%s** successfully.", name));
            interaction.reply(embed, false);
        } else {
            interaction.reply(EmbedUtil.sendErrorEmbed("I'm already connected to a voice channel."), false);
        }
    }
}
