package me.onebone.actaeon.route;

import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.math.Mth;
import cn.nukkit.math.Vector3;

import java.util.*;

public class WalkableIterator implements Iterator<Block> {

    private final Level level;
    private final int maxDistance;
    private final double width;

    private boolean end = false;

    private Block currentBlockObject;

    private double currentDistance;
    private final Vector3 startPosition;
    private Vector3 startPositionLeft;
    private Vector3 startPositionRight;
    private Vector3 currentPosition = null;
    private Vector3 currentPositionLeft = null;
    private Vector3 currentPositionRight = null;
    private Vector3 direction = null;

    private AdvancedRouteFinder advancedRouteFinder;
    private final Entity entity;

    public WalkableIterator(AdvancedRouteFinder advancedRouteFinder, Entity entity, Level level, Vector3 start, Vector3 direction, double width, int maxDistance) {
        this.advancedRouteFinder = advancedRouteFinder;
        this.entity = entity;
        this.level = level;
        this.width = width;
        this.maxDistance = maxDistance == 0 ? 120 : maxDistance;
        this.currentDistance = 0;
        this.direction = direction.normalize();
        this.currentPosition = start;
        //this.currentPositionLeft = this.getStartPos(start, -width / 2);
        //this.currentPositionRight = this.getStartPos(start, width / 2);
        this.startPosition = this.currentPosition.clone();
        //this.startPositionLeft = this.currentPositionLeft.clone();
        //this.startPositionRight = this.currentPositionRight.clone();
    }

    private Vector3 getStartPos(Vector3 center, double xOffset) {
        double angle = Mth.atan2(this.direction.z, this.direction.x);
        double yaw = (float) ((angle * 180) / Math.PI) - 90;
        double baseX = xOffset;
        double baseZ = 0;
        double a = Math.toRadians(yaw);
        double x = baseX * Mth.cos(a) - baseZ * Mth.sin(a);
        double z = baseX * Mth.sin(a) + baseZ * Mth.cos(a);
        return new Vector3(center.x + x, center.y, center.z + z);
    }

    @Override
    public Block next() {
        return this.currentBlockObject;
    }

    @Override
    public boolean hasNext() {
        this.scan();
        return this.currentBlockObject != null;
    }

    private void scan() {
        if (this.maxDistance != 0 && this.currentDistance > this.maxDistance) {
            this.end = true;
            return;
        }
        if (this.end) return;

        List<Vector3> checked = new ArrayList<>();
        do {
            //Middle
            Vector3 check;
            Vector3 next = this.currentPosition.add(this.direction);
            if (!checked.contains(check = this.currentPosition.floor())) {
                Block block = this.level.getBlock(check, false);
                double walkable = AdvancedRouteFinder.isWalkableAt(entity, block);
                //Server.getInstance().getLogger().info(block.getLocation().toString() + " LEFT walkable=" + walkable);
                if (walkable < 0) {
                    this.currentBlockObject = block;
                    this.end = true;
                }
                checked.add(check);
            }
            this.currentPosition = next;
            /*
            //Left
            if (!checked.contains(check = this.currentPositionLeft.floor())) {
                Block block = this.level.getBlock(check, false);
                double walkable = this.advancedRouteFinder.isWalkableAt(block);
                //Server.getInstance().getLogger().info(block.getLocation().toString() + " LEFT walkable=" + walkable);
                if (walkable < 0) {
                    this.currentBlockObject = block;
                    this.end = true;
                }
                checked.add(check);
            }
            this.currentPositionLeft = next;

            //Right
            next = this.currentPositionRight.add(this.direction);
            if (!checked.contains(check = this.currentPositionRight.floor())) {
                Block block = this.level.getBlock(check, false);
                double walkable = this.advancedRouteFinder.isWalkableAt(block);
                //Server.getInstance().getLogger().info(block.getLocation().toString() + " RIGHT walkable=" + walkable);
                if (walkable < 0) {
                    this.currentBlockObject = block;
                    this.end = true;
                }
                checked.add(check);
            }
            this.currentPositionRight = next;*/

            this.currentDistance = this.currentPosition.distance(this.startPosition);
            if (this.maxDistance > 0 && this.currentDistance > this.maxDistance) this.end = true;
        } while (!this.end);
    }

}
