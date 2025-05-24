package me.onebone.actaeon.entity.monster;

import cn.nukkit.Player;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.UpdateAttributesPacket;
import me.onebone.actaeon.entity.MovingEntity;

abstract public class Monster extends MovingEntity {

	public Monster(FullChunk chunk, CompoundTag nbt){
		super(chunk, nbt);
	}

	@Override
	public String getName() {
		return "Monster";
	}

	public float getDamage() {
		return 2;
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

		player.dataPacket(pk);

		UpdateAttributesPacket pk0 = new UpdateAttributesPacket();
		pk0.entityId = this.getId();
		pk0.entries = pk.attributes;
		player.dataPacket(pk0);

		super.spawnTo(player);
	}

}
