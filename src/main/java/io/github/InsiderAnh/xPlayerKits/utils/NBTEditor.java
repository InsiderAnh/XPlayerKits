package io.github.InsiderAnh.xPlayerKits.utils;

import io.github.InsiderAnh.xPlayerKits.enums.ServerVersion;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class NBTEditor {

    private final HashMap<String, Class<?>> classes = new HashMap<>();
    private final HashMap<String, Method> methods = new HashMap<>();
    private final ServerVersion serverVersion = XPKUtils.SERVER_VERSION;

    public NBTEditor() {
        try {
            addClass("CraftItemStack", Class.forName("org.bukkit.craftbukkit." + serverVersion + ".inventory.CraftItemStack"));
            if (!checkVersion(ServerVersion.v1_13_R1)) {
                addClass("NBTTagList", Class.forName("net.minecraft.server." + serverVersion + ".NBTTagList"));
            }
            if (checkVersion(ServerVersion.v1_17_R1)) {
                addClass("ItemStackNMS", Class.forName("net.minecraft.world.item.ItemStack"));
                addClass("NBTTagCompound", Class.forName("net.minecraft.nbt.NBTTagCompound"));
                addClass("MojangsonParser", Class.forName("net.minecraft.nbt.MojangsonParser"));
                addClass("NBTBase", Class.forName("net.minecraft.nbt.NBTBase"));
            } else {
                addClass("ItemStackNMS", Class.forName("net.minecraft.server." + serverVersion + ".ItemStack"));
                addClass("NBTTagCompound", Class.forName("net.minecraft.server." + serverVersion + ".NBTTagCompound"));
                addClass("MojangsonParser", Class.forName("net.minecraft.server." + serverVersion + ".MojangsonParser"));
                addClass("NBTBase", Class.forName("net.minecraft.server." + serverVersion + ".NBTBase"));
            }

            addMethod("asNMSCopy", getClassRef("CraftItemStack").getMethod("asNMSCopy", ItemStack.class));
            addMethod("asBukkitCopy", getClassRef("CraftItemStack").getMethod("asBukkitCopy", getClassRef("ItemStackNMS")));
            if (!checkVersion(ServerVersion.v1_13_R1)) {
                addMethod("getList", getClassRef("NBTTagCompound").getMethod("getList", String.class, int.class));
                addMethod("listSize", getClassRef("NBTTagList").getMethod("size"));
                addMethod("listGet", getClassRef("NBTTagList").getMethod("get", int.class));
                addMethod("listAdd", getClassRef("NBTTagList").getMethod("add", getClassRef("NBTBase")));
            }
            if (!checkVersion(ServerVersion.v1_18_R1)) {
                addMethod("hasTag", getClassRef("ItemStackNMS").getMethod("hasTag"));
                addMethod("getTag", getClassRef("ItemStackNMS").getMethod("getTag"));
                addMethod("setTag", getClassRef("ItemStackNMS").getMethod("setTag", getClassRef("NBTTagCompound")));
                addMethod("setString", getClassRef("NBTTagCompound").getMethod("setString", String.class, String.class));
                addMethod("setBoolean", getClassRef("NBTTagCompound").getMethod("setBoolean", String.class, boolean.class));
                addMethod("setDouble", getClassRef("NBTTagCompound").getMethod("setDouble", String.class, double.class));
                addMethod("setInt", getClassRef("NBTTagCompound").getMethod("setInt", String.class, int.class));
                addMethod("set", getClassRef("NBTTagCompound").getMethod("set", String.class, getClassRef("NBTBase")));
                addMethod("hasKey", getClassRef("NBTTagCompound").getMethod("hasKey", String.class));
                addMethod("getString", getClassRef("NBTTagCompound").getMethod("getString", String.class));
                addMethod("getBoolean", getClassRef("NBTTagCompound").getMethod("getBoolean", String.class));
                addMethod("getInt", getClassRef("NBTTagCompound").getMethod("getInt", String.class));
                addMethod("getDouble", getClassRef("NBTTagCompound").getMethod("getDouble", String.class));
                addMethod("getCompound", getClassRef("NBTTagCompound").getMethod("getCompound", String.class));
                addMethod("get", getClassRef("NBTTagCompound").getMethod("get", String.class));
                addMethod("remove", getClassRef("NBTTagCompound").getMethod("remove", String.class));
                if (checkVersion(ServerVersion.v1_13_R1)) {
                    addMethod("getKeys", getClassRef("NBTTagCompound").getMethod("getKeys"));
                } else {
                    addMethod("getKeys", getClassRef("NBTTagCompound").getMethod("c"));
                }
                addMethod("hasKeyOfType", getClassRef("NBTTagCompound").getMethod("hasKeyOfType", String.class, int.class));
                addMethod("parse", getClassRef("MojangsonParser").getMethod("parse", String.class));
            } else {
                String methodName = null;
                switch (serverVersion) {
                    case v1_18_R1:
                        methodName = "r";
                        break;
                    case v1_18_R2:
                        methodName = "s";
                        break;
                    case v1_19_R1:
                    case v1_19_R2:
                    case v1_19_R3:
                        methodName = "t";
                        break;
                    case v1_20_R1:
                    case v1_20_R2:
                    case v1_20_R3:
                        methodName = "u";
                        break;
                }
                addMethod("hasTag", getClassRef("ItemStackNMS").getMethod(methodName));

                methodName = null;
                switch (serverVersion) {
                    case v1_18_R1:
                        methodName = "s";
                        break;
                    case v1_18_R2:
                        methodName = "t";
                        break;
                    case v1_19_R1:
                    case v1_19_R2:
                    case v1_19_R3:
                        methodName = "u";
                        break;
                    case v1_20_R1:
                    case v1_20_R2:
                    case v1_20_R3:
                        methodName = "v";
                        break;
                }
                addMethod("getTag", getClassRef("ItemStackNMS").getMethod(methodName));

                addMethod("setTag", getClassRef("ItemStackNMS").getMethod("c", getClassRef("NBTTagCompound")));
                addMethod("setString", getClassRef("NBTTagCompound").getMethod("a", String.class, String.class));
                addMethod("setBoolean", getClassRef("NBTTagCompound").getMethod("a", String.class, boolean.class));
                addMethod("setDouble", getClassRef("NBTTagCompound").getMethod("a", String.class, double.class));
                addMethod("setInt", getClassRef("NBTTagCompound").getMethod("a", String.class, int.class));
                addMethod("set", getClassRef("NBTTagCompound").getMethod("a", String.class, getClassRef("NBTBase")));
                addMethod("hasKey", getClassRef("NBTTagCompound").getMethod("e", String.class));
                addMethod("getString", getClassRef("NBTTagCompound").getMethod("l", String.class));
                addMethod("getBoolean", getClassRef("NBTTagCompound").getMethod("q", String.class));
                addMethod("getInt", getClassRef("NBTTagCompound").getMethod("h", String.class));
                addMethod("getDouble", getClassRef("NBTTagCompound").getMethod("k", String.class));
                addMethod("getCompound", getClassRef("NBTTagCompound").getMethod("p", String.class));
                addMethod("get", getClassRef("NBTTagCompound").getMethod("c", String.class));
                addMethod("remove", getClassRef("NBTTagCompound").getMethod("r", String.class));

                methodName = null;
                switch (serverVersion) {
                    case v1_18_R1:
                    case v1_18_R2:
                    case v1_19_R1:
                        methodName = "d";
                        break;
                    case v1_19_R2:
                    case v1_19_R3:
                    case v1_20_R1:
                    case v1_20_R2:
                    case v1_20_R3:
                        methodName = "e";
                        break;
                }
                addMethod("getKeys", getClassRef("NBTTagCompound").getMethod(methodName));
                addMethod("hasKeyOfType", getClassRef("NBTTagCompound").getMethod("b", String.class, int.class));
                addMethod("parse", getClassRef("MojangsonParser").getMethod("a", String.class));
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public ItemStack setTag(ItemStack item, String key, String value) {
        try {
            Object newItem = getMethodRef("asNMSCopy").invoke(null, item); //ItemStackNMS
            Object compound = getNBTCompound(newItem);
            getMethodRef("setString").invoke(compound, key, value);
            getMethodRef("setTag").invoke(newItem, compound);
            return (ItemStack) getMethodRef("asBukkitCopy").invoke(null, newItem);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return item;
    }

    public String getString(ItemStack item, String key) {
        try {
            Object newItem = getMethodRef("asNMSCopy").invoke(null, item); //ItemStackNMS
            Object compound = getNBTCompound(newItem);
            if ((boolean) getMethodRef("hasKey").invoke(compound, key)) {
                return (String) getMethodRef("getString").invoke(compound, key);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasTag(ItemStack item, String key) {
        try {
            Object newItem = getMethodRef("asNMSCopy").invoke(null, item); //ItemStackNMS
            Object compound = getNBTCompound(newItem);
            if ((boolean) getMethodRef("hasKey").invoke(compound, key)) {
                return true;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ItemStack removeTag(ItemStack item, String key) {
        try {
            Object newItem = getMethodRef("asNMSCopy").invoke(null, item); //ItemStackNMS
            Object compound = getNBTCompound(newItem);
            getMethodRef("remove").invoke(compound, key);
            getMethodRef("setTag").invoke(newItem, compound);
            return (ItemStack) getMethodRef("asBukkitCopy").invoke(null, newItem);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return item;
    }

    private Object getNBTCompound(Object newItem) {
        try {
            boolean hasTag = (boolean) getMethodRef("hasTag").invoke(newItem, null);
            return hasTag ? getMethodRef("getTag").invoke(newItem, null) :
                getClassRef("NBTTagCompound").newInstance();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }


    private boolean checkVersion(ServerVersion version) {
        return serverVersion.ordinal() >= version.ordinal();
    }

    public void addClass(String name, Class<?> classType) {
        classes.put(name, classType);
    }

    public void addMethod(String name, Method methodType) {
        methods.put(name, methodType);
    }

    public Class<?> getClassRef(String name) {
        return classes.get(name);
    }

    public Method getMethodRef(String name) {
        return methods.get(name);
    }

}