package me.ego.ezbd.lib.fo.remain;

import java.lang.reflect.Method;
import me.ego.ezbd.lib.fo.MinecraftVersion;
import me.ego.ezbd.lib.fo.ReflectionUtil;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.exception.FoException;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public enum CompAttribute {
    GENERIC_MAX_HEALTH("generic.maxHealth", "maxHealth"),
    GENERIC_FOLLOW_RANGE("generic.followRange", "FOLLOW_RANGE"),
    GENERIC_KNOCKBACK_RESISTANCE("generic.knockbackResistance", "c"),
    GENERIC_MOVEMENT_SPEED("generic.movementSpeed", "MOVEMENT_SPEED"),
    GENERIC_FLYING_SPEED("generic.flyingSpeed"),
    GENERIC_ATTACK_DAMAGE("generic.attackDamage", "ATTACK_DAMAGE"),
    GENERIC_ATTACK_SPEED("generic.attackSpeed"),
    GENERIC_ARMOR("generic.armor"),
    GENERIC_ARMOR_TOUGHNESS("generic.armorToughness"),
    GENERIC_LUCK("generic.luck"),
    HORSE_JUMP_STRENGTH("horse.jumpStrength"),
    ZOMBIE_SPAWN_REINFORCEMENTS("zombie.spawnReinforcements");

    private final String minecraftName;
    private String genericFieldName;

    private CompAttribute(String name, String genericFieldName) {
        this.minecraftName = name;
        this.genericFieldName = genericFieldName;
    }

    public final boolean hasLegacy() {
        return this.genericFieldName != null;
    }

    public final Double get(LivingEntity entity) {
        try {
            AttributeInstance instance = entity.getAttribute(Attribute.valueOf(this.toString()));
            return instance != null ? instance.getBaseValue() : null;
        } catch (NoSuchMethodError | NoClassDefFoundError | IllegalArgumentException var6) {
            try {
                return this.hasLegacy() ? this.getLegacy(entity) : null;
            } catch (NullPointerException var4) {
                return null;
            } catch (Throwable var5) {
                if (MinecraftVersion.equals(V.v1_8)) {
                    var5.printStackTrace();
                }

                return null;
            }
        }
    }

    public final void set(LivingEntity entity, double value) {
        Valid.checkNotNull(entity, "Entity cannot be null");
        Valid.checkNotNull(entity, "Attribute cannot be null");

        try {
            AttributeInstance instance = entity.getAttribute(Attribute.valueOf(this.toString()));
            instance.setBaseValue(value);
        } catch (NoSuchMethodError | NoClassDefFoundError | NullPointerException var7) {
            try {
                if (this.hasLegacy()) {
                    this.setLegacy(entity, value);
                }
            } catch (Throwable var6) {
                if (MinecraftVersion.equals(V.v1_8)) {
                    var6.printStackTrace();
                }

                if (var6 instanceof NullPointerException) {
                    throw new FoException("Attribute " + this + " cannot be set for " + entity);
                }
            }
        }

    }

    private double getLegacy(Entity entity) {
        return (Double)ReflectionUtil.invoke("getValue", this.getLegacyAttributeInstance(entity), new Object[0]);
    }

    private void setLegacy(Entity entity, double value) {
        Object instance = this.getLegacyAttributeInstance(entity);
        ReflectionUtil.invoke(ReflectionUtil.getMethod(instance.getClass(), "setValue", new Class[]{Double.TYPE}), instance, new Object[]{value});
    }

    private Object getLegacyAttributeInstance(Entity entity) {
        Object nmsEntity = ReflectionUtil.invoke("getHandle", entity, new Object[0]);
        Class genericAttribute = ReflectionUtil.getNMSClass("GenericAttributes");

        Object iAttribute;
        try {
            iAttribute = ReflectionUtil.getStaticFieldContent(genericAttribute, this.genericFieldName);
        } catch (Throwable var8) {
            iAttribute = ReflectionUtil.getStaticFieldContent(genericAttribute, this.minecraftName);
        }

        Class<?> nmsLiving = ReflectionUtil.getNMSClass("EntityLiving");
        Method method = ReflectionUtil.getMethod(nmsLiving, "getAttributeInstance", new Class[]{ReflectionUtil.getNMSClass("IAttribute")});
        Object ret = ReflectionUtil.invoke(method, nmsEntity, new Object[]{iAttribute});
        return ret;
    }

    private CompAttribute(String minecraftName) {
        this.minecraftName = minecraftName;
    }

    public String getMinecraftName() {
        return this.minecraftName;
    }
}