package me.ego.ezbd.lib.fo.model;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import java.util.UUID;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

class LWCHook {
    LWCHook() {
    }

    String getOwner(Block block) {
        if (!LWC.ENABLED) {
            return null;
        } else {
            Protection protection = LWC.getInstance().findProtection(block);
            if (protection != null) {
                String uuid = protection.getOwner();
                if (uuid != null) {
                    OfflinePlayer opl = Remain.getOfflinePlayerByUUID(UUID.fromString(uuid));
                    if (opl != null) {
                        return opl.getName();
                    }
                }
            }

            return null;
        }
    }
}