package me.ego.ezbd.lib.fo.visual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.BlockUtil;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.Valid;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.region.Region;
import me.ego.ezbd.lib.fo.remain.CompParticle;
import me.ego.ezbd.lib.fo.remain.CompRunnable;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class VisualizedRegion extends Region {
    private final List<Player> viewers = new ArrayList();
    private BukkitTask task;
    private CompParticle particle;

    public VisualizedRegion(@Nullable Location primary, @Nullable Location secondary) {
        super(primary, secondary);
        this.particle = CompParticle.VILLAGER_HAPPY;
    }

    public VisualizedRegion(@Nullable String name, Location primary, @Nullable Location secondary) {
        super(name, primary, secondary);
        this.particle = CompParticle.VILLAGER_HAPPY;
    }

    public void showParticles(Player player, int durationTicks) {
        this.showParticles(player);
        Common.runLater(durationTicks, () -> {
            if (this.canSeeParticles(player)) {
                this.hideParticles(player);
            }

        });
    }

    public void showParticles(Player player) {
        Valid.checkBoolean(!this.canSeeParticles(player), "Player " + player.getName() + " already sees region " + this, new Object[0]);
        Valid.checkBoolean(this.isWhole(), "Cannot show particles of an incomplete region " + this, new Object[0]);
        this.viewers.add(player);
        if (this.task == null) {
            this.startVisualizing();
        }

    }

    public void hideParticles(Player player) {
        Valid.checkBoolean(this.canSeeParticles(player), "Player " + player.getName() + " is not seeing region " + this, new Object[0]);
        this.viewers.remove(player);
        if (this.viewers.isEmpty() && this.task != null) {
            this.stopVisualizing();
        }

    }

    public boolean canSeeParticles(Player player) {
        return this.viewers.contains(player);
    }

    private void startVisualizing() {
        Valid.checkBoolean(this.task == null, "Already visualizing region " + this + "!", new Object[0]);
        Valid.checkBoolean(this.isWhole(), "Cannot visualize incomplete region " + this + "!", new Object[0]);
        this.task = Common.runTimer(23, new CompRunnable() {
            public void run() {
                if (VisualizedRegion.this.viewers.isEmpty()) {
                    VisualizedRegion.this.stopVisualizing();
                } else {
                    Set<Location> blocks = BlockUtil.getBoundingBox(VisualizedRegion.this.getPrimary(), VisualizedRegion.this.getSecondary());
                    Iterator var2 = blocks.iterator();

                    while(var2.hasNext()) {
                        Location location = (Location)var2.next();
                        Iterator var4 = VisualizedRegion.this.viewers.iterator();

                        while(var4.hasNext()) {
                            Player viewer = (Player)var4.next();
                            Location viewerLocation = viewer.getLocation();
                            if (viewerLocation.getWorld().equals(location.getWorld()) && viewerLocation.distance(location) < 100.0D) {
                                VisualizedRegion.this.particle.spawnFor(viewer, location);
                            }
                        }
                    }

                }
            }
        });
    }

    private void stopVisualizing() {
        Valid.checkNotNull(this.task, "Region " + this + " not visualized");
        this.task.cancel();
        this.task = null;
        this.viewers.clear();
    }

    public static VisualizedRegion deserialize(SerializedMap map) {
        Valid.checkBoolean(map.containsKey("Primary") && map.containsKey("Secondary"), "The region must have Primary and a Secondary location", new Object[0]);
        String name = map.getString("Name");
        Location prim = map.getLocation("Primary");
        Location sec = map.getLocation("Secondary");
        return new VisualizedRegion(name, prim, sec);
    }

    public void setParticle(CompParticle particle) {
        this.particle = particle;
    }
}
