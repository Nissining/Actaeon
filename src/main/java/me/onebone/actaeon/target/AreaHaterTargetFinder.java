package me.onebone.actaeon.target;

import cn.nukkit.Player;
import me.onebone.actaeon.entity.IMovingEntity;

/**
 * 仇恨
 */
public class AreaHaterTargetFinder extends TargetFinder {

    private final int radius;
    private boolean first;

	public AreaHaterTargetFinder(IMovingEntity entity, long interval, int radius){
		super(entity, interval);
        this.radius = radius;
	}

	protected void find() {
        Player near = null;
        double nearest = this.radius * this.radius;

        for (Player player: this.getEntity().getLevel().getPlayers().values()) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
                continue;
            }

            if (this.getEntity().distanceSquared(player) < nearest){
                near = player;
                nearest = this.getEntity().distance(player);
            }
        }

        if (near != null) {
            this.getEntity().setTarget(near, this.getEntity().getName(), first);
            this.getEntity().setHate(near);
        } else {
            //this.getEntity().getRoute().forceStop();
            this.getEntity().setTarget(null, this.getEntity().getName(), first);
        }
        this.first = false;
	}
}
