package me.ego.ezbd.lib.fo.model;

import com.gmail.nossr50.datatypes.chat.ChatChannel;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.util.player.UserManager;
import java.util.ArrayList;
import java.util.List;
import me.ego.ezbd.lib.fo.Common;
import org.bukkit.entity.Player;

class McMMOHook {
    private boolean errorLogged = false;

    McMMOHook() {
    }

    String getActivePartyChat(Player player) {
        try {
            McMMOPlayer mcplayer = UserManager.getPlayer(player);
            if (mcplayer != null) {
                Party party = mcplayer.getParty();
                ChatChannel channelType = mcplayer.getChatChannel();
                return channelType != ChatChannel.PARTY && (channelType != ChatChannel.PARTY_OFFICER || party == null) ? null : party.getName();
            }
        } catch (Throwable var5) {
            if (!this.errorLogged) {
                Common.log(new String[]{"&cWarning: &fFailed getting mcMMO party chat for " + player.getName() + " due to error. Returning null. Ensure you have the latest mcMMO version, if so, contact plugin authors to update the integration. Error was: " + var5});
                this.errorLogged = true;
            }
        }

        return null;
    }

    List<Player> getPartyRecipients(Player bukkitPlayer) {
        try {
            McMMOPlayer mcplayer = UserManager.getPlayer(bukkitPlayer);
            if (mcplayer != null) {
                Party party = mcplayer.getParty();
                if (party != null) {
                    return party.getOnlineMembers();
                }
            }
        } catch (Throwable var4) {
            if (!this.errorLogged) {
                Common.log(new String[]{"&cWarning: &fFailed getting mcMMO party recipients for " + bukkitPlayer.getName() + " due to error. Returning null. Ensure you have the latest mcMMO version, if so, contact plugin authors to update the integration. Error was: " + var4});
                this.errorLogged = true;
            }
        }

        return new ArrayList();
    }
}