package me.ego.ezbd.lib.fo.settings;

import java.util.Arrays;
import java.util.List;
import me.ego.ezbd.lib.fo.collection.StrictMap;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

final class LegacyEnum {
    private static final StrictMap<Class<? extends Enum<?>>, List<String>> INCOMPATIBLE_TYPES = new StrictMap();

    LegacyEnum() {
    }

    public static <T extends Enum<T>> boolean isIncompatible(Class<T> type, String enumName) {
        List<String> types = (List)INCOMPATIBLE_TYPES.get(type);
        return types != null && types.contains(enumName.toUpperCase().replace(" ", "_"));
    }

    static {
        INCOMPATIBLE_TYPES.put(SpawnReason.class, Arrays.asList("DROWNED"));
    }
}