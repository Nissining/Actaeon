package me.onebone.actaeon.entity.animal;

import cn.nukkit.Player;
import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.UpdateAttributesPacket;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import me.onebone.actaeon.entity.MovingEntity;

abstract public class Animal extends MovingEntity implements EntityAgeable {
	public Animal(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);
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
}
