package me.ego.ezbd.lib.fo;

import java.awt.Color;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.collection.StrictCollection;
import me.ego.ezbd.lib.fo.collection.StrictMap;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.exception.InvalidWorldException;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator;
import me.ego.ezbd.lib.fo.menu.model.ItemCreator.ItemCreatorBuilder;
import me.ego.ezbd.lib.fo.model.ConfigSerializable;
import me.ego.ezbd.lib.fo.model.IsInList;
import me.ego.ezbd.lib.fo.model.SimpleSound;
import me.ego.ezbd.lib.fo.model.SimpleTime;
import me.ego.ezbd.lib.fo.remain.CompChatColor;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.Remain;
import me.ego.ezbd.lib.fo.settings.YamlConfig;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class SerializeUtil {
    public static Object serialize(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof ConfigSerializable) {
            return serialize(((ConfigSerializable)obj).serialize().serialize());
        } else if (obj instanceof StrictCollection) {
            return serialize(((StrictCollection)obj).serialize());
        } else if (obj instanceof ChatColor) {
            return ((ChatColor)obj).name();
        } else if (obj instanceof CompChatColor) {
            return ((CompChatColor)obj).getName();
        } else if (obj instanceof net.md_5.bungee.api.ChatColor) {
            net.md_5.bungee.api.ChatColor color = (net.md_5.bungee.api.ChatColor)obj;
            return MinecraftVersion.atLeast(V.v1_16) ? color.toString() : color.name();
        } else if (obj instanceof CompMaterial) {
            return obj.toString();
        } else if (obj instanceof Location) {
            return serializeLoc((Location)obj);
        } else if (obj instanceof UUID) {
            return obj.toString();
        } else if (obj instanceof Enum) {
            return obj.toString();
        } else if (obj instanceof CommandSender) {
            return ((CommandSender)obj).getName();
        } else if (obj instanceof World) {
            return ((World)obj).getName();
        } else if (obj instanceof PotionEffect) {
            return serializePotionEffect((PotionEffect)obj);
        } else if (obj instanceof ItemCreatorBuilder) {
            return ((ItemCreatorBuilder)obj).build().make();
        } else if (obj instanceof ItemCreator) {
            return ((ItemCreator)obj).make();
        } else if (obj instanceof SimpleTime) {
            return ((SimpleTime)obj).getRaw();
        } else if (obj instanceof SimpleSound) {
            return ((SimpleSound)obj).toString();
        } else if (obj instanceof Color) {
            return "#" + ((Color)obj).getRGB();
        } else if (obj instanceof BaseComponent) {
            return Remain.toJson(new BaseComponent[]{(BaseComponent)obj});
        } else if (obj instanceof BaseComponent[]) {
            return Remain.toJson((BaseComponent[])((BaseComponent[])obj));
        } else if (obj instanceof HoverEvent) {
            HoverEvent event = (HoverEvent)obj;
            return SerializedMap.ofArray(new Object[]{"Action", event.getAction(), "Value", event.getValue()}).serialize();
        } else if (obj instanceof ClickEvent) {
            ClickEvent event = (ClickEvent)obj;
            return SerializedMap.ofArray(new Object[]{"Action", event.getAction(), "Value", event.getValue()}).serialize();
        } else if (!(obj instanceof Iterable) && !obj.getClass().isArray() && !(obj instanceof IsInList)) {
            Iterator var15;
            Entry entry;
            if (obj instanceof StrictMap) {
                StrictMap<Object, Object> oldMap = (StrictMap)obj;
                StrictMap<Object, Object> newMap = new StrictMap();
                var15 = oldMap.entrySet().iterator();

                while(var15.hasNext()) {
                    entry = (Entry)var15.next();
                    newMap.put(serialize(entry.getKey()), serialize(entry.getValue()));
                }

                return newMap;
            } else if (!(obj instanceof Map)) {
                if (obj instanceof YamlConfig) {
                    throw new SerializeUtil.SerializeFailedException("To save your YamlConfig " + obj.getClass().getSimpleName() + " make it implement ConfigSerializable!");
                } else if (!(obj instanceof Integer) && !(obj instanceof Double) && !(obj instanceof Float) && !(obj instanceof Long) && !(obj instanceof Short) && !(obj instanceof String) && !(obj instanceof Boolean) && !(obj instanceof Map) && !(obj instanceof ItemStack) && !(obj instanceof MemorySection) && !(obj instanceof Pattern)) {
                    if (obj instanceof ConfigurationSerializable) {
                        return ((ConfigurationSerializable)obj).serialize();
                    } else {
                        throw new SerializeUtil.SerializeFailedException("Does not know how to serialize " + obj.getClass().getSimpleName() + "! Does it extends ConfigSerializable? Data: " + obj);
                    }
                } else {
                    return obj;
                }
            } else {
                Map<Object, Object> oldMap = (Map)obj;
                Map<Object, Object> newMap = new LinkedHashMap();
                var15 = oldMap.entrySet().iterator();

                while(var15.hasNext()) {
                    entry = (Entry)var15.next();
                    newMap.put(serialize(entry.getKey()), serialize(entry.getValue()));
                }

                return newMap;
            }
        } else {
            List<Object> serialized = new ArrayList();
            if (!(obj instanceof Iterable) && !(obj instanceof IsInList)) {
                Object[] var10 = (Object[])((Object[])obj);
                int var14 = var10.length;

                for(int var4 = 0; var4 < var14; ++var4) {
                    Object element = var10[var4];
                    serialized.add(serialize(element));
                }
            } else {
                Iterator var2 = ((Iterable)(obj instanceof IsInList ? ((IsInList)obj).getList() : (Iterable)obj)).iterator();

                while(var2.hasNext()) {
                    Object element = var2.next();
                    serialized.add(serialize(element));
                }
            }

            return serialized;
        }
    }

    public static String serializeLoc(Location loc) {
        return loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + (loc.getPitch() == 0.0F && loc.getYaw() == 0.0F ? "" : " " + Math.round(loc.getYaw()) + " " + Math.round(loc.getPitch()));
    }

    public static String serializeLocD(Location loc) {
        return loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + (loc.getPitch() == 0.0F && loc.getYaw() == 0.0F ? "" : " " + loc.getYaw() + " " + loc.getPitch());
    }

    private static String serializePotionEffect(PotionEffect effect) {
        return effect.getType().getName() + " " + effect.getDuration() + " " + effect.getAmplifier();
    }

    public static <T> T deserialize(@NonNull Class<T> classOf, @NonNull Object object) {
        if (classOf == null) {
            throw new NullPointerException("classOf is marked non-null but is null");
        } else if (object == null) {
            throw new NullPointerException("object is marked non-null but is null");
        } else {
            return deserialize(classOf, object, (Object[])null);
        }
    }

    public static <T> T deserialize(@NonNull Class<T> classOf, @NonNull Object object, Object... deserializeParameters) {
        if (classOf == null) {
            throw new NullPointerException("classOf is marked non-null but is null");
        } else if (object == null) {
            throw new NullPointerException("object is marked non-null but is null");
        } else {
            SerializedMap map = SerializedMap.of(object);
            Method deserializeMethod = ReflectionUtil.getMethod(classOf, "deserialize", new Class[]{SerializedMap.class});
            if (deserializeMethod != null) {
                return ReflectionUtil.invokeStatic(deserializeMethod, new Object[]{map});
            } else {
                Object[] array;
                int i;
                Object element;
                Object[] rawArray;
                if (deserializeParameters != null) {
                    List<Class<?>> joinedClasses = new ArrayList();
                    joinedClasses.add(SerializedMap.class);
                    array = deserializeParameters;
                    int var7 = deserializeParameters.length;

                    for(i = 0; i < var7; ++i) {
                        element = array[i];
                        joinedClasses.add(element.getClass());
                    }

                    deserializeMethod = ReflectionUtil.getMethod(classOf, "deserialize", (Class[])joinedClasses.toArray(new Class[joinedClasses.size()]));
                    List<Object> joinedParams = new ArrayList();
                    joinedParams.add(map);
                    rawArray = deserializeParameters;
                    i = deserializeParameters.length;

                    for(int var21 = 0; var21 < i; ++var21) {
                        Object param = rawArray[var21];
                        joinedParams.add(param);
                    }

                    if (deserializeMethod != null) {
                        Valid.checkBoolean(joinedClasses.size() == joinedParams.size(), "static deserialize method arguments length " + joinedClasses.size() + " != given params " + joinedParams.size(), new Object[0]);
                        return ReflectionUtil.invokeStatic(deserializeMethod, joinedParams.toArray());
                    }
                }

                if (deserializeMethod == null && object instanceof String) {
                    deserializeMethod = ReflectionUtil.getMethod(classOf, "getByName", new Class[]{String.class});
                    if (deserializeMethod != null) {
                        return ReflectionUtil.invokeStatic(deserializeMethod, new Object[]{object});
                    }
                }

                if (object != null) {
                    if (classOf == String.class) {
                        object = object.toString();
                    } else if (classOf == Integer.class) {
                        object = Double.valueOf(object.toString()).intValue();
                    } else if (classOf == Long.class) {
                        object = Double.valueOf(object.toString()).longValue();
                    } else if (classOf == Double.class) {
                        object = Double.valueOf(object.toString());
                    } else if (classOf == Float.class) {
                        object = Float.valueOf(object.toString());
                    } else if (classOf == Boolean.class) {
                        object = Boolean.valueOf(object.toString());
                    } else if (classOf == SerializedMap.class) {
                        object = SerializedMap.of(object);
                    } else if (classOf == Location.class) {
                        object = deserializeLocation(object);
                    } else if (classOf == PotionEffect.class) {
                        object = deserializePotionEffect(object);
                    } else if (classOf == CompMaterial.class) {
                        object = CompMaterial.fromString(object.toString());
                    } else if (classOf == SimpleTime.class) {
                        object = SimpleTime.from(object.toString());
                    } else if (classOf == SimpleSound.class) {
                        object = new SimpleSound(object.toString());
                    } else {
                        if (classOf == net.md_5.bungee.api.ChatColor.class) {
                            throw new FoException("Instead of net.md_5.bungee.api.ChatColor, use our CompChatColor");
                        }

                        if (classOf == CompChatColor.class) {
                            object = CompChatColor.of(object.toString());
                        } else if (classOf == UUID.class) {
                            object = UUID.fromString(object.toString());
                        } else if (classOf == BaseComponent.class) {
                            BaseComponent[] deserialized = Remain.toComponent(object.toString());
                            Valid.checkBoolean(deserialized.length == 1, "Failed to deserialize into singular BaseComponent: " + object, new Object[0]);
                            object = deserialized[0];
                        } else if (classOf == BaseComponent[].class) {
                            object = Remain.toComponent(object.toString());
                        } else {
                            SerializedMap serialized;
                            if (classOf == HoverEvent.class) {
                                serialized = SerializedMap.of(object);
                                Action action = (Action)serialized.get("Action", Action.class);
                                BaseComponent[] value = (BaseComponent[])serialized.get("Value", BaseComponent[].class);
                                object = new HoverEvent(action, value);
                            } else if (classOf == ClickEvent.class) {
                                serialized = SerializedMap.of(object);
                                net.md_5.bungee.api.chat.ClickEvent.Action action = (net.md_5.bungee.api.chat.ClickEvent.Action)serialized.get("Action", net.md_5.bungee.api.chat.ClickEvent.Action.class);
                                String value = serialized.getString("Value");
                                object = new ClickEvent(action, value);
                            } else if (Enum.class.isAssignableFrom(classOf)) {
                                object = ReflectionUtil.lookupEnum(classOf, object.toString());
                            } else if (Color.class.isAssignableFrom(classOf)) {
                                object = CompChatColor.of(object.toString()).getColor();
                            } else if ((!List.class.isAssignableFrom(classOf) || !(object instanceof List)) && (!Map.class.isAssignableFrom(classOf) || !(object instanceof Map)) && (!ConfigurationSerializable.class.isAssignableFrom(classOf) || !(object instanceof ConfigurationSerializable))) {
                                if (classOf.isArray()) {
                                    Class<?> arrayType = classOf.getComponentType();
                                    if (object instanceof List) {
                                        List<?> rawList = (List)object;
                                        array = (Object[])((Object[])Array.newInstance(classOf.getComponentType(), rawList.size()));

                                        for(i = 0; i < rawList.size(); ++i) {
                                            element = rawList.get(i);
                                            array[i] = element == null ? null : deserialize(arrayType, element, (Object[])null);
                                        }
                                    } else {
                                        rawArray = (Object[])((Object[])object);
                                        array = (Object[])((Object[])Array.newInstance(classOf.getComponentType(), rawArray.length));

                                        for(i = 0; i < array.length; ++i) {
                                            array[i] = rawArray[i] == null ? null : deserialize(classOf.getComponentType(), rawArray[i], (Object[])null);
                                        }
                                    }

                                    return array;
                                }

                                if (classOf != Object.class) {
                                    throw new SerializeUtil.SerializeFailedException("Unable to deserialize " + classOf.getSimpleName() + ", lacking static deserialize method! Data: " + object);
                                }
                            }
                        }
                    }
                }

                return object;
            }
        }
    }

    public static Location deserializeLocation(Object raw) {
        if (raw == null) {
            return null;
        } else if (raw instanceof Location) {
            return (Location)raw;
        } else {
            Object raw = raw.toString().replace("\"", "");
            String[] parts = raw.toString().contains(", ") ? raw.toString().split(", ") : raw.toString().split(" ");
            Valid.checkBoolean(parts.length == 4 || parts.length == 6, "Expected location (String) but got " + raw.getClass().getSimpleName() + ": " + raw, new Object[0]);
            String world = parts[0];
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) {
                throw new InvalidWorldException("Location with invalid world '" + world + "': " + raw + " (Doesn't exist)", world);
            } else {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                float yaw = Float.parseFloat(parts.length == 6 ? parts[4] : "0");
                float pitch = Float.parseFloat(parts.length == 6 ? parts[5] : "0");
                return new Location(bukkitWorld, (double)x, (double)y, (double)z, yaw, pitch);
            }
        }
    }

    public static Location deserializeLocationD(Object raw) {
        if (raw == null) {
            return null;
        } else if (raw instanceof Location) {
            return (Location)raw;
        } else {
            Object raw = raw.toString().replace("\"", "");
            String[] parts = raw.toString().contains(", ") ? raw.toString().split(", ") : raw.toString().split(" ");
            Valid.checkBoolean(parts.length == 4 || parts.length == 6, "Expected location (String) but got " + raw.getClass().getSimpleName() + ": " + raw, new Object[0]);
            String world = parts[0];
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) {
                throw new InvalidWorldException("Location with invalid world '" + world + "': " + raw + " (Doesn't exist)", world);
            } else {
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                float yaw = Float.parseFloat(parts.length == 6 ? parts[4] : "0");
                float pitch = Float.parseFloat(parts.length == 6 ? parts[5] : "0");
                return new Location(bukkitWorld, x, y, z, yaw, pitch);
            }
        }
    }

    private static PotionEffect deserializePotionEffect(Object raw) {
        if (raw == null) {
            return null;
        } else if (raw instanceof PotionEffect) {
            return (PotionEffect)raw;
        } else {
            String[] parts = raw.toString().split(" ");
            Valid.checkBoolean(parts.length == 3, "Expected PotionEffect (String) but got " + raw.getClass().getSimpleName() + ": " + raw, new Object[0]);
            String typeRaw = parts[0];
            PotionEffectType type = PotionEffectType.getByName(typeRaw);
            int duration = Integer.parseInt(parts[1]);
            int amplifier = Integer.parseInt(parts[2]);
            return new PotionEffect(type, duration, amplifier);
        }
    }

    /** @deprecated */
    @Deprecated
    public static <T extends ConfigSerializable> List<T> deserializeMapList(Object listOfObjects, Class<T> asWhat) {
        if (listOfObjects == null) {
            return null;
        } else {
            Valid.checkBoolean(listOfObjects instanceof ArrayList, "Only deserialize a list of maps, nie " + listOfObjects.getClass(), new Object[0]);
            List<T> loaded = new ArrayList();
            Iterator var3 = ((ArrayList)listOfObjects).iterator();

            while(var3.hasNext()) {
                Object part = var3.next();
                T deserialized = deserializeMap(part, asWhat);
                if (deserialized != null) {
                    loaded.add(deserialized);
                }
            }

            return loaded;
        }
    }

    public static <T extends ConfigSerializable> T deserializeMap(Object rawMap, Class<T> asWhat) {
        if (rawMap == null) {
            return null;
        } else {
            Valid.checkBoolean(rawMap instanceof Map, "The object to deserialize must be map, but got: " + rawMap.getClass(), new Object[0]);
            Map map = (Map)rawMap;

            Method deserialize;
            try {
                deserialize = asWhat.getMethod("deserialize", SerializedMap.class);
                Valid.checkBoolean(Modifier.isPublic(deserialize.getModifiers()) && Modifier.isStatic(deserialize.getModifiers()), asWhat + " is missing public 'public static T deserialize()' method", new Object[0]);
            } catch (NoSuchMethodException var7) {
                Common.throwError(var7, new String[]{"Class lacks a final method deserialize(SerializedMap) metoda. Tried: " + asWhat.getSimpleName()});
                return null;
            }

            Object invoked;
            try {
                invoked = deserialize.invoke((Object)null, SerializedMap.of(map));
            } catch (ReflectiveOperationException var6) {
                Common.throwError(var6, new String[]{"Error calling " + deserialize.getName() + " as " + asWhat.getSimpleName() + " with data " + map});
                return null;
            }

            Valid.checkBoolean(invoked.getClass().isAssignableFrom(asWhat), invoked.getClass().getSimpleName() + " != " + asWhat.getSimpleName(), new Object[0]);
            return (ConfigSerializable)invoked;
        }
    }

    private SerializeUtil() {
    }

    public static class SerializeFailedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public SerializeFailedException(String reason) {
            super(reason);
        }
    }
}