package net.smart.moving.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Reflection limited to optional third-party integrations and legacy config fields. */
public final class Reflect {
    private Reflect() { }

    public static boolean CheckClasses(Class<?> owner, Name... names) {
        for (Name name : names)
            if (LoadClass(owner, name, false) == null) return false;
        return true;
    }

    public static Class<?> LoadClass(Class<?> owner, Name name, boolean required) {
        for (String candidate : candidates(name)) {
            if (candidate == null) continue;
            try { return owner.getClassLoader().loadClass(candidate); }
            catch (ClassNotFoundException ignored) { }
        }
        if (required) throw new IllegalStateException("Missing optional class " + name.deobfuscated);
        return null;
    }

    public static Field GetField(Class<?> owner, Name name) { return GetField(owner, name, true); }

    public static Field GetField(Class<?> owner, Name name, boolean required) {
        if (owner == null) {
            if (required) throw new IllegalArgumentException("Missing owner for " + name.deobfuscated);
            return null;
        }
        for (String candidate : candidates(name)) {
            if (candidate == null) continue;
            try {
                Field field = owner.getDeclaredField(candidate);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) { }
        }
        if (required) throw new IllegalStateException("Missing field " + name.deobfuscated + " in " + owner);
        return null;
    }

    public static Object GetField(Field field, Object owner) {
        try { return field.get(owner); }
        catch (IllegalAccessException e) { throw new IllegalStateException(field.toString(), e); }
    }

    public static Object GetField(Class<?> type, Object owner, Name name) {
        return GetField(GetField(type, name), owner);
    }

    public static Method GetMethod(Class<?> owner, Name name, Class<?>... parameters) {
        return GetMethod(owner, name, true, parameters);
    }

    public static Method GetMethod(Class<?> owner, Name name, boolean required, Class<?>... parameters) {
        if (owner == null) {
            if (required) throw new IllegalArgumentException("Missing owner for " + name.deobfuscated);
            return null;
        }
        for (String candidate : candidates(name)) {
            if (candidate == null) continue;
            try {
                Method method = owner.getDeclaredMethod(candidate, parameters);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) { }
        }
        if (required) throw new IllegalStateException("Missing method " + name.deobfuscated + " in " + owner);
        return null;
    }

    public static Object Invoke(Method method, Object owner, Object... args) {
        try { return method.invoke(owner, args); }
        catch (ReflectiveOperationException e) { throw new IllegalStateException(method.toString(), e); }
    }

    private static String[] candidates(Name name) {
        return new String[] { name.obfuscated, name.forgefuscated, name.deobfuscated };
    }
}
