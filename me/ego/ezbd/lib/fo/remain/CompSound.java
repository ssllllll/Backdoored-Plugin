package me.ego.ezbd.lib.fo.remain;

import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum CompSound {
    AMBIENCE_CAVE(new String[]{"AMBIENCE_CAVE", "AMBIENT_CAVE"}),
    AMBIENCE_RAIN(new String[]{"AMBIENCE_RAIN", "WEATHER_RAIN"}),
    AMBIENCE_THUNDER(new String[]{"AMBIENCE_THUNDER", "ENTITY_LIGHTNING_THUNDER", "ENTITY_LIGHTNING_BOLT_THUNDER"}),
    ANVIL_BREAK(new String[]{"ANVIL_BREAK", "BLOCK_ANVIL_BREAK"}),
    ANVIL_LAND(new String[]{"ANVIL_LAND", "BLOCK_ANVIL_LAND"}),
    ANVIL_USE(new String[]{"ANVIL_USE", "BLOCK_ANVIL_USE"}),
    ARROW_HIT(new String[]{"ARROW_HIT", "ENTITY_ARROW_HIT"}),
    BURP(new String[]{"BURP", "ENTITY_PLAYER_BURP"}),
    CHEST_CLOSE(new String[]{"CHEST_CLOSE", "ENTITY_CHEST_CLOSE", "BLOCK_CHEST_CLOSE"}),
    CHEST_OPEN(new String[]{"CHEST_OPEN", "ENTITY_CHEST_OPEN", "BLOCK_CHEST_OPEN"}),
    CLICK(new String[]{"CLICK", "UI_BUTTON_CLICK"}),
    DOOR_CLOSE(new String[]{"DOOR_CLOSE", "BLOCK_WOODEN_DOOR_CLOSE"}),
    DOOR_OPEN(new String[]{"DOOR_OPEN", "BLOCK_WOODEN_DOOR_OPEN"}),
    DRINK(new String[]{"DRINK", "ENTITY_GENERIC_DRINK"}),
    EAT(new String[]{"EAT", "ENTITY_GENERIC_EAT"}),
    EXPLODE(new String[]{"EXPLODE", "ENTITY_GENERIC_EXPLODE"}),
    FALL_BIG(new String[]{"FALL_BIG", "ENTITY_GENERIC_BIG_FALL"}),
    FALL_SMALL(new String[]{"FALL_SMALL", "ENTITY_GENERIC_SMALL_FALL"}),
    FIRE(new String[]{"FIRE", "BLOCK_FIRE_AMBIENT"}),
    FIRE_IGNITE(new String[]{"FIRE_IGNITE", "ITEM_FLINTANDSTEEL_USE"}),
    FIZZ(new String[]{"FIZZ", "BLOCK_FIRE_EXTINGUISH"}),
    FUSE(new String[]{"FUSE", "ENTITY_TNT_PRIMED"}),
    GLASS(new String[]{"GLASS", "BLOCK_GLASS_BREAK"}),
    HURT_FLESH(new String[]{"HURT_FLESH", "ENTITY_PLAYER_HURT"}),
    ITEM_BREAK(new String[]{"ITEM_BREAK", "ENTITY_ITEM_BREAK"}),
    ITEM_PICKUP(new String[]{"ITEM_PICKUP", "ENTITY_ITEM_PICKUP"}),
    LAVA(new String[]{"LAVA", "BLOCK_LAVA_AMBIENT"}),
    LAVA_POP(new String[]{"LAVA_POP", "BLOCK_LAVA_POP"}),
    LEVEL_UP(new String[]{"LEVEL_UP", "ENTITY_PLAYER_LEVELUP"}),
    MINECART_BASE(new String[]{"MINECART_BASE", "ENTITY_MINECART_RIDING"}),
    MINECART_INSIDE(new String[]{"MINECART_INSIDE", "ENTITY_MINECART_RIDING"}),
    NOTE_BASS(new String[]{"NOTE_BASS", "BLOCK_NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS"}),
    NOTE_PIANO(new String[]{"NOTE_PIANO", "BLOCK_NOTE_HARP", "BLOCK_NOTE_BLOCK_HARP"}),
    NOTE_BASS_DRUM(new String[]{"NOTE_BASS_DRUM", "BLOCK_NOTE_BASEDRUM", "BLOCK_NOTE_BLOCK_BASEDRUM"}),
    NOTE_STICKS(new String[]{"NOTE_STICKS", "BLOCK_NOTE_HAT", "BLOCK_NOTE_BLOCK_HAT"}),
    NOTE_BASS_GUITAR(new String[]{"NOTE_BASS_GUITAR", "BLOCK_NOTE_GUITAR", "BLOCK_NOTE_BLOCK_GUITAR", "BLOCK_NOTE_BASS"}),
    NOTE_SNARE_DRUM(new String[]{"NOTE_SNARE_DRUM", "BLOCK_NOTE_SNARE", "BLOCK_NOTE_BLOCK_SNARE"}),
    NOTE_PLING(new String[]{"NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING"}),
    ORB_PICKUP(new String[]{"ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP"}),
    PISTON_EXTEND(new String[]{"PISTON_EXTEND", "BLOCK_PISTON_EXTEND"}),
    PISTON_RETRACT(new String[]{"PISTON_RETRACT", "BLOCK_PISTON_CONTRACT"}),
    PORTAL(new String[]{"PORTAL", "BLOCK_PORTAL_AMBIENT"}),
    PORTAL_TRAVEL(new String[]{"PORTAL_TRAVEL", "BLOCK_PORTAL_TRAVEL"}),
    PORTAL_TRIGGER(new String[]{"PORTAL_TRIGGER", "BLOCK_PORTAL_TRIGGER"}),
    SHOOT_ARROW(new String[]{"SHOOT_ARROW", "ENTITY_ARROW_SHOOT"}),
    SPLASH(new String[]{"SPLASH", "ENTITY_GENERIC_SPLASH"}),
    SPLASH2(new String[]{"SPLASH2", "ENTITY_BOBBER_SPLASH", "ENTITY_FISHING_BOBBER_SPLASH"}),
    STEP_GRASS(new String[]{"STEP_GRASS", "BLOCK_GRASS_STEP"}),
    STEP_GRAVEL(new String[]{"STEP_GRAVEL", "BLOCK_GRAVEL_STEP"}),
    STEP_LADDER(new String[]{"STEP_LADDER", "BLOCK_LADDER_STEP"}),
    STEP_SAND(new String[]{"STEP_SAND", "BLOCK_SAND_STEP"}),
    STEP_SNOW(new String[]{"STEP_SNOW", "BLOCK_SNOW_STEP"}),
    STEP_STONE(new String[]{"STEP_STONE", "BLOCK_STONE_STEP"}),
    STEP_WOOD(new String[]{"STEP_WOOD", "BLOCK_WOOD_STEP"}),
    STEP_WOOL(new String[]{"STEP_WOOL", "BLOCK_CLOTH_STEP", "BLOCK_WOOL_STEP"}),
    SWIM(new String[]{"SWIM", "ENTITY_GENERIC_SWIM"}),
    WATER(new String[]{"WATER", "BLOCK_WATER_AMBIENT"}),
    WOOD_CLICK(new String[]{"WOOD_CLICK", "BLOCK_WOOD_BUTTON_CLICK_ON", "BLOCK_WOODEN_BUTTON_CLICK_ON"}),
    BAT_DEATH(new String[]{"BAT_DEATH", "ENTITY_BAT_DEATH"}),
    BAT_HURT(new String[]{"BAT_HURT", "ENTITY_BAT_HURT"}),
    BAT_IDLE(new String[]{"BAT_IDLE", "ENTITY_BAT_AMBIENT"}),
    BAT_LOOP(new String[]{"BAT_LOOP", "ENTITY_BAT_LOOP"}),
    BAT_TAKEOFF(new String[]{"BAT_TAKEOFF", "ENTITY_BAT_TAKEOFF"}),
    BLAZE_BREATH(new String[]{"BLAZE_BREATH", "ENTITY_BLAZE_AMBIENT"}),
    BLAZE_DEATH(new String[]{"BLAZE_DEATH", "ENTITY_BLAZE_DEATH"}),
    BLAZE_HIT(new String[]{"BLAZE_HIT", "ENTITY_BLAZE_HURT"}),
    CAT_HISS(new String[]{"CAT_HISS", "ENTITY_CAT_HISS"}),
    CAT_HIT(new String[]{"CAT_HIT", "ENTITY_CAT_HURT"}),
    CAT_MEOW(new String[]{"CAT_MEOW", "ENTITY_CAT_AMBIENT"}),
    CAT_PURR(new String[]{"CAT_PURR", "ENTITY_CAT_PURR"}),
    CAT_PURREOW(new String[]{"CAT_PURREOW", "ENTITY_CAT_PURREOW"}),
    CHICKEN_IDLE(new String[]{"CHICKEN_IDLE", "ENTITY_CHICKEN_AMBIENT"}),
    CHICKEN_HURT(new String[]{"CHICKEN_HURT", "ENTITY_CHICKEN_HURT"}),
    CHICKEN_EGG_POP(new String[]{"CHICKEN_EGG_POP", "ENTITY_CHICKEN_EGG"}),
    CHICKEN_WALK(new String[]{"CHICKEN_WALK", "ENTITY_CHICKEN_STEP"}),
    COW_IDLE(new String[]{"COW_IDLE", "ENTITY_COW_AMBIENT"}),
    COW_HURT(new String[]{"COW_HURT", "ENTITY_COW_HURT"}),
    COW_WALK(new String[]{"COW_WALK", "ENTITY_COW_STEP"}),
    CREEPER_HISS(new String[]{"CREEPER_HISS", "ENTITY_CREEPER_PRIMED"}),
    CREEPER_DEATH(new String[]{"CREEPER_DEATH", "ENTITY_CREEPER_DEATH"}),
    ENDERDRAGON_DEATH(new String[]{"ENDERDRAGON_DEATH", "ENTITY_ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH"}),
    ENDERDRAGON_GROWL(new String[]{"ENDERDRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_GROWL"}),
    ENDERDRAGON_HIT(new String[]{"ENDERDRAGON_HIT", "ENTITY_ENDERDRAGON_HURT", "ENTITY_ENDER_DRAGON_HURT"}),
    ENDERDRAGON_WINGS(new String[]{"ENDERDRAGON_WINGS", "ENTITY_ENDERDRAGON_FLAP", "ENTITY_ENDER_DRAGON_FLAP"}),
    ENDERMAN_DEATH(new String[]{"ENDERMAN_DEATH", "ENTITY_ENDERMEN_DEATH", "ENTITY_ENDERMAN_DEATH"}),
    ENDERMAN_HIT(new String[]{"ENDERMAN_HIT", "ENTITY_ENDERMEN_HURT", "ENTITY_ENDERMAN_HURT"}),
    ENDERMAN_IDLE(new String[]{"ENDERMAN_IDLE", "ENTITY_ENDERMEN_AMBIENT", "ENTITY_ENDERMAN_AMBIENT"}),
    ENDERMAN_TELEPORT(new String[]{"ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT"}),
    ENDERMAN_SCREAM(new String[]{"ENDERMAN_SCREAM", "ENTITY_ENDERMEN_SCREAM", "ENTITY_ENDERMAN_SCREAM"}),
    ENDERMAN_STARE(new String[]{"ENDERMAN_STARE", "ENTITY_ENDERMEN_STARE", "ENTITY_ENDERMAN_STARE"}),
    GHAST_SCREAM(new String[]{"GHAST_SCREAM", "ENTITY_GHAST_SCREAM"}),
    GHAST_SCREAM2(new String[]{"GHAST_SCREAM2", "ENTITY_GHAST_HURT"}),
    GHAST_CHARGE(new String[]{"GHAST_CHARGE", "ENTITY_GHAST_WARN"}),
    GHAST_DEATH(new String[]{"GHAST_DEATH", "ENTITY_GHAST_DEATH"}),
    GHAST_FIREBALL(new String[]{"GHAST_FIREBALL", "ENTITY_GHAST_SHOOT"}),
    GHAST_MOAN(new String[]{"GHAST_MOAN", "ENTITY_GHAST_AMBIENT"}),
    IRONGOLEM_ATTACK(new String[]{"IRONGOLEM_THROW", "ENTITY_IRONGOLEM_ATTACK", "ENTITY_IRON_GOLEM_ATTACK"}),
    IRONGOLEM_DEATH(new String[]{"IRONGOLEM_DEATH", "ENTITY_IRONGOLEM_DEATH", "ENTITY_IRON_GOLEM_DEATH"}),
    IRONGOLEM_HIT(new String[]{"IRONGOLEM_HIT", "ENTITY_IRONGOLEM_HURT", "ENTITY_IRON_GOLEM_HURT"}),
    IRONGOLEM_WALK(new String[]{"IRONGOLEM_WALK", "ENTITY_IRONGOLEM_STEP", "ENTITY_IRON_GOLEM_STEP"}),
    MAGMACUBE_WALK(new String[]{"MAGMACUBE_WALK", "ENTITY_MAGMACUBE_SQUISH", "ENTITY_MAGMA_CUBE_SQUISH"}),
    MAGMACUBE_WALK2(new String[]{"MAGMACUBE_WALK2", "ENTITY_MAGMACUBE_SQUISH", "ENTITY_MAGMA_CUBE_SQUISH_SMALL"}),
    MAGMACUBE_JUMP(new String[]{"MAGMACUBE_JUMP", "ENTITY_MAGMACUBE_JUMP", "ENTITY_MAGMA_CUBE_JUMP"}),
    PIG_IDLE(new String[]{"PIG_IDLE", "ENTITY_PIG_AMBIENT"}),
    PIG_DEATH(new String[]{"PIG_DEATH", "ENTITY_PIG_DEATH"}),
    PIG_WALK(new String[]{"PIG_WALK", "ENTITY_PIG_STEP"}),
    SHEEP_IDLE(new String[]{"SHEEP_IDLE", "ENTITY_SHEEP_AMBIENT"}),
    SHEEP_SHEAR(new String[]{"SHEEP_SHEAR", "ENTITY_SHEEP_SHEAR"}),
    SHEEP_WALK(new String[]{"SHEEP_WALK", "ENTITY_SHEEP_STEP"}),
    SILVERFISH_HIT(new String[]{"SILVERFISH_HIT", "ENTITY_SILVERFISH_HURT"}),
    SILVERFISH_KILL(new String[]{"SILVERFISH_KILL", "ENTITY_SILVERFISH_DEATH"}),
    SILVERFISH_IDLE(new String[]{"SILVERFISH_IDLE", "ENTITY_SILVERFISH_AMBIENT"}),
    SILVERFISH_WALK(new String[]{"SILVERFISH_WALK", "ENTITY_SILVERFISH_STEP"}),
    SKELETON_IDLE(new String[]{"SKELETON_IDLE", "ENTITY_SKELETON_AMBIENT"}),
    SKELETON_DEATH(new String[]{"SKELETON_DEATH", "ENTITY_SKELETON_DEATH"}),
    SKELETON_HURT(new String[]{"SKELETON_HURT", "ENTITY_SKELETON_HURT"}),
    SKELETON_WALK(new String[]{"SKELETON_WALK", "ENTITY_SKELETON_STEP"}),
    SLIME_ATTACK(new String[]{"SLIME_ATTACK", "ENTITY_SLIME_ATTACK"}),
    SLIME_WALK(new String[]{"SLIME_WALK", "ENTITY_SLIME_JUMP"}),
    SLIME_WALK2(new String[]{"SLIME_WALK2", "ENTITY_SLIME_SQUISH"}),
    SPIDER_IDLE(new String[]{"SPIDER_IDLE", "ENTITY_SPIDER_AMBIENT"}),
    SPIDER_DEATH(new String[]{"SPIDER_DEATH", "ENTITY_SPIDER_DEATH"}),
    SPIDER_WALK(new String[]{"SPIDER_WALK", "ENTITY_SPIDER_STEP"}),
    WITHER_DEATH(new String[]{"WITHER_DEATH", "ENTITY_WITHER_DEATH"}),
    WITHER_HURT(new String[]{"WITHER_HURT", "ENTITY_WITHER_HURT"}),
    WITHER_IDLE(new String[]{"WITHER_IDLE", "ENTITY_WITHER_AMBIENT"}),
    WITHER_SHOOT(new String[]{"WITHER_SHOOT", "ENTITY_WITHER_SHOOT"}),
    WITHER_SPAWN(new String[]{"WITHER_SPAWN", "ENTITY_WITHER_SPAWN"}),
    WOLF_BARK(new String[]{"WOLF_BARK", "ENTITY_WOLF_AMBIENT"}),
    WOLF_DEATH(new String[]{"WOLF_DEATH", "ENTITY_WOLF_DEATH"}),
    WOLF_GROWL(new String[]{"WOLF_GROWL", "ENTITY_WOLF_GROWL"}),
    WOLF_HOWL(new String[]{"WOLF_HOWL", "ENTITY_WOLF_HOWL"}),
    WOLF_HURT(new String[]{"WOLF_HURT", "ENTITY_WOLF_HURT"}),
    WOLF_PANT(new String[]{"WOLF_PANT", "ENTITY_WOLF_PANT"}),
    WOLF_SHAKE(new String[]{"WOLF_SHAKE", "ENTITY_WOLF_SHAKE"}),
    WOLF_WALK(new String[]{"WOLF_WALK", "ENTITY_WOLF_STEP"}),
    WOLF_WHINE(new String[]{"WOLF_WHINE", "ENTITY_WOLF_WHINE"}),
    ZOMBIE_METAL(new String[]{"ZOMBIE_METAL", "ENTITY_ZOMBIE_ATTACK_IRON_DOOR"}),
    ZOMBIE_WOOD(new String[]{"ZOMBIE_WOOD", "ENTITY_ZOMBIE_ATTACK_DOOR_WOOD", "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR"}),
    ZOMBIE_WOODBREAK(new String[]{"ZOMBIE_WOODBREAK", "ENTITY_ZOMBIE_BREAK_DOOR_WOOD", "ENTITY_ZOMBIE_BREAK_WOODEN_DOOR"}),
    ZOMBIE_IDLE(new String[]{"ZOMBIE_IDLE", "ENTITY_ZOMBIE_AMBIENT"}),
    ZOMBIE_DEATH(new String[]{"ZOMBIE_DEATH", "ENTITY_ZOMBIE_DEATH"}),
    ZOMBIE_HURT(new String[]{"ZOMBIE_HURT", "ENTITY_ZOMBIE_HURT"}),
    ZOMBIE_INFECT(new String[]{"ZOMBIE_INFECT", "ENTITY_ZOMBIE_INFECT"}),
    ZOMBIE_UNFECT(new String[]{"ZOMBIE_UNFECT", "ENTITY_ZOMBIE_VILLAGER_CONVERTED"}),
    ZOMBIE_REMEDY(new String[]{"ZOMBIE_REMEDY", "ENTITY_ZOMBIE_VILLAGER_CURE"}),
    ZOMBIE_WALK(new String[]{"ZOMBIE_WALK", "ENTITY_ZOMBIE_STEP"}),
    ZOMBIE_PIG_IDLE(new String[]{"ZOMBIE_PIG_IDLE", "ENTITY_ZOMBIE_PIG_AMBIENT", "ENTITY_ZOMBIE_PIGMAN_AMBIENT"}),
    ZOMBIE_PIG_ANGRY(new String[]{"ZOMBIE_PIG_ANGRY", "ENTITY_ZOMBIE_PIG_ANGRY", "ENTITY_ZOMBIE_PIGMAN_ANGRY"}),
    ZOMBIE_PIG_DEATH(new String[]{"ZOMBIE_PIG_DEATH", "ENTITY_ZOMBIE_PIG_DEATH", "ENTITY_ZOMBIE_PIGMAN_DEATH"}),
    ZOMBIE_PIG_HURT(new String[]{"ZOMBIE_PIG_HURT", "ENTITY_ZOMBIE_PIG_HURT", "ENTITY_ZOMBIE_PIGMAN_HURT"}),
    DIG_WOOL(new String[]{"DIG_WOOL", "BLOCK_CLOTH_BREAK", "BLOCK_WOOL_BREAK"}),
    DIG_GRASS(new String[]{"DIG_GRASS", "BLOCK_GRASS_BREAK"}),
    DIG_GRAVEL(new String[]{"DIG_GRAVEL", "BLOCK_GRAVEL_BREAK"}),
    DIG_SAND(new String[]{"DIG_SAND", "BLOCK_SAND_BREAK"}),
    DIG_SNOW(new String[]{"DIG_SNOW", "BLOCK_SNOW_BREAK"}),
    DIG_STONE(new String[]{"DIG_STONE", "BLOCK_STONE_BREAK"}),
    DIG_WOOD(new String[]{"DIG_WOOD", "BLOCK_WOOD_BREAK"}),
    FIREWORK_BLAST(new String[]{"FIREWORK_BLAST", "ENTITY_FIREWORK_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST"}),
    FIREWORK_BLAST2(new String[]{"FIREWORK_BLAST2", "ENTITY_FIREWORK_BLAST_FAR", "ENTITY_FIREWORK_ROCKET_BLAST_FAR"}),
    FIREWORK_LARGE_BLAST(new String[]{"FIREWORK_LARGE_BLAST", "ENTITY_FIREWORK_LARGE_BLAST", "ENTITY_FIREWORK_ROCKET_LARGE_BLAST"}),
    FIREWORK_LARGE_BLAST2(new String[]{"FIREWORK_LARGE_BLAST2", "ENTITY_FIREWORK_LARGE_BLAST_FAR", "ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR"}),
    FIREWORK_LAUNCH(new String[]{"FIREWORK_LAUNCH", "ENTITY_FIREWORK_LAUNCH", "ENTITY_FIREWORK_ROCKET_LAUNCH"}),
    FIREWORK_TWINKLE(new String[]{"FIREWORK_TWINKLE", "ENTITY_FIREWORK_TWINKLE", "ENTITY_FIREWORK_ROCKET_TWINKLE"}),
    FIREWORK_TWINKLE2(new String[]{"FIREWORK_TWINKLE2", "ENTITY_FIREWORK_TWINKLE_FAR", "ENTITY_FIREWORK_ROCKET_TWINKLE_FAR"}),
    SUCCESSFUL_HIT(new String[]{"SUCCESSFUL_HIT", "ENTITY_ARROW_HIT_PLAYER"}),
    HORSE_ANGRY(new String[]{"HORSE_ANGRY", "ENTITY_HORSE_ANGRY"}),
    HORSE_ARMOR(new String[]{"HORSE_ARMOR", "ENTITY_HORSE_ARMOR"}),
    HORSE_BREATHE(new String[]{"HORSE_BREATHE", "ENTITY_HORSE_BREATHE"}),
    HORSE_DEATH(new String[]{"HORSE_DEATH", "ENTITY_HORSE_DEATH"}),
    HORSE_GALLOP(new String[]{"HORSE_GALLOP", "ENTITY_HORSE_GALLOP"}),
    HORSE_HIT(new String[]{"HORSE_HIT", "ENTITY_HORSE_HURT"}),
    HORSE_IDLE(new String[]{"HORSE_IDLE", "ENTITY_HORSE_AMBIENT"}),
    HORSE_JUMP(new String[]{"HORSE_JUMP", "ENTITY_HORSE_JUMP"}),
    HORSE_LAND(new String[]{"HORSE_LAND", "ENTITY_HORSE_LAND"}),
    HORSE_SADDLE(new String[]{"HORSE_SADDLE", "ENTITY_HORSE_SADDLE"}),
    HORSE_SOFT(new String[]{"HORSE_SOFT", "ENTITY_HORSE_STEP"}),
    HORSE_WOOD(new String[]{"HORSE_WOOD", "ENTITY_HORSE_STEP_WOOD"}),
    DONKEY_ANGRY(new String[]{"DONKEY_ANGRY", "ENTITY_DONKEY_ANGRY"}),
    DONKEY_DEATH(new String[]{"DONKEY_DEATH", "ENTITY_DONKEY_DEATH"}),
    DONKEY_HIT(new String[]{"DONKEY_HIT", "ENTITY_DONKEY_HURT"}),
    DONKEY_IDLE(new String[]{"DONKEY_IDLE", "ENTITY_DONKEY_AMBIENT"}),
    HORSE_SKELETON_DEATH(new String[]{"HORSE_SKELETON_DEATH", "ENTITY_SKELETON_HORSE_DEATH"}),
    HORSE_SKELETON_HIT(new String[]{"HORSE_SKELETON_HIT", "ENTITY_SKELETON_HORSE_HURT"}),
    HORSE_SKELETON_IDLE(new String[]{"HORSE_SKELETON_IDLE", "ENTITY_SKELETON_HORSE_AMBIENT"}),
    HORSE_ZOMBIE_DEATH(new String[]{"HORSE_ZOMBIE_DEATH", "ENTITY_ZOMBIE_HORSE_DEATH"}),
    HORSE_ZOMBIE_HIT(new String[]{"HORSE_ZOMBIE_HIT", "ENTITY_ZOMBIE_HORSE_HURT"}),
    HORSE_ZOMBIE_IDLE(new String[]{"HORSE_ZOMBIE_IDLE", "ENTITY_ZOMBIE_HORSE_AMBIENT"}),
    VILLAGER_DEATH(new String[]{"VILLAGER_DEATH", "ENTITY_VILLAGER_DEATH"}),
    VILLAGER_TRADE(new String[]{"VILLAGER_HAGGLE", "ENTITY_VILLAGER_TRADING", "ENTITY_VILLAGER_TRADE"}),
    VILLAGER_HIT(new String[]{"VILLAGER_HIT", "ENTITY_VILLAGER_HURT"}),
    VILLAGER_IDLE(new String[]{"VILLAGER_IDLE", "ENTITY_VILLAGER_AMBIENT"}),
    VILLAGER_NO(new String[]{"VILLAGER_NO", "ENTITY_VILLAGER_NO"}),
    VILLAGER_YES(new String[]{"VILLAGER_YES", "ENTITY_VILLAGER_YES"}),
    BLOCK_DISPENSER_LAUNCH(new String[]{"LEVEL_UP", "DISPENSER_LAUNCH", "BLOCK_DISPENSER_LAUNCH"}),
    ENTITY_ITEMFRAME_BREAK(new String[]{"STEP_WOOL", "BLOCK_CLOTH_STEP", "BLOCK_WOOL_STEP", "ENTITY_ITEMFRAME_BREAK"});

    private String[] versionDependentNames;
    private Sound cached = null;

    private CompSound(String... versionDependentNames) {
        this.versionDependentNames = versionDependentNames;
        ArrayUtils.reverse(this.versionDependentNames);
    }

    public final void play(Player player) {
        this.play(player, 1.0F, 1.0F);
    }

    public final void play(Player player, float volume, float pitch) {
        try {
            player.playSound(player.getLocation(), this.getSound(), volume, pitch);
        } catch (Throwable var5) {
        }

    }

    public final void play(Location loc) {
        this.play(loc, 1.0F, 1.0F);
    }

    public final void play(Location loc, float volume, float pitch) {
        try {
            loc.getWorld().playSound(loc, this.getSound(), volume, pitch);
        } catch (Throwable var5) {
        }

    }

    public final Sound getSound() {
        if (this.cached != null) {
            return this.cached;
        } else {
            String[] var1 = this.versionDependentNames;
            int var2 = var1.length;
            int var3 = 0;

            while(var3 < var2) {
                String name = var1[var3];

                try {
                    return this.cached = Sound.valueOf(name);
                } catch (IllegalArgumentException var6) {
                    ++var3;
                }
            }

            return getFallback();
        }
    }

    public static final Sound getFallback() {
        return Sound.valueOf(MinecraftVersion.atLeast(V.v1_9) ? "ENTITY_PLAYER_LEVELUP" : "LEVEL_UP");
    }

    public static final Sound convert(String soundName) {
        CompSound sound = null;
        if (MinecraftVersion.atLeast(V.v1_9)) {
            try {
                return Sound.valueOf(soundName.toUpperCase());
            } catch (IllegalArgumentException var10) {
            }
        }

        CompSound[] var2 = values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            CompSound compSound = var2[var4];
            String[] var6 = compSound.versionDependentNames;
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                String name = var6[var8];
                if (name.equalsIgnoreCase(soundName)) {
                    sound = compSound;
                }
            }
        }

        return sound != null ? sound.getSound() : getFallback();
    }
}