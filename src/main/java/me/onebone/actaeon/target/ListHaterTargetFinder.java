package me.onebone.actaeon.target;

import cn.nukkit.entity.Entity;
import cn.nukkit.math.Vector3;
import me.onebone.actaeon.entity.IMovingEntity;

import java.util.List;

public class ListHaterTargetFinder extends TargetFinder {

    private List<? extends Entity> list;
    private boolean first = true;
    private double keepDistance = 0;  //为0时为关闭，>0时，自动选取与玩家保持距离的最佳坐标
    private double maxDistance;

    public ListHaterTargetFinder(IMovingEntity entity, long interval, List<? extends Entity> list){
        this(entity, interval, list, 100000);
    }

	public ListHaterTargetFinder(IMovingEntity entity, long interval, List<? extends Entity> list, double maxDistance){
		super(entity, interval);
        this.list = list;
        this.maxDistance = maxDistance;
	}

    public ListHaterTargetFinder setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    public void setList(List<? extends Entity> list) {
        this.list = list;
    }

    public double getKeepDistance() {
        return keepDistance;
    }

    public void setKeepDistance(double keepDistance) {
        this.keepDistance = keepDistance;
    }

    protected void find() {
        Entity near = null;

        double nearest = maxDistance;

        for (Entity entity: this.list) {
            if (this.getEntity() != entity && this.getEntity().distanceSquared(entity) < nearest * nearest){
                near = entity;
                nearest = this.getEntity().distance(entity);
            }
        }

        if (near != null) {
            this.getEntity().setHate(near);
            if (this.getKeepDistance() <= 0) {
                this.getEntity().setTarget(near.getPosition(), this.getEntity().getName(), this.first);
            } else {
                double angle = Math.atan2(this.getEntity().getZ() - near.z, this.getEntity().getX() - near.x);
                double yaw = (float) ((angle * 180) / Math.PI) - 90;
                Vector3 target = this.getEntity().getPosition().add(this.getDirectionVector(yaw).multiply(this.getKeepDistance()));
                this.getEntity().setTarget(target, this.getEntity().getName(), this.first);
            }
            //Server.getInstance().getLogger().warning(this.getEntity().getName() + " 已设定仇恨：" + near.getName());
        } else {
            //this.getEntity().getRoute().forceStop();
            this.getEntity().setTarget(null, this.getEntity().getName());
        }
        this.first = false;
	}

    public Vector3 getDirectionVector(double yaw0) {
        double pitch = Math.PI / 2;
        double yaw = (yaw0 + 90) * Math.PI / 180;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double z = Math.sin(pitch) * Math.sin(yaw);
        double y = Math.cos(pitch);
        return (new Vector3(x, y, z)).normalize();
    }

}
