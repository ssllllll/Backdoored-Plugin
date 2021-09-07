package me.ego.ezbd.lib.fo.remain;

import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.remain.internal.ParticleInternals;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

public enum CompParticle {
    EXPLOSION_NORMAL,
    EXPLOSION_LARGE,
    EXPLOSION_HUGE,
    FIREWORKS_SPARK,
    WATER_BUBBLE,
    WATER_SPLASH,
    WATER_WAKE,
    SUSPENDED,
    SUSPENDED_DEPTH,
    CRIT,
    CRIT_MAGIC,
    SMOKE_NORMAL,
    SMOKE_LARGE,
    SPELL,
    SPELL_INSTANT,
    SPELL_MOB,
    SPELL_MOB_AMBIENT,
    SPELL_WITCH,
    DRIP_WATER,
    DRIP_LAVA,
    VILLAGER_ANGRY,
    VILLAGER_HAPPY,
    TOWN_AURA,
    NOTE,
    PORTAL,
    ENCHANTMENT_TABLE,
    FLAME,
    LAVA,
    FOOTSTEP,
    CLOUD,
    REDSTONE,
    SNOWBALL,
    SNOW_SHOVEL,
    SLIME,
    HEART,
    BARRIER,
    ITEM_CRACK,
    BLOCK_CRACK,
    BLOCK_DUST,
    WATER_DROP,
    ITEM_TAKE,
    MOB_APPEARANCE,
    DRAGON_BREATH,
    END_ROD,
    DAMAGE_INDICATOR,
    SWEEP_ATTACK,
    FALLING_DUST,
    TOTEM,
    SPIT;

    private static final boolean hasNewMaterials = MinecraftVersion.atLeast(V.v1_13);
    /** @deprecated */
    @Deprecated
    private MaterialData data;

    private CompParticle() {
    }

    /** @deprecated */
    @Deprecated
    public CompParticle setWoolData(int data) {
        this.data = new MaterialData(CompMaterial.WHITE_WOOL.getMaterial(), (byte)data);
        return this;
    }

    /** @deprecated */
    @Deprecated
    public CompParticle setData(Material material, int data) {
        this.data = new MaterialData(material, (byte)data);
        return this;
    }

    public final void spawn(Location location) {
        this.spawn(location, (Double)null);
    }

    public final void spawn(Location location, Double extra) {
        if (Remain.hasParticleAPI()) {
            Particle particle = (Particle)ReflectionUtil.lookupEnumSilent(Particle.class, this.toString());
            if (particle != null) {
                if (MinecraftVersion.atLeast(V.v1_13) && particle.getDataType() == BlockData.class) {
                    BlockData opt = Material.END_ROD.createBlockData();
                    if (this.data != null) {
                        opt = Bukkit.getUnsafe().fromLegacy(this.data.getItemType(), this.data.getData());
                    }

                    location.getWorld().spawnParticle(particle, location, 1, 0.0D, 0.0D, 0.0D, extra != null ? extra : 0.0D, opt);
                    return;
                }

                location.getWorld().spawnParticle(particle, location, 1, 0.0D, 0.0D, 0.0D, extra != null ? extra : 0.0D);
            }
        } else {
            ParticleInternals particle = (ParticleInternals)ReflectionUtil.lookupEnumSilent(ParticleInternals.class, this.toString());
            if (particle != null) {
                particle.send(location, extra != null ? extra.floatValue() : 0.0F);
            }
        }

    }

    public final void spawnWithData(Location location, CompMaterial data) {
        if (Remain.hasParticleAPI()) {
            Particle particle = (Particle)ReflectionUtil.lookupEnumSilent(Particle.class, this.toString());
            if (particle != null) {
                if (hasNewMaterials) {
                    location.getWorld().spawnParticle(particle, location, 1, data.getMaterial().createBlockData());
                } else {
                    location.getWorld().spawnParticle(particle, location, 1, data.getMaterial().getNewData((byte)data.getData()));
                }
            }
        } else {
            ParticleInternals particle = (ParticleInternals)ReflectionUtil.lookupEnumSilent(ParticleInternals.class, this.toString());
            if (particle != null) {
                particle.sendColor(location, DyeColor.getByWoolData((byte)data.getData()).getColor());
            }
        }

    }

    public final void spawnFor(Player player, Location location) {
        this.spawnFor(player, location, (Double)null);
    }

    public final void spawnFor(Player player, Location location, Double extra) {
        if (Remain.hasParticleAPI()) {
            Particle particle = (Particle)ReflectionUtil.lookupEnumSilent(Particle.class, this.toString());
            if (particle != null) {
                player.spawnParticle(particle, location, 1, 0.0D, 0.0D, 0.0D, extra != null ? extra : 0.0D);
            }
        } else {
            ParticleInternals p = (ParticleInternals)ReflectionUtil.lookupEnumSilent(ParticleInternals.class, this.toString());
            if (p != null) {
                p.send(player, location, extra != null ? extra.floatValue() : 0.0F);
            }
        }

    }

    /** @deprecated */
    @Deprecated
    public MaterialData getData() {
        return this.data;
    }
}