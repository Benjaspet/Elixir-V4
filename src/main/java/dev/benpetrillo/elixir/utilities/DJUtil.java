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

package dev.benpetrillo.elixir.utilities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.HashMap;
import java.util.Map;

public final class DJUtil {

    private static final Map<String, DJInformation> roleCache = new HashMap<>();

    /**
     * Check if a member is a DJ.
     * @param guild The guild to check in.
     * @param member The member to check.
     * @return -1 if execution should continue, a number greater than 0 if execution is awaiting further members.
     */
    
    public static int continueExecution(Guild guild, Member member) {
        return -1;
        
//        var cache = DJUtil.roleCache.computeIfAbsent(guild.getId(), guildId -> {
//            var collection = DatabaseManager.getDjRoleCollection();
//            Bson dbObject = new BasicDBObject("guildId", guildId);
//            Document roleObject = collection.find(dbObject).first();
//            
//            if(roleObject == null) {
//                guild.createRole().setName("DJ").queue(role -> {
//                    collection.insertOne(new Document("guildId", guildId)
//                            .append("djRole", role.getId())
//                            .append("maxContinues", 3)
//                            .append("useDjRole", true));
//                    roleCache.get(guildId).djRole = role;
//                });
//                
//                return new DJInformation();
//            }
//            
//            var information = new DJInformation();
//            information.djRole = guild.getRoleById(roleObject.getString("djRole"));
//            information.maxContinue = roleObject.getInteger("maxContinues");
//            information.useDjRole = roleObject.getBoolean("useDjRole");
//            return information;
//        }); if(!cache.useDjRole) return -1;
//        
//        boolean alone = member.getVoiceState().getChannel().getMembers().size() != 3;
//        if(cache.djRole != null && !member.getRoles().contains(cache.djRole) && !alone) {
//            cache.currentContinue++;
//            if(cache.currentContinue >= cache.maxContinue) {
//                cache.currentContinue = 0; return -1;
//            }
//        } else return -1;
//        
//        return cache.maxContinue - cache.currentContinue;
    }
    
    private static class DJInformation {
        public Role djRole;
        public int maxContinue = 3;
        public int currentContinue = 0;
        public boolean useDjRole = true;
    }
}