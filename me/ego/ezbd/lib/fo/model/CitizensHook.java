package me.ego.ezbd.lib.fo.model;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.entity.Entity;

class CitizensHook {
    CitizensHook() {
    }

    boolean isNPC(Entity entity) {
        NPCRegistry reg = CitizensAPI.getNPCRegistry();
        return reg != null ? reg.isNPC(entity) : false;
    }
}