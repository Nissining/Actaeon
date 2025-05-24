package me.onebone.actaeon.target;

import cn.nukkit.math.Vector3;
import me.onebone.actaeon.entity.IMovingEntity;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 逃跑
 */
public class PanicTargetFinder extends TargetFinder {

	private final double range;

	public PanicTargetFinder(IMovingEntity entity, double range) {
		super(entity, 1000 * (ThreadLocalRandom.current().nextInt(1) + 1));
		this.range = range;
	}

	@Override
	protected void find() {
		Random random = ThreadLocalRandom.current();
		this.entity.getEntity().setSprinting(true);
		this.entity.setTarget(new Vector3(this.entity.getX() + random.nextFloat() * range * 2 - range, this.entity.getY(), this.entity.getZ() + random.nextFloat() * range * 2 - range), "Panic", true);
	}

}
