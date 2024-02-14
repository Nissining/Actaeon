package me.onebone.actaeon.entity.monster.evoker;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.level.sound.SoundEnum;
import cn.nukkit.math.Mth;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import me.onebone.actaeon.entity.IMovingEntity;
import me.onebone.actaeon.task.MovingEntityTask;

import java.awt.*;

/**
 * AttackTask
 * ===============
 * author: boybook
 * ===============
 */
public class EvokerAttackTask extends MovingEntityTask {

    private final Vector3 target;
    private final float damage;
    private boolean lineMode;
    private int index = 1;

    public EvokerAttackTask(IMovingEntity entity, Entity target, float damage) {
        super(entity);
        this.target = target.getPosition();
        this.damage = damage;
        entity.getEntity().setMovementSpeed(0);
        this.getEntity().getEntity().setDataFlag(Entity.DATA_FLAG_EVOKER_SPELL, true);
        this.getEntity().getEntity().setDataProperty(new IntEntityData(Entity.DATA_SPELL_CASTING_COLOR, new Color(97, 76, 86).getRGB()));
        //如果目标离唤魔者的距离少于3格，唤魔者会以自身为中心召唤两圈尖牙。
        this.lineMode = this.getEntity().getEntity().distance(this.target) > 3;
        this.getEntity().getLevel().addSound(this.getEntity().getEntity(), SoundEnum.MOB_EVOCATION_ILLAGER_PREPARE_ATTACK);
    }

    public EvokerAttackTask setLineMode(boolean lineMode) {
        this.lineMode = lineMode;
        return this;
    }

    public Vector3 getTarget() {
        return target;
    }

    @Override
    public void onUpdate(int tick) {
        if (this.lineMode) {
            if (index < 16) {
                // 16个尖牙，延伸20格，所以每一格的距离是20/16=1.25
                Vector2 v2 = new Vector2(this.target.x - this.entity.getX(), this.target.z - this.entity.getZ());
                v2 = v2.normalize().multiply(1.25);

                double x = this.getEntity().getX() + v2.multiply(index + 1).getX();
                double z = this.getEntity().getZ() + v2.multiply(index + 1).getY();
                double y = this.getEntity().getY();
                for (int offset = 0; offset < 5; offset++) {
                    if (this.getEntity().getLevel().getBlock(new Vector3(x, y + offset, z), false).isAir()) {
                        y += offset;
                        break;
                    }
                }
                CompoundTag nbt = new CompoundTag()
                        .putList(new ListTag<DoubleTag>("Pos")
                                .add(new DoubleTag("", x))
                                .add(new DoubleTag("", y))
                                .add(new DoubleTag("", z)))
                        .putList(new ListTag<DoubleTag>("Motion")
                                .add(new DoubleTag("", 0))
                                .add(new DoubleTag("", 0))
                                .add(new DoubleTag("", 0)))
                        .putList(new ListTag<FloatTag>("Rotation")
                                .add(new FloatTag("", (float) 0))
                                .add(new FloatTag("", (float) 0)))
                        .putFloat("Damage", this.damage);
                EntityEvocationFang fang = new EntityEvocationFang(this.getEntity().getLevel().getChunk((int)x >> 4, (int)z >> 4), nbt, this.getEntity().getEntity());
                fang.spawnToAll();
            }
        } else {
            if (index == 20) {
                // 内圈5个尖牙，距离1，环绕成一圈
                for (int i = 0; i < 5; i++) {
                    double angle = 2 * Math.PI / 5 * i;
                    double x = this.getEntity().getX() + Mth.cos(angle);
                    double z = this.getEntity().getZ() + Mth.sin(angle);
                    double y = this.getEntity().getY();
                    for (int offset = 0; offset < 5; offset++) {
                        if (this.getEntity().getLevel().getBlock(new Vector3(x, y + offset, z), false).isAir()) {
                            y += offset;
                            break;
                        }
                    }
                    CompoundTag nbt = new CompoundTag()
                            .putList(new ListTag<DoubleTag>("Pos")
                                    .add(new DoubleTag("", x))
                                    .add(new DoubleTag("", y))
                                    .add(new DoubleTag("", z)))
                            .putList(new ListTag<DoubleTag>("Motion")
                                    .add(new DoubleTag("", 0))
                                    .add(new DoubleTag("", 0))
                                    .add(new DoubleTag("", 0)))
                            .putList(new ListTag<FloatTag>("Rotation")
                                    .add(new FloatTag("", (float) 0))
                                    .add(new FloatTag("", (float) 0)))
                            .putFloat("Damage", this.damage);
                    EntityEvocationFang fang = new EntityEvocationFang(this.getEntity().getLevel().getChunk((int)x >> 4, (int)z >> 4), nbt, this.getEntity().getEntity());
                    fang.spawnToAll();
                }
            } else if (index == 23) {
                // 外圈为8个尖牙，距离2，环绕成一圈
                for (int i = 0; i < 8; i++) {
                    double angle = 2 * Math.PI / 8 * i;
                    double x = this.getEntity().getX() + 2 * Mth.cos(angle);
                    double z = this.getEntity().getZ() + 2 * Mth.sin(angle);
                    double y = this.getEntity().getY();
                    for (int offset = 0; offset < 5; offset++) {
                        if (this.getEntity().getLevel().getBlock(new Vector3(x, y + offset, z), false).isAir()) {
                            y += offset;
                            break;
                        }
                    }
                    CompoundTag nbt = new CompoundTag()
                            .putList(new ListTag<DoubleTag>("Pos")
                                    .add(new DoubleTag("", x))
                                    .add(new DoubleTag("", y))
                                    .add(new DoubleTag("", z)))
                            .putList(new ListTag<DoubleTag>("Motion")
                                    .add(new DoubleTag("", 0))
                                    .add(new DoubleTag("", 0))
                                    .add(new DoubleTag("", 0)))
                            .putList(new ListTag<FloatTag>("Rotation")
                                    .add(new FloatTag("", (float) 0))
                                    .add(new FloatTag("", (float) 0)))
                            .putFloat("Damage", this.damage);
                    EntityEvocationFang fang = new EntityEvocationFang(this.getEntity().getLevel().getChunk((int)x >> 4, (int)z >> 4), nbt, this.getEntity().getEntity());
                    fang.spawnToAll();
                }
            }
        }
        if (index++ > 50) {
            this.entity.updateBotTask(null);
        }
    }

    @Override
    public void forceStop() {
        entity.getEntity().setMovementSpeed(0.1f);
        this.getEntity().getEntity().setDataFlag(Entity.DATA_FLAG_EVOKER_SPELL, false);
    }

}
