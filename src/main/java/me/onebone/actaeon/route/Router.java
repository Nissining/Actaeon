package me.onebone.actaeon.route;

import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import me.onebone.actaeon.entity.IMovingEntity;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Router implements Iterator<Node> {

    private IRouteFinder routeFinder = new AdvancedRouteFinder();  // 默认使用Simple寻路算法
    private int current = 0;
    // 目的地
    protected Position destination = null;
    protected Position lastDestination = null;
    private boolean arrived = true;
    protected ObjectArrayList<Node> nodes = new ObjectArrayList<>();
    private double stopDistanceSq;
    private boolean ignoreStopDistance;

    protected IMovingEntity entity;
    public long nextRouteFind = System.currentTimeMillis();

    @Getter
    @Setter
    private boolean isSearching = false;

    /**
     * 正在执行中的任务，如果已经执行完了，那么应该为null
     */
//	private List<Node> promise = null;
    public Router(IMovingEntity entity) {
        if (entity == null) throw new IllegalArgumentException("Entity cannot be null");
        this.entity = entity;
    }

    public void setDestination(Position destination) {
        this.setDestination(destination, false);
    }

    public void setDestination(Position destination, boolean immediate) {
        this.destination = destination;
        if (immediate) {
            // 强制立即开始寻路
            this.lastDestination = null;
            this.nextRouteFind = System.currentTimeMillis();
        }
    }

    public void onTick() {
        if (!this.isSearching() && System.currentTimeMillis() >= this.nextRouteFind && this.routeFinder != null) {
            if (destination == null || entity.getEntity().isImmobile() || needStop())
                return;

            // 目的地没有变更，则不需要再次寻路
            if (Position.fromObject(destination, destination.level).equals(lastDestination)) return;

            this.nextRouteFind = System.currentTimeMillis() + routeFinder.getRouteFindCooldown();
            this.lastDestination = Position.fromObject(destination, destination.level);

            this.isSearching = true;
            val result = routeFinder.search(entity.getEntity(), entity.getPosition(), destination);
            if (result == null) {
                this.isSearching = false;
                // 寻路失败
                this.arrived();
                return;
            }
            this.isSearching = false;
            this.nodes = result;
            this.arrived = false;
            this.current = 0;
        }
    }

    public IMovingEntity getEntity() {
        return this.entity;
    }

    public IRouteFinder getRouteFinder() {
        return routeFinder;
    }

    public Router setRouteFinder(IRouteFinder routeFinder) {
        this.routeFinder = routeFinder;
        return this;
    }

    public Router setStopDistance(double distance) {
        this.stopDistanceSq = distance * distance;
        return this;
    }

    /**
     * @return true if it has next node to go
     */
    @Override
    public boolean hasNext() {
        if (nodes.isEmpty()) throw new IllegalStateException("There is no path found");

        return !this.arrived && nodes.size() > this.current + 1;
    }

    /**
     * Move to next node
     *
     * @return true if succeed
     */
    @Override
    public Node next() {
        if (nodes.isEmpty()) {
            throw new IllegalStateException("There is no path found");
        }

        if (this.hasNext()) {
            this.current++;
            return this.nodes.get(this.current);
        }
        return null;
    }

    /**
     * Returns if the entity has reached the node
     *
     * @return true if reached
     */
    public boolean hasReachedNode(Vector3 vec) {
        Vector3 cur = this.get().getVector3();
        return vec.x == cur.x
                //&& vec.y == cur.y
                && vec.z == cur.z;
    }

    /**
     * Gets node of current
     *
     * @return current node
     */
    public Node get() {
        if (nodes.isEmpty()) throw new IllegalStateException("There is no path found.");
        if (this.arrived) return null;
        if (this.current >= this.nodes.size()) {
            return null;
        }
        return nodes.get(current);
    }

    public void arrived() {
        this.current = 0;
        this.arrived = true;
    }

    public boolean hasRoute() {
        return !this.nodes.isEmpty();
    }

    public boolean needStop() {
        return !ignoreStopDistance && destination != null && stopDistanceSq > 0 && entity.distanceSquared(destination) <= stopDistanceSq;
    }

}
