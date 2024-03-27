package me.onebone.actaeon.entity.animal;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityID;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.actaeon.entity.Fallable;
import me.onebone.actaeon.target.AreaPlayerHoldTargetFinder;

import java.util.concurrent.ThreadLocalRandom;

public class Chicken extends Animal implements EntityAgeable, Fallable{
	public static final int NETWORK_ID = EntityID.CHICKEN;

	public Chicken(FullChunk chunk, CompoundTag nbt) {
		super(chunk, nbt);
		this.setTargetFinder(new AreaPlayerHoldTargetFinder(this, 500, Item.get(Item.WHEAT), 100));
	}

	@Override
	public float getWidth(){
		return 0.6f;
	}

	@Override
	protected float getGravity() {
		return 0.05f;
	}

	@Override
	public float getHeight() {
		return 0.8f;
	}

	@Override
	public float getEyeHeight(){
		if (isBaby()){
			return 0.51f;
		}
		return 0.7f;
	}

	@Override
	protected double getStepHeight() {
		return 1;
	}

	@Override
	public Vector3f getMountedOffset(Entity entity) {
		return new Vector3f(0, 0.4f + entity.getRidingOffset(), 0);
	}

	@Override
	public Item[] getDrops(){
		return new Item[]{
				Item.get(isOnFire() ? Item.COOKED_CHICKEN : Item.CHICKEN),
				Item.get(Item.FEATHER, 0, ThreadLocalRandom.current().nextInt(3)),
		};
	}

	@Override
	public int getNetworkId(){
		return NETWORK_ID;
	}

	@Override
	protected void initEntity(){
		super.initEntity();
		setMaxHealth(4);
	}
}
