package me.onebone.actaeon.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Raycaster {

    private final Level level;
    private final Vector3 start;
    private final Vector3 end;
    private final Vector3 direction;
    private final double maxDistance;
    private final int maxBlockCollide;
    private final boolean liquid;
    private final boolean ignoreBarrier;

    public Raycaster(Level level, Vector3 start, Vector3 end) {
        this(level, start, end, 1, false, false);
    }

    public Raycaster(Level level, Vector3 start, Vector3 end, int maxBlockCollide, boolean liquid, boolean ignoreBarrier) {
        this.level = level;
        this.start = start.clone();
        this.end = end;
        this.direction = end.subtract(start).normalize();
        this.maxDistance = end.distance(start);
        this.liquid = liquid;
        this.ignoreBarrier = ignoreBarrier;
        this.maxBlockCollide = maxBlockCollide;
    }

    public Level getLevel() {
        return level;
    }

    public Vector3 getStart() {
        return start;
    }

    public Vector3 getDirection() {
        return direction;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public boolean isIgnoreBarrier() {
        return ignoreBarrier;
    }

    public List<RayHitTarget> raytraceEntities() {
        AxisAlignedBB aabb = new SimpleAxisAlignedBB(start, end);
        Entity[] entities = level.getNearbyEntities(aabb);

        List<RayHitTarget> targets = new ArrayList<>();
        for (Entity entity : entities) {
            AxisAlignedBB entityBoundingBox = entity.getBoundingBox();
            MovingObjectPosition hitResult = entityBoundingBox.calculateIntercept(start, end);
            if (hitResult != null) {
                hitResult.typeOfHit = 0;
                hitResult.entityHit = entity;
                double distanceSq = start.distanceSquared(hitResult.hitVector);
                targets.add(new RayHitTarget(this, hitResult, distanceSq));
            }
        }

        return targets;
    }

    public List<RayHitTarget> raytraceBlocks() {
        List<RayHitTarget> targets = new ArrayList<>();
        BlockClip.BlockClipIterator iterator = BlockClip.clip(level, start, end, liquid, ignoreBarrier, (int) (maxDistance * 3));
        while (iterator.hasNext() && targets.size() < maxBlockCollide) {
            MovingObjectPosition hitResult = iterator.next();
            double distanceSq = start.distanceSquared(hitResult.hitVector);
            targets.add(new RayHitTarget(this, hitResult, distanceSq));
        }
        return targets;
    }

    public List<RayHitTarget> raytraceAll() {
        List<RayHitTarget> blockHits = this.raytraceBlocks();
        List<RayHitTarget> entityHits = this.raytraceEntities();
        return Stream.concat(blockHits.stream(), entityHits.stream()).sorted().collect(Collectors.toCollection(ArrayList::new));
    }

    public record RayHitTarget(
            Raycaster raycaster,
            MovingObjectPosition target,
            double distanceSq
    ) implements Comparable<RayHitTarget> {
        @Override
        public int compareTo(RayHitTarget that) {
            if (this.distanceSq == that.distanceSq) return 0;
            return (this.distanceSq < that.distanceSq ? -1 : 1);
        }
    }
}
