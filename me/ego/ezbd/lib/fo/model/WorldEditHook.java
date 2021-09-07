package me.ego.ezbd.lib.fo.model;

class WorldEditHook {
    public final boolean legacy;

    public WorldEditHook() {
        boolean ok = false;

        try {
            Class.forName("com.sk89q.worldedit.world.World");
            ok = true;
        } catch (ClassNotFoundException var3) {
        }

        this.legacy = !ok;
    }
}