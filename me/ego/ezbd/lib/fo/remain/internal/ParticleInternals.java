package me.ego.ezbd.lib.fo.remain.internal;

import java.util.Iterator;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/** @deprecated */
@Deprecated
public enum ParticleInternals {
    HUGE_EXPLOSION("hugeexplosion", "EXPLOSION_HUGE"),
    LARGE_EXPLODE("largeexplode", "EXPLOSION_LARGE"),
    BUBBLE("bubble", "WATER_BUBBLE"),
    SUSPEND("suspended", "SUSPENDED"),
    DEPTH_SUSPEND("depthsuspend", "SUSPENDED_DEPTH"),
    MAGIC_CRIT("magicCrit", "CRIT_MAGIC"),
    MOB_SPELL("mobSpell", "SPELL_MOB", true),
    MOB_SPELL_AMBIENT("mobSpellAmbient", "SPELL_MOB_AMBIENT"),
    INSTANT_SPELL("instantSpell", "SPELL_INSTANT"),
    WITCH_MAGIC("witchMagic", "SPELL_WITCH"),
    EXPLODE("explode", "EXPLOSION_NORMAL"),
    SPLASH("splash", "WATER_SPLASH"),
    LARGE_SMOKE("largesmoke", "SMOKE_LARGE"),
    RED_DUST("reddust", "REDSTONE", true),
    SNOWBALL_POOF("snowballpoof", "SNOWBALL"),
    ANGRY_VILLAGER("angryVillager", "VILLAGER_ANGRY"),
    HAPPY_VILLAGER("happyVillager", "VILLAGER_HAPPY"),
    EXPLOSION_NORMAL(EXPLODE.name),
    EXPLOSION_LARGE(LARGE_EXPLODE.name),
    EXPLOSION_HUGE(HUGE_EXPLOSION.name),
    FIREWORKS_SPARK("fireworksSpark"),
    WATER_BUBBLE(BUBBLE.name),
    WATER_SPLASH(SPLASH.name),
    WATER_WAKE("wake"),
    SUSPENDED(SUSPEND.name),
    SUSPENDED_DEPTH(DEPTH_SUSPEND.name),
    CRIT("crit"),
    CRIT_MAGIC(MAGIC_CRIT.name),
    SMOKE_NORMAL("smoke"),
    SMOKE_LARGE(LARGE_SMOKE.name),
    SPELL("spell"),
    SPELL_INSTANT(INSTANT_SPELL.name),
    SPELL_MOB(MOB_SPELL.name, true),
    SPELL_MOB_AMBIENT(MOB_SPELL_AMBIENT.name),
    SPELL_WITCH(WITCH_MAGIC.name),
    DRIP_WATER("dripWater"),
    DRIP_LAVA("dripLava"),
    VILLAGER_ANGRY(ANGRY_VILLAGER.name),
    VILLAGER_HAPPY(HAPPY_VILLAGER.name),
    TOWN_AURA("townaura"),
    NOTE("note", true),
    PORTAL("portal"),
    ENCHANTMENT_TABLE("enchantmenttable"),
    FLAME("flame"),
    LAVA("lava"),
    FOOTSTEP("footstep"),
    CLOUD("cloud"),
    REDSTONE("reddust", true),
    SNOWBALL("snowballpoof"),
    SNOW_SHOVEL("snowshovel"),
    SLIME("slime"),
    HEART("heart"),
    BARRIER("barrier"),
    ITEM_CRACK("iconcrack_"),
    BLOCK_CRACK("blockcrack_"),
    BLOCK_DUST("blockdust_"),
    WATER_DROP("droplet"),
    ITEM_TAKE("take"),
    MOB_APPEARANCE("mobappearance");

    private static final Class<?> nmsPacketPlayOutParticle = MinecraftVersion.atLeast(V.v1_7) ? ReflectionUtil.getNMSClass("PacketPlayOutWorldParticles") : null;
    private static Class<?> nmsEnumParticle;
    private String name;
    private String enumValue;
    private boolean hasColor;

