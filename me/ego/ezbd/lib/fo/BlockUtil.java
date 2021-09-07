package me.ego.ezbd.lib.fo;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import me.ego.ezbd.lib.fo.MinecraftVersion.V;
import me.ego.ezbd.lib.fo.remain.CompMaterial;
import me.ego.ezbd.lib.fo.remain.Remain;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

public final class BlockUtil {
    private static final Pattern SLAB_PATTERN = Pattern.compile("(?!DOUBLE).*STEP");
    private static final BlockFace[] TREE_TRUNK_FACES;
    private static final Set<String> TREE_GROUND_BLOCKS;
    public static double BOUNDING_VERTICAL_GAP;
    public static double BOUNDING_HORIZONTAL_GAP;

    public static boolean isWithinCuboid(Location location, Location primary, Location secondary) {
        double locX = location.getX();
        double locY = location.getY();
        double locZ = location.getZ();
        int x = primary.getBlockX();
        int y = primary.getBlockY();
        int z = primary.getBlockZ();
        int x1 = secondary.getBlockX();
        int y1 = secondary.getBlockY();
        int z1 = secondary.getBlockZ();
        return (locX >= (double)x && locX <= (double)x1 || locX <= (double)x && locX >= (double)x1) && (locZ >= (double)z && locZ <= (double)z1 || locZ <= (double)z && locZ >= (double)z1) && (locY >= (double)y && locY <= (double)y1 || locY <= (double)y && locY >= (double)y1);
    }

    public static Set<Location> getBoundingBox(Location primary, Location secondary) {
        List<BlockUtil.VectorHelper> shape = new ArrayList();
        BlockUtil.VectorHelper min = getMinimumPoint(primary, secondary);
        BlockUtil.VectorHelper max = getMaximumPoint(primary, secondary).add(1.0D, 0.0D, 1.0D);
        int height = getHeight(primary, secondary);
        List<BlockUtil.VectorHelper> bottomCorners = new ArrayList();
        bottomCorners.add(new BlockUtil.VectorHelper(min.getX(), min.getY(), min.getZ()));
        bottomCorners.add(new BlockUtil.VectorHelper(max.getX(), min.getY(), min.getZ()));
        bottomCorners.add(new BlockUtil.VectorHelper(max.getX(), min.getY(), max.getZ()));
        bottomCorners.add(new BlockUtil.VectorHelper(min.getX(), min.getY(), max.getZ()));

        BlockUtil.VectorHelper p2;
        for(int i = 0; i < bottomCorners.size(); ++i) {
            BlockUtil.VectorHelper p1 = (BlockUtil.VectorHelper)bottomCorners.get(i);
            p2 = i + 1 < bottomCorners.size() ? (BlockUtil.VectorHelper)bottomCorners.get(i + 1) : (BlockUtil.VectorHelper)bottomCorners.get(0);
            BlockUtil.VectorHelper p3 = p1.add(0.0D, (double)height, 0.0D);
            BlockUtil.VectorHelper p4 = p2.add(0.0D, (double)height, 0.0D);
            shape.addAll(plotLine(p1, p2));
            shape.addAll(plotLine(p3, p4));
            shape.addAll(plotLine(p1, p3));

            for(double offset = BOUNDING_VERTICAL_GAP; offset < (double)height; offset += BOUNDING_VERTICAL_GAP) {
                BlockUtil.VectorHelper p5 = p1.add(0.0D, offset, 0.0D);
                BlockUtil.VectorHelper p6 = p2.add(0.0D, offset, 0.0D);
                shape.addAll(plotLine(p5, p6));
            }
        }

        Set<Location> locations = new HashSet();
        Iterator var17 = shape.iterator();

        while(var17.hasNext()) {
            p2 = (BlockUtil.VectorHelper)var17.next();
            locations.add(new Location(primary.getWorld(), p2.getX(), p2.getY(), p2.getZ()));
        }

        return locations;
    }

