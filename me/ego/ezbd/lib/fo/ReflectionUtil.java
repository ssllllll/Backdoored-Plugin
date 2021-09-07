package me.ego.ezbd.lib.fo;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReflectionUtil {
    public static final String NMS = "net.minecraft.server";
    public static final String CRAFTBUKKIT = "org.bukkit.craftbukkit";
    private static final Map<String, V> legacyEntityTypes;
    private static final Map<String, Class<?>> classCache = new ConcurrentHashMap();
    private static final Map<Class<?>, ReflectionUtil.ReflectionData<?>> reflectionDataCache = new ConcurrentHashMap();
    private static final Collection<String> classNameGuard = ConcurrentHashMap.newKeySet();

    public static Class<?> getNMSClass(String name) {
        String version = MinecraftVersion.getServerVersion();
        if (!version.isEmpty()) {
            version = version + ".";
        }

        return lookupClass("net.minecraft.server." + version + name);
    }

    public static Class<?> getOBCClass(String name) {
        String version = MinecraftVersion.getServerVersion();
        if (!version.isEmpty()) {
            version = version + ".";
        }

        return lookupClass("org.bukkit.craftbukkit." + version + name);
    }

    public static Constructor<?> getConstructor(@NonNull String classPath, Class<?>... params) {
        if (classPath == null) {
            throw new NullPointerException("classPath is marked non-null but is null");
        } else {
            Class<?> clazz = lookupClass(classPath);
            return getConstructor(clazz, params);
        }
    }

    public static Constructor<?> getConstructor(@NonNull Class<?> clazz, Class<?>... params) {
        if (clazz == null) {
            throw new NullPointerException("clazz is marked non-null but is null");
        } else {
            try {
                if (reflectionDataCache.containsKey(clazz)) {
                    return ((ReflectionUtil.ReflectionData)reflectionDataCache.get(clazz)).getConstructor(params);
                } else {
                    Constructor<?> constructor = clazz.getConstructor(params);
                    constructor.setAccessible(true);
                    return constructor;
                }
            } catch (ReflectiveOperationException var3) {
                throw new FoException(var3, "Could not get constructor of " + clazz + " with parameters " + Common.join(params));
            }
        }
    }

    public static <T> T getFieldContent(Object instance, String field) {
        return getFieldContent(instance.getClass(), field, instance);
    }

    public static <T> T getFieldContent(Class<?> clazz, String field, Object instance) {
        String originalClassName = clazz.getSimpleName();

        do {
            Field[] var4 = clazz.getDeclaredFields();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Field f = var4[var6];
                if (f.getName().equals(field)) {
                    return getFieldContent(f, instance);
                }
            }
        } while(!(clazz = clazz.getSuperclass()).isAssignableFrom(Object.class));

        throw new ReflectionUtil.ReflectionException("No such field " + field + " in " + originalClassName + " or its superclasses");
    }

    public static Object getFieldContent(Field field, Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (ReflectiveOperationException var3) {
            throw new ReflectionUtil.ReflectionException("Could not get field " + field.getName() + " in instance " + (instance != null ? instance : field).getClass().getSimpleName());
        }
    }

    public static Field[] getAllFields(@NonNull Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz is marked non-null but is null");
        } else {
            ArrayList list = new ArrayList();

            try {
                do {
                    list.addAll(Arrays.asList(clazz.getDeclaredFields()));
                } while(!(clazz = clazz.getSuperclass()).isAssignableFrom(Object.class));
            } catch (NullPointerException var3) {
            }

            return (Field[])list.toArray(new Field[0]);
        }
    }

    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            if (reflectionDataCache.containsKey(clazz)) {
                return ((ReflectionUtil.ReflectionData)reflectionDataCache.get(clazz)).getDeclaredField(fieldName);
            } else {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            }
        } catch (ReflectiveOperationException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static void setDeclaredField(@NonNull Object instance, String fieldName, Object fieldValue) {
        if (instance == null) {
            throw new NullPointerException("instance is marked non-null but is null");
        } else {
            Field field = getDeclaredField(instance.getClass(), fieldName);

            try {
                field.set(instance, fieldValue);
            } catch (ReflectiveOperationException var5) {
                var5.printStackTrace();
            }

        }
    }

    public static <T> T getStaticFieldContent(@NonNull Class<?> clazz, String field) {
        if (clazz == null) {
            throw new NullPointerException("clazz is marked non-null but is null");
        } else {
            return getFieldContent(clazz, field, (Object)null);
        }
    }

    public static void setStaticField(@NonNull Class<?> clazz, String fieldName, Object fieldValue) {
        if (clazz == null) {
            throw new NullPointerException("clazz is marked non-null but is null");
        } else {
            try {
                Field field = getDeclaredField(clazz, fieldName);
                field.set((Object)null, fieldValue);
            } catch (Throwable var4) {
                throw new FoException(var4, "Could not set " + fieldName + " in " + clazz + " to " + fieldValue);
            }
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... args) {
        Method[] var3 = clazz.getMethods();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Method method = var3[var5];
            if (method.getName().equals(methodName) && isClassListEqual(args, method.getParameterTypes())) {
                method.setAccessible(true);
                return method;
            }
        }

        return null;
    }

    private static boolean isClassListEqual(Class<?>[] first, Class<?>[] second) {
        if (first.length != second.length) {
            return false;
        } else {
            for(int i = 0; i < first.length; ++i) {
                if (first[i] != second[i]) {
                    return false;
                }
            }

            return true;
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName) {
        Method[] var2 = clazz.getMethods();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Method method = var2[var4];
            if (method.getName().equals(methodName)) {
                method.setAccessible(true);
                return method;
            }
        }

        return null;
    }

    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            return reflectionDataCache.containsKey(clazz) ? ((ReflectionUtil.ReflectionData)reflectionDataCache.get(clazz)).getDeclaredMethod(methodName, args) : ((ReflectionUtil.ReflectionData)reflectionDataCache.computeIfAbsent(clazz, ReflectionUtil.ReflectionData::new)).getDeclaredMethod(methodName, args);
        } catch (ReflectiveOperationException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public static <T> T invokeStatic(Class<?> cl, String methodName, Object... params) {
        return invokeStatic(getMethod(cl, methodName), params);
    }

    public static <T> T invokeStatic(Method method, Object... params) {
        try {
            return method.invoke((Object)null, params);
        } catch (ReflectiveOperationException var3) {
            throw new ReflectionUtil.ReflectionException("Could not invoke static method " + method + " with params " + StringUtils.join(params), var3);
        }
    }

    public static <T> T invoke(String methodName, Object instance, Object... params) {
        return invoke(getMethod(instance.getClass(), methodName), instance, params);
    }

    public static <T> T invoke(Method method, Object instance, Object... params) {
        Valid.checkNotNull(method, "Method cannot be null for " + instance);

        try {
            return method.invoke(instance, params);
        } catch (ReflectiveOperationException var4) {
            throw new ReflectionUtil.ReflectionException("Could not invoke method " + method + " on instance " + instance + " with params " + StringUtils.join(params), var4);
        }
    }

    public static <T> T instantiate(Class<T> clazz) {
        try {
            Constructor constructor;
            if (reflectionDataCache.containsKey(clazz)) {
                constructor = ((ReflectionUtil.ReflectionData)reflectionDataCache.get(clazz)).getDeclaredConstructor();
            } else {
                constructor = clazz.getDeclaredConstructor();
            }

            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException var2) {
            throw new ReflectionUtil.ReflectionException("Could not make instance of: " + clazz, var2);
        }
    }

    public static <T> T instantiate(Class<T> clazz, Object... params) {
        try {
            List<Class<?>> classes = new ArrayList();
            Object[] var3 = params;
            int var4 = params.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Object param = var3[var5];
                Valid.checkNotNull(param, "Argument cannot be null when instatiating " + clazz);
                Class<?> paramClass = param.getClass();
                classes.add(paramClass.isPrimitive() ? ClassUtils.wrapperToPrimitive(paramClass) : paramClass);
            }

            Class<?>[] paramArr = (Class[])classes.toArray(new Class[0]);
            Constructor constructor;
            if (reflectionDataCache.containsKey(clazz)) {
                constructor = ((ReflectionUtil.ReflectionData)reflectionDataCache.get(clazz)).getDeclaredConstructor(paramArr);
            } else {
                classCache.put(clazz.getCanonicalName(), clazz);
                constructor = ((ReflectionUtil.ReflectionData)reflectionDataCache.computeIfAbsent(clazz, ReflectionUtil.ReflectionData::new)).getDeclaredConstructor(paramArr);
            }

            constructor.setAccessible(true);
            return constructor.newInstance(params);
        } catch (ReflectiveOperationException var8) {
            throw new ReflectionUtil.ReflectionException("Could not make instance of: " + clazz, var8);
        }
    }

    public static <T> T instantiate(Constructor<T> constructor, Object... params) {
        try {
            return constructor.newInstance(params);
        } catch (ReflectiveOperationException var3) {
            throw new FoException(var3, "Could not make new instance of " + constructor + " with params: " + Common.join(params));
        }
    }

    public static boolean isClassAvailable(String path) {
        try {
            if (classCache.containsKey(path)) {
                return true;
            } else {
                Class.forName(path);
                return true;
            }
        } catch (Throwable var2) {
            return false;
        }
    }

    public static <T> Class<T> lookupClass(String path) {
        if (classCache.containsKey(path)) {
            return (Class)classCache.get(path);
        } else if (!classNameGuard.contains(path)) {
            Class var2;
            try {
                classNameGuard.add(path);
                Class<?> clazz = Class.forName(path);
                classCache.put(path, clazz);
                reflectionDataCache.computeIfAbsent(clazz, ReflectionUtil.ReflectionData::new);
                var2 = clazz;
            } catch (ClassNotFoundException var6) {
                throw new ReflectionUtil.ReflectionException("Could not find class: " + path);
            } finally {
                classNameGuard.remove(path);
            }

            return var2;
        } else {
            while(classNameGuard.contains(path)) {
            }

            return lookupClass(path);
        }
    }

    public static <E extends Enum<E>> E lookupEnumCompat(Class<E> enumType, String name) {
        try {
            return (Enum)(enumType == CompMaterial.class ? CompMaterial.fromStringCompat(name) : lookupEnum(enumType, name));
        } catch (ReflectionUtil.MissingEnumException var4) {
            if (enumType == EntityType.class) {
                V since = (V)legacyEntityTypes.get(name.toUpperCase().replace(" ", "_"));
                if (since != null && MinecraftVersion.olderThan(since)) {
                    return null;
                }
            }

            throw var4;
        }
    }

    public static <E extends Enum<E>> E lookupEnum(Class<E> enumType, String name) {
        return lookupEnum(enumType, name, "The enum '" + enumType.getSimpleName() + "' does not contain '" + name + "' on MC " + MinecraftVersion.getServerVersion() + "! Available values: {available}");
    }

    public static <E extends Enum<E>> E lookupEnum(Class<E> enumType, String name, String errMessage) {
        Valid.checkNotNull(enumType, "Type missing for " + name);
        Valid.checkNotNull(name, "Name missing for " + enumType);
        String rawName = name.toUpperCase().replace(" ", "_");
        if (enumType == Biome.class && MinecraftVersion.atLeast(V.v1_13) && rawName.equalsIgnoreCase("ICE_MOUNTAINS")) {
            name = "SNOWY_TAIGA";
        }

        if (enumType == EntityType.class) {
            if (MinecraftVersion.atLeast(V.v1_16) && rawName.equals("PIG_ZOMBIE")) {
                name = "ZOMBIFIED_PIGLIN";
            }

            if (MinecraftVersion.atLeast(V.v1_14) && rawName.equals("TIPPED_ARROW")) {
                name = "ARROW";
            }

            if (MinecraftVersion.olderThan(V.v1_16) && rawName.equals("ZOMBIFIED_PIGLIN")) {
                name = "PIG_ZOMBIE";
            }

            if (MinecraftVersion.olderThan(V.v1_9)) {
                if (rawName.equals("TRIDENT")) {
                    name = "ARROW";
                } else if (rawName.equals("DRAGON_FIREBALL")) {
                    name = "FIREBALL";
                }
            }

            if (MinecraftVersion.olderThan(V.v1_13)) {
                if (rawName.equals("DROWNED")) {
                    name = "ZOMBIE";
                } else if (rawName.equals("ZOMBIE_VILLAGER")) {
                    name = "ZOMBIE";
                }
            }
        }

        if (enumType == DamageCause.class) {
            if (MinecraftVersion.olderThan(V.v1_13) && rawName.equals("DRYOUT")) {
                name = "CUSTOM";
            }

            if (MinecraftVersion.olderThan(V.v1_11)) {
                if (rawName.equals("ENTITY_SWEEP_ATTACK")) {
                    name = "ENTITY_ATTACK";
                } else if (rawName.equals("CRAMMING")) {
                    name = "CUSTOM";
                }
            }

            if (MinecraftVersion.olderThan(V.v1_9)) {
                if (rawName.equals("FLY_INTO_WALL")) {
                    name = "SUFFOCATION";
                } else if (rawName.equals("HOT_FLOOR")) {
                    name = "LAVA";
                }
            }

            if (rawName.equals("DRAGON_BREATH")) {
                try {
                    DamageCause.valueOf("DRAGON_BREATH");
                } catch (Throwable var7) {
                    name = "ENTITY_ATTACK";
                }
            }
        }

        E result = lookupEnumSilent(enumType, name);
        if (result == null) {
            name = name.toUpperCase();
            result = lookupEnumSilent(enumType, name);
        }

        if (result == null) {
            name = name.replace(" ", "_");
            result = lookupEnumSilent(enumType, name);
        }

        if (result == null) {
            result = lookupEnumSilent(enumType, name.replace("_", ""));
        }

        if (result == null && enumType == Material.class) {
            CompMaterial compMaterial = CompMaterial.fromString(name);
            if (compMaterial != null) {
                return compMaterial.getMaterial();
            }
        }

        if (result == null) {
            throw new ReflectionUtil.MissingEnumException(name, errMessage.replace("{available}", StringUtils.join(enumType.getEnumConstants(), ", ")));
        } else {
            return result;
        }
    }

    public static <E extends Enum<E>> E lookupEnumSilent(Class<E> enumType, String name) {
        try {
            boolean hasKey = false;
            Method method = null;

            try {
                method = enumType.getDeclaredMethod("fromKey", String.class);
                if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
                    hasKey = true;
                }
            } catch (Throwable var5) {
            }

            return hasKey ? (Enum)method.invoke((Object)null, name) : Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException var6) {
            return null;
        } catch (ReflectiveOperationException var7) {
            return null;
        }
    }

    public static String getCallerMethods(int skipMethods, int count) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StringBuilder methods = new StringBuilder();
        int counted = 0;

        for(int i = 2 + skipMethods; i < elements.length && counted < count; ++i) {
            StackTraceElement el = elements[i];
            if (!el.getMethodName().equals("getCallerMethods") && el.getClassName().indexOf("java.lang.Thread") != 0) {
                String[] clazz = el.getClassName().split("\\.");
                methods.append(clazz[clazz.length == 0 ? 0 : clazz.length - 1]).append("#").append(el.getLineNumber()).append("-").append(el.getMethodName()).append("()").append(i + 1 == elements.length ? "" : ".");
                ++counted;
            }
        }

        return methods.toString();
    }

    public static <T> List<Class<? extends T>> getClasses(Plugin plugin, @NonNull Class<T> extendingClass) {
        if (extendingClass == null) {
            throw new NullPointerException("extendingClass is marked non-null but is null");
        } else {
            List<Class<? extends T>> found = new ArrayList();
            Iterator var3 = getClasses(plugin).iterator();

            while(var3.hasNext()) {
                Class<?> clazz = (Class)var3.next();
                if (extendingClass.isAssignableFrom(clazz) && clazz != extendingClass) {
                    found.add(clazz);
                }
            }

            return found;
        }
    }

    public static TreeSet<Class<?>> getClasses(Plugin plugin) {
        try {
            Valid.checkNotNull(plugin, "Plugin is null!");
            Valid.checkBoolean(JavaPlugin.class.isAssignableFrom(plugin.getClass()), "Plugin must be a JavaPlugin", new Object[0]);
            Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
            getFileMethod.setAccessible(true);
            File pluginFile = (File)getFileMethod.invoke(plugin);
            TreeSet<Class<?>> classes = new TreeSet(Comparator.comparing(Class::toString));
            JarFile jarFile = new JarFile(pluginFile);
            Throwable var5 = null;

            try {
                Enumeration entries = jarFile.entries();

                while(entries.hasMoreElements()) {
                    String name = ((JarEntry)entries.nextElement()).getName();
                    if (name.endsWith(".class")) {
                        name = name.replace("/", ".").replaceFirst(".class", "");

                        Class clazz;
                        try {
                            clazz = Class.forName(name);
                        } catch (Throwable var27) {
                            continue;
                        } finally {
                            ;
                        }

                        classes.add(clazz);
                    }
                }
            } catch (Throwable var29) {
                var5 = var29;
                throw var29;
            } finally {
                if (jarFile != null) {
                    if (var5 != null) {
                        try {
                            jarFile.close();
                        } catch (Throwable var26) {
                            var5.addSuppressed(var26);
                        }
                    } else {
                        jarFile.close();
                    }
                }

            }

            return classes;
        } catch (Throwable var31) {
            throw var31;
        }
    }

    private ReflectionUtil() {
    }

    static {
        Map<String, V> map = new HashMap();
        map.put("TIPPED_ARROW", V.v1_9);
        map.put("SPECTRAL_ARROW", V.v1_9);
        map.put("SHULKER_BULLET", V.v1_9);
        map.put("DRAGON_FIREBALL", V.v1_9);
        map.put("SHULKER", V.v1_9);
        map.put("AREA_EFFECT_CLOUD", V.v1_9);
        map.put("LINGERING_POTION", V.v1_9);
        map.put("POLAR_BEAR", V.v1_10);
        map.put("HUSK", V.v1_10);
        map.put("ELDER_GUARDIAN", V.v1_11);
        map.put("WITHER_SKELETON", V.v1_11);
        map.put("STRAY", V.v1_11);
        map.put("DONKEY", V.v1_11);
        map.put("MULE", V.v1_11);
        map.put("EVOKER_FANGS", V.v1_11);
        map.put("EVOKER", V.v1_11);
        map.put("VEX", V.v1_11);
        map.put("VINDICATOR", V.v1_11);
        map.put("ILLUSIONER", V.v1_12);
        map.put("PARROT", V.v1_12);
        map.put("TURTLE", V.v1_13);
        map.put("PHANTOM", V.v1_13);
        map.put("TRIDENT", V.v1_13);
        map.put("COD", V.v1_13);
        map.put("SALMON", V.v1_13);
        map.put("PUFFERFISH", V.v1_13);
        map.put("TROPICAL_FISH", V.v1_13);
        map.put("DROWNED", V.v1_13);
        map.put("DOLPHIN", V.v1_13);
        map.put("CAT", V.v1_14);
        map.put("PANDA", V.v1_14);
        map.put("PILLAGER", V.v1_14);
        map.put("RAVAGER", V.v1_14);
        map.put("TRADER_LLAMA", V.v1_14);
        map.put("WANDERING_TRADER", V.v1_14);
        map.put("FOX", V.v1_14);
        map.put("BEE", V.v1_15);
        map.put("HOGLIN", V.v1_16);
        map.put("PIGLIN", V.v1_16);
        map.put("STRIDER", V.v1_16);
        map.put("ZOGLIN", V.v1_16);
        map.put("PIGLIN_BRUTE", V.v1_16);
        legacyEntityTypes = map;
    }

    public static final class MissingEnumException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String enumName;

        public MissingEnumException(String enumName, String msg) {
            super(msg);
            this.enumName = enumName;
        }

        public MissingEnumException(String enumName, String msg, Exception ex) {
            super(msg, ex);
            this.enumName = enumName;
        }

        public String getEnumName() {
            return this.enumName;
        }
    }

    public static final class ReflectionException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ReflectionException(String msg) {
            super(msg);
        }

        public ReflectionException(String msg, Exception ex) {
            super(msg, ex);
        }
    }

    private static final class ReflectionData<T> {
        private final Class<T> clazz;
        private final Map<String, Collection<Method>> methodCache = new ConcurrentHashMap();
        private final Map<Integer, Constructor<?>> constructorCache = new ConcurrentHashMap();
        private final Map<String, Field> fieldCache = new ConcurrentHashMap();
        private final Collection<String> fieldGuard = ConcurrentHashMap.newKeySet();
        private final Collection<Integer> constructorGuard = ConcurrentHashMap.newKeySet();

        ReflectionData(Class<T> clazz) {
            this.clazz = clazz;
        }

        public void cacheConstructor(Constructor<T> constructor) {
            List<Class<?>> classes = new ArrayList();
            Class[] var3 = constructor.getParameterTypes();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Class<?> param = var3[var5];
                Valid.checkNotNull(param, "Argument cannot be null when instatiating " + this.clazz);
                classes.add(param);
            }

            this.constructorCache.put(Arrays.hashCode(classes.toArray(new Class[0])), constructor);
        }

        public Constructor<T> getDeclaredConstructor(Class<?>... paramTypes) throws NoSuchMethodException {
            Integer hashCode = Arrays.hashCode(paramTypes);
            if (this.constructorCache.containsKey(hashCode)) {
                return (Constructor)this.constructorCache.get(hashCode);
            } else if (!this.constructorGuard.contains(hashCode)) {
                this.constructorGuard.add(hashCode);

                Constructor var4;
                try {
                    Constructor<T> constructor = this.clazz.getDeclaredConstructor(paramTypes);
                    this.cacheConstructor(constructor);
                    var4 = constructor;
                } finally {
                    this.constructorGuard.remove(hashCode);
                }

                return var4;
            } else {
                while(this.constructorGuard.contains(hashCode)) {
                }

                return this.getDeclaredConstructor(paramTypes);
            }
        }

        public Constructor<T> getConstructor(Class<?>... paramTypes) throws NoSuchMethodException {
            Integer hashCode = Arrays.hashCode(paramTypes);
            if (this.constructorCache.containsKey(hashCode)) {
                return (Constructor)this.constructorCache.get(hashCode);
            } else if (!this.constructorGuard.contains(hashCode)) {
                this.constructorGuard.add(hashCode);

                Constructor var4;
                try {
                    Constructor<T> constructor = this.clazz.getConstructor(paramTypes);
                    this.cacheConstructor(constructor);
                    var4 = constructor;
                } finally {
                    this.constructorGuard.remove(hashCode);
                }

                return var4;
            } else {
                while(this.constructorGuard.contains(hashCode)) {
                }

                return this.getConstructor(paramTypes);
            }
        }

        public void cacheMethod(Method method) {
            ((Collection)this.methodCache.computeIfAbsent(method.getName(), (unused) -> {
                return ConcurrentHashMap.newKeySet();
            })).add(method);
        }

        public Method getDeclaredMethod(String name, Class<?>... paramTypes) throws NoSuchMethodException {
            if (this.methodCache.containsKey(name)) {
                Collection<Method> methods = (Collection)this.methodCache.get(name);
                Iterator var4 = methods.iterator();

                while(var4.hasNext()) {
                    Method method = (Method)var4.next();
                    if (Arrays.equals(paramTypes, method.getParameterTypes())) {
                        return method;
                    }
                }
            }

            Method method = this.clazz.getDeclaredMethod(name, paramTypes);
            this.cacheMethod(method);
            return method;
        }

        public void cacheField(Field field) {
            this.fieldCache.put(field.getName(), field);
        }

        public Field getDeclaredField(String name) throws NoSuchFieldException {
            if (this.fieldCache.containsKey(name)) {
                return (Field)this.fieldCache.get(name);
            } else if (!this.fieldGuard.contains(name)) {
                this.fieldGuard.add(name);

                Field var3;
                try {
                    Field field = this.clazz.getDeclaredField(name);
                    this.cacheField(field);
                    var3 = field;
                } finally {
                    this.fieldGuard.remove(name);
                }

                return var3;
            } else {
                while(this.fieldGuard.contains(name)) {
                }

                return this.getDeclaredField(name);
            }
        }
    }
}