    private ParticleInternals(String particleName, String enumValue, boolean hasColor) {
        this.name = particleName;
        this.enumValue = enumValue;
        this.hasColor = hasColor;
    }

    private ParticleInternals(String particleName, String enumValue) {
        this(particleName, enumValue, false);
    }

    private ParticleInternals(String particleName) {
        this(particleName, (String)null);
    }

    private ParticleInternals(String particleName, boolean hasColor) {
        this(particleName, (String)null, hasColor);
    }

    public void send(Location loc, float speed) {
        Iterator var3 = loc.getWorld().getPlayers().iterator();

        while(var3.hasNext()) {
            Player player = (Player)var3.next();
            this.send(player, loc, 0.0F, 0.0F, 0.0F, speed, 1);
        }

    }

    public void send(Player player, Location location, float speed) {
        this.send(player, location, 0.0F, 0.0F, 0.0F, speed, 1);
    }

    public void send(Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count, int... extra) {
        if (!MinecraftVersion.olderThan(V.v1_7)) {
            Object packet;
            int data;
            if (MinecraftVersion.equals(V.v1_8)) {
                if (nmsEnumParticle == null) {
                    nmsEnumParticle = ReflectionUtil.getNMSClass("EnumParticle");
                }

                if (this == BLOCK_CRACK) {
                    int id = 0;
                    data = 0;
                    if (extra.length > 0) {
                        id = extra[0];
                    }

                    if (extra.length > 1) {
                        data = extra[1];
                    }

                    extra = new int[]{id, id | data << 12};
                }

                try {
                    packet = nmsPacketPlayOutParticle.getConstructor(nmsEnumParticle, Boolean.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Integer.TYPE, int[].class).newInstance(getEnum(nmsEnumParticle.getName() + "." + (this.enumValue != null ? this.enumValue : this.name().toUpperCase())), true, (float)location.getX(), (float)location.getY(), (float)location.getZ(), offsetX, offsetY, offsetZ, speed, count, extra);
                } catch (ReflectiveOperationException var14) {
                    return;
                }
            } else {
                if (this.name == null) {
                    this.name = this.name().toLowerCase();
                }

                String name = this.name;
                if (this == BLOCK_CRACK || this == ITEM_CRACK || this == BLOCK_DUST) {
                    data = 0;
                    int data2 = 0;
                    if (extra.length > 0) {
                        data = extra[0];
                    }

                    if (extra.length > 1) {
                        data2 = extra[1];
                    }

                    name = name + data + "_" + data2;
                }

                try {
                    packet = nmsPacketPlayOutParticle.getConstructor(String.class, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Integer.TYPE).newInstance(name, (float)location.getX(), (float)location.getY(), (float)location.getZ(), offsetX, offsetY, offsetZ, speed, count);
                } catch (ReflectiveOperationException var13) {
                    return;
                }
            }

            Remain.sendPacket(player, packet);
        }
    }

    private static Enum<?> getEnum(String enumFullName) {
        String[] x = enumFullName.split("\\.(?=[^\\.]+$)");
        if (x.length == 2) {
            String enumClassName = x[0];
            String enumName = x[1];

            try {
                Class<Enum> cl = Class.forName(enumClassName);
                return Enum.valueOf(cl, enumName);
            } catch (ClassNotFoundException var5) {
                var5.printStackTrace();
            }
        }

        return null;
    }

    public void sendColor(Location loc, Color color) {
        Iterator var3 = loc.getWorld().getPlayers().iterator();

        while(var3.hasNext()) {
            Player player = (Player)var3.next();
            this.sendColor(player, loc, color);
        }

    }

    public void sendColor(Player player, Location location, Color color) {
        if (this.hasColor) {
            this.send(player, location, this.getColor((float)color.getRed()), this.getColor((float)color.getGreen()), this.getColor((float)color.getBlue()), 1.0F, 0);
        }
    }

    private float getColor(float value) {
        if (value <= 0.0F) {
            value = -1.0F;
        }

        return value / 255.0F;
    }
}