    private static List<BlockUtil.VectorHelper> plotLine(BlockUtil.VectorHelper p1, BlockUtil.VectorHelper p2) {
        List<BlockUtil.VectorHelper> ShapeVectors = new ArrayList();
        int points = (int)(p1.distance(p2) / BOUNDING_HORIZONTAL_GAP) + 1;
        double length = p1.distance(p2);
        double gap = length / (double)(points - 1);
        BlockUtil.VectorHelper gapShapeVector = p2.subtract(p1).normalize().multiply(gap);

        for(int i = 0; i < points; ++i) {
            BlockUtil.VectorHelper currentPoint = p1.add(gapShapeVector.multiply((double)i));
            ShapeVectors.add(currentPoint);
        }

        return ShapeVectors;
    }

    public static Set<Location> getSphere(Location location, int radius, boolean hollow) {
        Set<Location> blocks = new HashSet();
        World world = location.getWorld();
        int X = location.getBlockX();
        int Y = location.getBlockY();
        int Z = location.getBlockZ();
        int radiusSquared = radius * radius;
        int x;
        int y;
        int z;
        if (hollow) {
            for(x = X - radius; x <= X + radius; ++x) {
                for(y = Y - radius; y <= Y + radius; ++y) {
                    for(z = Z - radius; z <= Z + radius; ++z) {
                        if ((X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z) <= radiusSquared) {
                            blocks.add(new Location(world, (double)x, (double)y, (double)z));
                        }
                    }
                }
            }

            return makeHollow(blocks, true);
        } else {
            for(x = X - radius; x <= X + radius; ++x) {
                for(y = Y - radius; y <= Y + radius; ++y) {
                    for(z = Z - radius; z <= Z + radius; ++z) {
                        if ((X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z) <= radiusSquared) {
                            blocks.add(new Location(world, (double)x, (double)y, (double)z));
                        }
                    }
                }
            }

            return blocks;
        }
    }

    public static Set<Location> getCircle(Location location, int radius, boolean hollow) {
        Set<Location> blocks = new HashSet();
        World world = location.getWorld();
        int initialX = location.getBlockX();
        int initialY = location.getBlockY();
        int initialZ = location.getBlockZ();
        int radiusSquared = radius * radius;
        int x;
        int z;
        if (hollow) {
            for(x = initialX - radius; x <= initialX + radius; ++x) {
                for(z = initialZ - radius; z <= initialZ + radius; ++z) {
                    if ((initialX - x) * (initialX - x) + (initialZ - z) * (initialZ - z) <= radiusSquared) {
                        blocks.add(new Location(world, (double)x, (double)initialY, (double)z));
                    }
                }
            }

            return makeHollow(blocks, false);
        } else {
            for(x = initialX - radius; x <= initialX + radius; ++x) {
                for(z = initialZ - radius; z <= initialZ + radius; ++z) {
                    if ((initialX - x) * (initialX - x) + (initialZ - z) * (initialZ - z) <= radiusSquared) {
                        blocks.add(new Location(world, (double)x, (double)initialY, (double)z));
                    }
                }
            }

            return blocks;
        }
    }

    private static Set<Location> makeHollow(Set<Location> blocks, boolean sphere) {
        Set<Location> edge = new HashSet();
        Iterator var3;
        Location location;
        World world;
        int x;
        int y;
        int z;
        Location front;
        Location back;
        Location left;
        Location right;
        if (!sphere) {
            var3 = blocks.iterator();

            while(true) {
                do {
                    if (!var3.hasNext()) {
                        return edge;
                    }

                    location = (Location)var3.next();
                    world = location.getWorld();
                    x = location.getBlockX();
                    y = location.getBlockY();
                    z = location.getBlockZ();
                    front = new Location(world, (double)(x + 1), (double)y, (double)z);
                    back = new Location(world, (double)(x - 1), (double)y, (double)z);
                    left = new Location(world, (double)x, (double)y, (double)(z + 1));
                    right = new Location(world, (double)x, (double)y, (double)(z - 1));
                } while(blocks.contains(front) && blocks.contains(back) && blocks.contains(left) && blocks.contains(right));

                edge.add(location);
            }
        } else {
            var3 = blocks.iterator();

            while(true) {
                Location top;
                Location bottom;
                do {
                    if (!var3.hasNext()) {
                        return edge;
                    }

                    location = (Location)var3.next();
                    world = location.getWorld();
                    x = location.getBlockX();
                    y = location.getBlockY();
                    z = location.getBlockZ();
                    front = new Location(world, (double)(x + 1), (double)y, (double)z);
                    back = new Location(world, (double)(x - 1), (double)y, (double)z);
                    left = new Location(world, (double)x, (double)y, (double)(z + 1));
                    right = new Location(world, (double)x, (double)y, (double)(z - 1));
                    top = new Location(world, (double)x, (double)(y + 1), (double)z);
                    bottom = new Location(world, (double)x, (double)(y - 1), (double)z);
                } while(blocks.contains(front) && blocks.contains(back) && blocks.contains(left) && blocks.contains(right) && blocks.contains(top) && blocks.contains(bottom));

                edge.add(location);
            }
        }
    }

