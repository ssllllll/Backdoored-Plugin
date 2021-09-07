package me.ego.ezbd.lib.fo.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.ego.ezbd.lib.fo.model.HookManager;
import me.ego.ezbd.lib.fo.model.SimpleEnchantment;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.bukkit.inventory.ItemStack;

final class FoundationPacketListener {
    FoundationPacketListener() {
    }

    static void addPacketListener() {
        if (HookManager.isProtocolLibLoaded()) {
            HookManager.addPacketListener(new PacketAdapter(SimplePlugin.getInstance(), new PacketType[]{Server.SET_SLOT}) {
                public void onPacketSending(PacketEvent event) {
                    StructureModifier<ItemStack> itemModifier = event.getPacket().getItemModifier();
                    ItemStack item = (ItemStack)itemModifier.read(0);
                    if (item != null && !CompMaterial.isAir(item.getType())) {
                        item = SimpleEnchantment.addEnchantmentLores(item);
                        if (item != null) {
                            itemModifier.write(0, item);
                        }
                    }

                }
            });
        }

    }
}