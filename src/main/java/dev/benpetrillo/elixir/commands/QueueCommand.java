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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.benpetrillo.elixir.managers.ElixirMusicManager;
import dev.benpetrillo.elixir.managers.GuildMusicManager;
import dev.benpetrillo.elixir.types.ApplicationCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.concurrent.BlockingQueue;

public final class QueueCommand implements ApplicationCommand {
    
    private final String name = "queue";
    private final String description = "View all songs in the queue.";
    private final String[] options = {};
    private final String[] optionDescriptions = {};

    @Override
    public void runCommand(SlashCommandEvent event, Member member, Guild guild) {
        event.deferReply().queue(hook -> {
            try {
                final GuildMusicManager musicManager = ElixirMusicManager.getInstance().getMusicManager(member.getGuild());
                final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
                hook.editOriginal("Queue length: " + queue.size()).queue();
//                String queueDescription = "";
//                for (AudioTrack track : queue) {
//                    queueDescription += track.getInfo().title;
//                }
            } catch (PermissionException ignored) {

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