    public static List<Block> getBlocks(Location primary, Location secondary) {
        Valid.checkNotNull(primary, "Primary region point must be set!");
        Valid.checkNotNull(secondary, "Secondary region point must be set!");
        List<Block> blocks = new ArrayList();
        int topBlockX = primary.getBlockX() < secondary.getBlockX() ? secondary.getBlockX() : primary.getBlockX();
        int bottomBlockX = primary.getBlockX() > secondary.getBlockX() ? secondary.getBlockX() : primary.getBlockX();
        int topBlockY = primary.getBlockY() < secondary.getBlockY() ? secondary.getBlockY() : primary.getBlockY();
        int bottomBlockY = primary.getBlockY() > secondary.getBlockY() ? secondary.getBlockY() : primary.getBlockY();
        int topBlockZ = primary.getBlockZ() < secondary.getBlockZ() ? secondary.getBlockZ() : primary.getBlockZ();
        int bottomBlockZ = primary.getBlockZ() > secondary.getBlockZ() ? secondary.getBlockZ() : primary.getBlockZ();

        for(int x = bottomBlockX; x <= topBlockX; ++x) {
            for(int z = bottomBlockZ; z <= topBlockZ; ++z) {
                for(int y = bottomBlockY; y <= topBlockY; ++y) {
                    Block block = primary.getWorld().getBlockAt(x, y, z);
                    if (block != null) {
                        blocks.add(block);
                    }
                }
            }
        }

        return blocks;
    }

    public static List<Block> getBlocks(Location loc, int height, int radius) {
        List<Block> blocks = new ArrayList();

        for(int y = 0; y < height; ++y) {
            for(int x = -radius; x <= radius; ++x) {
                for(int z = -radius; z <= radius; ++z) {
                    Block checkBlock = loc.getBlock().getRelative(x, y, z);
                    if (checkBlock != null && checkBlock.getType() != Material.AIR) {
                        blocks.add(checkBlock);
                    }
                }
            }
        }

        return blocks;
    }

    public static List<Chunk> getChunks(Location location, int radius) {
        HashSet<Chunk> addedChunks = new HashSet();
        World world = location.getWorld();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        for(int x = chunkX - radius; x <= chunkX + radius; ++x) {
            for(int z = chunkZ - radius; z <= chunkZ + radius; ++z) {
                if (world.isChunkLoaded(x, z)) {
                    addedChunks.add(world.getChunkAt(x, z));
                }
            }
        }

        return new ArrayList(addedChunks);
    }

    public static List<Block> getTreePartsUp(Block treeBase) {
        Material baseMaterial = treeBase.getState().getType();
        String logType = MinecraftVersion.atLeast(V.v1_13) ? baseMaterial.toString() : "LOG";
        String leaveType = MinecraftVersion.atLeast(V.v1_13) ? logType.replace("_LOG", "") + "_LEAVES" : "LEAVES";
        Set<Block> treeParts = new HashSet();
        Set<Block> toSearch = new HashSet();
        Set<Block> searched = new HashSet();
        toSearch.add(treeBase.getRelative(BlockFace.UP));
        searched.add(treeBase);

        for(int cycle = 0; cycle < 1000 && !toSearch.isEmpty(); ++cycle) {
            Block block = (Block)toSearch.iterator().next();
            toSearch.remove(block);
            searched.add(block);
            if (!block.getType().toString().equals(logType) && !block.getType().toString().equals(leaveType)) {
                if (!block.getType().isTransparent()) {
                    return new ArrayList();
                }
            } else {
                treeParts.add(block);
                BlockFace[] var9 = TREE_TRUNK_FACES;
                int var10 = var9.length;

                for(int var11 = 0; var11 < var10; ++var11) {
                    BlockFace face = var9[var11];
                    Block relative = block.getRelative(face);
                    if (!searched.contains(relative)) {
                        toSearch.add(relative);
                    }
                }
            }
        }

        return new ArrayList(treeParts);
    }

