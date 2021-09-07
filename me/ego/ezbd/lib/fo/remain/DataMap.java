package me.ego.ezbd.lib.fo.remain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import org.bukkit.entity.EntityType;

final class DataMap {
    private static final Map<Integer, String> map = new HashMap();

    DataMap() {
    }

    static EntityType getEntity(int data) {
        String name = (String)map.get(data);
        return name != null ? (EntityType)ReflectionUtil.lookupEnumSilent(EntityType.class, name.toUpperCase()) : null;
    }

    static int getData(EntityType type) {
        Integer data = getKeyFromValue(type.toString());
        return data != null ? data : -1;
    }

    private static Integer getKeyFromValue(String value) {
        Iterator var1 = map.entrySet().iterator();

        Entry e;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            e = (Entry)var1.next();
        } while(!((String)e.getValue()).equals(value));

        return (Integer)e.getKey();
    }

    static {
        map.put(1, "DROPPED_ITEM");
        map.put(2, "EXPERIENCE_ORB");
        map.put(3, "AREA_EFFECT_CLOUD");
        map.put(4, "ELDER_GUARDIAN");
        map.put(5, "WITHER_SKELETON");
        map.put(6, "STRAY");
        map.put(7, "EGG");
        map.put(8, "LEASH_HITCH");
        map.put(9, "PAINTING");
        map.put(10, "ARROW");
        map.put(11, "SNOWBALL");
        map.put(12, "FIREBALL");
        map.put(13, "SMALL_FIREBALL");
        map.put(14, "ENDER_PEARL");
        map.put(15, "ENDER_SIGNAL");
        map.put(16, "SPLASH_POTION");
        map.put(17, "THROWN_EXP_BOTTLE");
        map.put(18, "ITEM_FRAME");
        map.put(19, "WITHER_SKULL");
        map.put(20, "PRIMED_TNT");
        map.put(21, "FALLING_BLOCK");
        map.put(22, "FIREWORK");
        map.put(23, "HUSK");
        map.put(24, "SPECTRAL_ARROW");
        map.put(25, "SHULKER_BULLET");
        map.put(26, "DRAGON_FIREBALL");
        map.put(27, "ZOMBIE_VILLAGER");
        map.put(28, "SKELETON_HORSE");
        map.put(29, "ZOMBIE_HORSE");
        map.put(30, "ARMOR_STAND");
        map.put(31, "DONKEY");
        map.put(32, "MULE");
        map.put(33, "EVOKER_FANGS");
        map.put(34, "EVOKER");
        map.put(35, "VEX");
        map.put(36, "VINDICATOR");
        map.put(37, "ILLUSIONER");
        map.put(40, "MINECART_COMMAND");
        map.put(41, "BOAT");
        map.put(42, "MINECART");
        map.put(43, "MINECART_CHEST");
        map.put(44, "MINECART_FURNACE");
        map.put(45, "MINECART_TNT");
        map.put(46, "MINECART_HOPPER");
        map.put(47, "MINECART_MOB_SPAWNER");
        map.put(50, "CREEPER");
        map.put(51, "SKELETON");
        map.put(52, "SPIDER");
        map.put(53, "GIANT");
        map.put(54, "ZOMBIE");
        map.put(55, "SLIME");
        map.put(56, "GHAST");
        map.put(57, "PIG_ZOMBIE");
        map.put(58, "ENDERMAN");
        map.put(59, "CAVE_SPIDER");
        map.put(60, "SILVERFISH");
        map.put(61, "BLAZE");
        map.put(62, "MAGMA_CUBE");
        map.put(63, "ENDER_DRAGON");
        map.put(64, "WITHER");
        map.put(65, "BAT");
        map.put(66, "WITCH");
        map.put(67, "ENDERMITE");
        map.put(68, "GUARDIAN");
        map.put(69, "SHULKER");
        map.put(90, "PIG");
        map.put(91, "SHEEP");
        map.put(92, "COW");
        map.put(93, "CHICKEN");
        map.put(94, "SQUID");
        map.put(95, "WOLF");
        map.put(96, "MUSHROOM_COW");
        map.put(97, "SNOWMAN");
        map.put(98, "OCELOT");
        map.put(99, "IRON_GOLEM");
        map.put(100, "HORSE");
        map.put(101, "RABBIT");
        map.put(102, "POLAR_BEAR");
        map.put(103, "LLAMA");
        map.put(104, "LLAMA_SPIT");
        map.put(105, "PARROT");
        map.put(120, "VILLAGER");
    }
}