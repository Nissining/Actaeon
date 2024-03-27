package me.onebone.actaeon.entity.monster.evoker;

import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityID;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.actaeon.entity.Climbable;
import me.onebone.actaeon.entity.Fallable;
import me.onebone.actaeon.entity.monster.Monster;
import me.onebone.actaeon.target.AreaHaterTargetFinder;

public class EntityEvoker extends Monster implements EntityAgeable, Fallable, Climbable {
	public static final int NETWORK_ID = EntityID.EVOCATION_ILLAGER;

	public EntityEvoker(FullChunk chunk, CompoundTag nbt) {
		super(chunk, nbt);
        this.setTargetFinder(new AreaHaterTargetFinder(this, 500, 20));
        this.getRouter().setStopDistance(10);
        registerHooks();
	}

    protected void registerHooks() {
        this.addHook("attack", new EvokerAttackHook(this));
	}

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @Override
    protected double getStepHeight() {
        return 1;
    }

    @Override
    public float getRidingOffset() {
        return -0.5f;
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{
                Item.get(Item.TOTEM_OF_UNDYING),
        };
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (source instanceof EntityDamageByChildEntityEvent && ((EntityDamageByChildEntityEvent) source).getChild() instanceof EntityEvocationFang) {
            return false;
        }
        return super.attack(source);
    }
}