    public static boolean isLogOnGround(Block treeBaseBlock) {
        while(CompMaterial.isLog(treeBaseBlock.getType())) {
            treeBaseBlock = treeBaseBlock.getRelative(BlockFace.DOWN);
        }

        return TREE_GROUND_BLOCKS.contains(CompMaterial.fromMaterial(treeBaseBlock.getType()).toString());
    }

    public static boolean isBreakingFallingBlock(Material material) {
        return material.isTransparent() && material != CompMaterial.NETHER_PORTAL.getMaterial() && material != CompMaterial.END_PORTAL.getMaterial() || material == CompMaterial.COBWEB.getMaterial() || material == Material.DAYLIGHT_DETECTOR || CompMaterial.isTrapDoor(material) || material == CompMaterial.SIGN.getMaterial() || CompMaterial.isWallSign(material) || SLAB_PATTERN.matcher(material.name()).matches();
    }

    public static boolean isTool(Material material) {
        return material.name().endsWith("AXE") || material.name().endsWith("SPADE") || material.name().endsWith("SWORD") || material.name().endsWith("HOE") || material.name().endsWith("BUCKET") || material == CompMaterial.BOW.getMaterial() || material == CompMaterial.FISHING_ROD.getMaterial() || material == CompMaterial.CLOCK.getMaterial() || material == CompMaterial.COMPASS.getMaterial() || material == CompMaterial.FLINT_AND_STEEL.getMaterial();
    }

    public static boolean isArmor(Material material) {
        return material.name().endsWith("HELMET") || material.name().endsWith("CHESTPLATE") || material.name().endsWith("LEGGINGS") || material.name().endsWith("BOOTS");
    }

    public static boolean isForBlockSelection(Material material) {
        if (material.isBlock() && material != Material.AIR) {
            try {
                if (material.isInteractable()) {
                    return false;
                }
            } catch (Throwable var3) {
            }

            try {
                if (material.hasGravity()) {
                    return false;
                }
            } catch (Throwable var2) {
            }

            return material.isSolid();
        } else {
            return false;
        }
    }

