package me.onebone.actaeon.entity.monster;

import cn.nukkit.Player;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.UpdateAttributesPacket;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import me.onebone.actaeon.entity.MovingEntity;

abstract public class Monster extends MovingEntity implements EntityAgeable {

	public Monster(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);
	}

	public float getDamage() {
		return 2;
	}

	@Override
	public boolean isBaby(){
		return this.getDataFlag(Entity.DATA_FLAG_BABY);
	}

	@Override
	public void spawnTo(Player player){
		if (this.hasSpawned.containsKey(player.getLoaderId())) {
			return;
		}

		AddEntityPacket pk = new AddEntityPacket();
		pk.type = this.getNetworkId();
		pk.entityUniqueId = this.getId();
		pk.entityRuntimeId = this.getId();
		pk.x = (float) this.x;
		pk.y = (float) this.y;
		pk.z = (float) this.z;
		pk.yaw = (float) this.yaw;
		pk.pitch = (float) this.pitch;
		pk.headYaw = (float) this.yaw;
		pk.speedX = (float) this.motionX;
		pk.speedY = (float) this.motionY;
		pk.speedZ = (float) this.motionZ;
		pk.metadata = this.dataProperties;
		Pair<Int2IntMap, Int2FloatMap> propertyValues = getProperties().getValues();
		if (propertyValues != null) {
			pk.intProperties = propertyValues.left();
			pk.floatProperties = propertyValues.right();
		}
		pk.attributes = new Attribute[]{
				Attribute.getAttribute(Attribute.HEALTH).setMaxValue(this.getMaxHealth()).setValue(this.getHealth()),
		};
		player.dataPacket(pk);

		UpdateAttributesPacket pk0 = new UpdateAttributesPacket();
		pk0.entityId = this.getId();
		pk0.entries = pk.attributes;
		player.dataPacket(pk0);

		super.spawnTo(player);
	}

	@Override
	public void onCollideWithPlayer(EntityHuman entityPlayer) {
		/*if (!entityPlayer.getDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE)) {
			Vector3 motion = this.subtract(entityPlayer);
			this.motionX += motion.x / 2;
			this.motionZ += motion.z / 2;
		}*/
	}
}
