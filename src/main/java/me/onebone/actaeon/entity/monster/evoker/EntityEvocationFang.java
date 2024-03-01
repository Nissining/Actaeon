package me.onebone.actaeon.entity.monster.evoker;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityID;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.CriticalParticle;
import cn.nukkit.level.sound.SoundEnum;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * net.easecation.ecgrave.entity
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public class EntityEvocationFang extends Entity {

    public static final int NETWORK_ID = EntityID.EVOCATION_FANG;

    private final Entity owner;
    private final float damage;

    public EntityEvocationFang(FullChunk chunk, CompoundTag nbt, Entity owner) {
        super(chunk, nbt);
        this.owner = owner;
        this.setDataProperty(new IntEntityData(Entity.DATA_LIMITED_LIFE, 40), false);
        if (nbt.contains("Damage")) {
            this.damage = nbt.getFloat("Damage");
        } else {
            this.damage = 6f;
        }
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void spawnTo(Player player) {
        if (this.hasSpawned.containsKey(player.getLoaderId())) return;
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
        player.dataPacket(pk);
        super.spawnTo(player);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return false; //无敌
    }

    @Override
    public boolean onUpdate(int currentTick) {
        super.onUpdate(currentTick);
        if (this.isAlive()) {
            if (this.age == this.getDataPropertyInt(Entity.DATA_LIMITED_LIFE) - 20) {
                this.getLevel().addSound(this, SoundEnum.MOB_EVOCATION_FANGS_ATTACK);
            } else if (this.age >= this.getDataPropertyInt(Entity.DATA_LIMITED_LIFE) - 10) {
                Entity[] entities = this.getLevel().getNearbyEntities(this.getBoundingBox().grow(0.6, 2, 0.6));
                for (Entity entity: entities) {
                    if (!entity.isAlive()) {
                        continue;
                    }

                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (player.isCreativeLike()) {
                            continue;
                        }
                    }

                    EntityDamageByChildEntityEvent event = new EntityDamageByChildEntityEvent(this.owner, this, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, this.damage);
                    event.setKnockBack(0);
                    if (entity != this.owner) {
                        entity.attack(event);
                    }
                }
                this.getLevel().addParticleEffect(this.add(0, 2, 0), ParticleEffect.EVOCATION_FANG);
                Random random = ThreadLocalRandom.current();
                for (int i = 0; i < 5; i++) {
                    this.getLevel().addParticle(new CriticalParticle(this.add(random.nextFloat() - 0.5f, 1 + random.nextFloat() * 1.2f, random.nextFloat() - 0.5f)));
                }
                this.kill();
            }
        }

        return this.isAlive();
    }
}