    public static int findHighestBlockNoSnow(Location location) {
        return findHighestBlockNoSnow(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    public static int findHighestBlockNoSnow(World world, int x, int z) {
        for(int y = world.getMaxHeight(); y > 0; --y) {
            Block block = world.getBlockAt(x, y, z);
            if (block != null && !CompMaterial.isAir(block) && block.getType() != CompMaterial.SNOW.getMaterial()) {
                return y + 1;
            }
        }

        return -1;
    }

    public static int findHighestBlock(Location location, Predicate<Material> predicate) {
        return findHighestBlock(location.getWorld(), location.getBlockX(), location.getBlockZ(), predicate);
    }

    public static int findHighestBlock(World world, int x, int z, Predicate<Material> predicate) {
        for(int y = world.getMaxHeight(); y > 0; --y) {
            Block block = world.getBlockAt(x, y, z);
            if (block != null && !CompMaterial.isAir(block) && predicate.test(block.getType())) {
                return y + 1;
            }
        }

        return -1;
    }

    public static Location findClosestLocation(Location location, List<Location> locations) {
        List<Location> locations = new ArrayList(locations);
        Collections.sort(locations, (f, s) -> {
            return Double.compare(f.distance(location), s.distance(location));
        });
        return (Location)locations.get(0);
    }

    public static FallingBlock shootBlock(Block block, Vector velocity) {
        return shootBlock(block, velocity, 0.0D);
    }

    public static FallingBlock shootBlock(Block block, Vector velocity, double burnOnFallChance) {
        if (!canShootBlock(block)) {
            return null;
        } else {
            FallingBlock falling = Remain.spawnFallingBlock(block.getLocation(), block.getType());
            double x = MathUtil.range(velocity.getX(), -2.0D, 2.0D) * 0.5D;
            double y = Math.random();
            double z = MathUtil.range(velocity.getZ(), -2.0D, 2.0D) * 0.5D;
            falling.setVelocity(new Vector(x, y, z));
            if (RandomUtil.chanceD(burnOnFallChance) && block.getType().isBurnable()) {
                scheduleBurnOnFall(falling);
            }

            falling.setDropItem(false);
            block.setType(Material.AIR);
            return falling;
        }
    }

    private static boolean canShootBlock(Block block) {
        Material material = block.getType();
        return !CompMaterial.isAir(material) && (material.toString().contains("STEP") || material.toString().contains("SLAB") || isForBlockSelection(material));
    }

    private static void scheduleBurnOnFall(FallingBlock block) {
        EntityUtil.trackFalling(block, () -> {
            Block upperBlock = block.getLocation().getBlock().getRelative(BlockFace.UP);
            if (upperBlock.getType() == Material.AIR) {
                upperBlock.setType(Material.FIRE);
            }

        });
    }

    private static BlockUtil.VectorHelper getMinimumPoint(Location pos1, Location pos2) {
        return new BlockUtil.VectorHelper(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
    }

    private static BlockUtil.VectorHelper getMaximumPoint(Location pos1, Location pos2) {
        return new BlockUtil.VectorHelper(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
    }

    private static int getHeight(Location pos1, Location pos2) {
        BlockUtil.VectorHelper min = getMinimumPoint(pos1, pos2);
        BlockUtil.VectorHelper max = getMaximumPoint(pos1, pos2);
        return (int)(max.getY() - min.getY() + 1.0D);
    }

    private BlockUtil() {
    }

    static {
        TREE_TRUNK_FACES = new BlockFace[]{BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH};
        TREE_GROUND_BLOCKS = Sets.newHashSet(new String[]{"GRASS_BLOCK", "COARSE_DIRT", "DIRT", "MYCELIUM", "PODZOL"});
        BOUNDING_VERTICAL_GAP = 1.0D;
        BOUNDING_HORIZONTAL_GAP = 1.0D;
    }

    private static final class VectorHelper {
        protected final double x;
        protected final double y;
        protected final double z;

        public BlockUtil.VectorHelper add(BlockUtil.VectorHelper other) {
            return this.add(other.x, other.y, other.z);
        }

        public BlockUtil.VectorHelper add(double x, double y, double z) {
            return new BlockUtil.VectorHelper(this.x + x, this.y + y, this.z + z);
        }

        public BlockUtil.VectorHelper subtract(BlockUtil.VectorHelper other) {
            return this.subtract(other.x, other.y, other.z);
        }

        public BlockUtil.VectorHelper subtract(double x, double y, double z) {
            return new BlockUtil.VectorHelper(this.x - x, this.y - y, this.z - z);
        }

        public BlockUtil.VectorHelper multiply(double n) {
            return new BlockUtil.VectorHelper(this.x * n, this.y * n, this.z * n);
        }

        public BlockUtil.VectorHelper divide(double n) {
            return new BlockUtil.VectorHelper(this.x / n, this.y / n, this.z / n);
        }

        public double length() {
            return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        }

        public double distance(BlockUtil.VectorHelper other) {
            return Math.sqrt(Math.pow(other.x - this.x, 2.0D) + Math.pow(other.y - this.y, 2.0D) + Math.pow(other.z - this.z, 2.0D));
        }

        public BlockUtil.VectorHelper normalize() {
            return this.divide(this.length());
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof BlockUtil.VectorHelper)) {
                return false;
            } else {
                BlockUtil.VectorHelper other = (BlockUtil.VectorHelper)obj;
                return other.x == this.x && other.y == this.y && other.z == this.z;
            }
        }

        public String toString() {
            return "(" + this.x + ", " + this.y + ", " + this.z + ")";
        }

        public VectorHelper(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }
    }
}