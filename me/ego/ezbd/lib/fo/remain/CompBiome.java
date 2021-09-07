package me.ego.ezbd.lib.fo.remain;

import java.util.Iterator;
import java.util.Map.Entry;
import me.ego.ezbd.lib.fo.collection.StrictMap;
import org.bukkit.block.Biome;

/** @deprecated */
@Deprecated
public final class CompBiome {
    private static final StrictMap<Biome, Integer> map = new StrictMap();

    private CompBiome() {
    }

    public static Biome getBiomeByID(int id) {
        Iterator var1 = map.entrySet().iterator();

        Entry e;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            e = (Entry)var1.next();
        } while((Integer)e.getValue() != id);

        return (Biome)e.getKey();
    }

    public static byte getBiomeID(Biome biome) {
        return map.contains(biome) ? ((Integer)map.get(biome)).byteValue() : 0;
    }

    private static void add(String biome, int id) {
        try {
            map.put(Biome.valueOf(biome), id);
        } catch (Throwable var3) {
        }

    }

    static {
        add("PLAINS", 1);
        add("FOREST_HILLS", 18);
        add("TAIGA", 5);
        add("BEACHES", 16);
        add("SAVANNA_ROCK", 36);
        add("REDWOOD_TAIGA_HILLS", 33);
        add("MUTATED_TAIGA_COLD", -98);
        add("OCEAN", 0);
        add("COLD_BEACH", 26);
        add("ICE_FLATS", 12);
        add("MUTATED_SAVANNA", -93);
        add("DEEP_OCEAN", 24);
        add("MUTATED_SAVANNA_ROCK", -92);
        add("MUTATED_ICE_FLATS", -116);
        add("MUTATED_JUNGLE", -107);
        add("HELL", 8);
        add("MUTATED_FOREST", -124);
        add("MESA_CLEAR_ROCK", 39);
        add("MUTATED_SWAMPLAND", -122);
        add("TAIGA_HILLS", 19);
        add("SWAMPLAND", 6);
        add("BIRCH_FOREST_HILLS", 28);
        add("MUTATED_REDWOOD_TAIGA_HILLS", -95);
        add("RIVER", 7);
        add("SNOWY_TAIGA", 10);
        add("FROZEN_OCEAN", 10);
        add("MUTATED_PLAINS", -127);
        add("TAIGA_COLD", 30);
        add("MESA_ROCK", 38);
        add("JUNGLE_HILLS", 22);
        add("MUTATED_TAIGA", -123);
        add("FROZEN_RIVER", 11);
        add("STONE_BEACH", 25);
        add("MUTATED_BIRCH_FOREST_HILLS", -100);
        add("MUTATED_REDWOOD_TAIGA", -96);
        add("MUTATED_MESA_ROCK", -90);
        add("MUTATED_MESA_CLEAR_ROCK", -89);
        add("JUNGLE_EDGE", 23);
        add("MUTATED_EXTREME_HILLS_WITH_TREES", -94);
        add("EXTREME_HILLS_WITH_TREES", 34);
        add("DESERT_HILLS", 17);
        add("REDWOOD_TAIGA", 32);
        add("BIRCH_FOREST", 27);
        add("VOID", 127);
        add("MUTATED_JUNGLE_EDGE", -105);
        add("JUNGLE", 21);
        add("MUTATED_ROOFED_FOREST", -99);
        add("ROOFED_FOREST", 29);
        add("MUSHROOM_ISLAND", 14);
        add("EXTREME_HILLS", 3);
        add("MESA", 37);
        add("MUTATED_BIRCH_FOREST", -101);
        add("SMALLER_EXTREME_HILLS", 20);
        add("MUSHROOM_ISLAND_SHORE", 15);
        add("MUTATED_MESA", -91);
        add("SKY", 9);
        add("FOREST", 4);
        add("DESERT", 2);
        add("SAVANNA", 35);
        add("MUTATED_EXTREME_HILLS", -125);
        add("ICE_MOUNTAINS", 13);
        add("MUTATED_DESERT", -126);
        add("TAIGA_COLD_HILLS", 31);
    }
}