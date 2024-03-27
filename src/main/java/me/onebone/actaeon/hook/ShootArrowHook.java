package me.onebone.actaeon.hook;

import cn.nukkit.Difficulty;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.math.Mth;
import cn.nukkit.math.Vector3;
import me.onebone.actaeon.entity.IMovingEntity;
import me.onebone.actaeon.task.ShootArrowTask;

/**
 * ECPlayerBotAttackHook
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public class ShootArrowHook extends MovingEntityHook {

    public interface ShootArrowTaskSupplier {
        ShootArrowTask get(IMovingEntity entity, Entity target, int ticks, double pitch, double pow, double uncertainty);

        static ShootArrowTaskSupplier ofDefault() {
            return ShootArrowTask::new;
        }
    }

    private Entity target;
    private int nextAttackTick;

    private final double minDistanceSq;
    private final double maxDistanceSq;
    private int coolDownTicks;
    private final int minAimTicks;
    private final int maxAimTicks;
    private final double pitch;
    private ShootArrowTaskSupplier shootArrowTaskSupplier = ShootArrowTaskSupplier.ofDefault();

    private double pow = 1.6;
    private double uncertainty = 1;

    public ShootArrowHook(IMovingEntity entity) {
        this(entity, 2, 15, 0);
    }

    public ShootArrowHook(IMovingEntity entity, double minDistance, double maxDistance, int coolDownTicks) {
        this(entity, minDistance, maxDistance, coolDownTicks, 20);
    }

    public ShootArrowHook(IMovingEntity entity, double minDistance, double maxDistance, int coolDownTicks, int minAimTicks) {
        this(entity, minDistance, maxDistance, coolDownTicks, minAimTicks, 20 * 3);
    }

    public ShootArrowHook(IMovingEntity entity, double minDistance, double maxDistance, int coolDownTicks, int minAimTicks, int maxAimTicks) {
        this(entity, minDistance, maxDistance, coolDownTicks, minAimTicks, maxAimTicks, 5);
    }

    public ShootArrowHook(IMovingEntity entity, double minDistance, double maxDistance, int coolDownTicks, int minAimTicks, int maxAimTicks, double pitch) {
        super(entity);
        this.minDistanceSq = minDistance * minDistance;
        this.maxDistanceSq = maxDistance * maxDistance;
        this.coolDownTicks = coolDownTicks;
        this.minAimTicks = minAimTicks;
        this.maxAimTicks = maxAimTicks;
        this.pitch = pitch;
    }

    public ShootArrowHook setShootArrowTaskSupplier(ShootArrowTaskSupplier shootArrowTaskSupplier) {
        this.shootArrowTaskSupplier = shootArrowTaskSupplier;
        return this;
    }

    public int getCoolDownTicks() {
        return coolDownTicks;
    }

    public ShootArrowHook setCoolDownTicks(int coolDownTicks) {
        this.coolDownTicks = coolDownTicks;
        return this;
    }

    public long getNextAttackTick() {
        return nextAttackTick;
    }

    public ShootArrowHook setNextAttackTick(int nextAttackTick) {
        this.nextAttackTick = nextAttackTick;
        return this;
    }

    @Override
    public void onUpdate(int tick) {
        Entity hate = this.entity.getHate();
        if (hate != null) {
            double distanceSq = this.entity.distanceSquared(hate);
            if (distanceSq <= this.maxDistanceSq) {
                entity.setLookAtFront(false);
                Vector3 eyePos = (target != null ? target : hate).getEyePosition();
                entity.getEntity().lookAt(eyePos);
                entity.getRouter().setIgnoreStopDistance(false);

                if (distanceSq < this.minDistanceSq) {
                    return;
                }

                int now = entity.getEntity().getServer().getTick();
                if (now >= this.nextAttackTick) {
                    try {
                        Block[] blocks = this.entity.getEntity().getLineOfSight(Mth.floor(this.entity.distance(eyePos)));
                        for (Block block : blocks) {
                            if (!block.is(Block.BARRIER) && block.isSolid()) {
                                // 无法直接看到目标
                                entity.setLookAtFront(true);
                                entity.getRouter().setIgnoreStopDistance(true);
                                return;
                            }
                        }

                        if (this.entity.getTask() == null) {
                            target = hate;
                            this.entity.updateBotTask(this.shootArrowTaskSupplier.get(this.entity, hate, (int) Mth.lerp(distanceSq / maxDistanceSq, minAimTicks, maxAimTicks), this.pitch, pow, uncertainty)
                                    .setCallback(() -> target = null));
                        }
                        this.nextAttackTick = now + coolDownTicks;
                    } catch (Exception e) {
                        this.nextAttackTick = now + coolDownTicks;
                    }
                }
            } else {
                entity.setLookAtFront(true);
                entity.getRouter().setIgnoreStopDistance(true);
            }
        }
    }

    public double getPow() {
        return pow;
    }

    public ShootArrowHook setPow(double pow) {
        this.pow = pow;
        return this;
    }

    public double getUncertainty() {
        return uncertainty;
    }

    public ShootArrowHook setUncertainty(double uncertainty) {
        this.uncertainty = uncertainty;
        return this;
    }

    public ShootArrowHook setUncertainty(Difficulty difficulty) {
        setUncertainty(getDifficultyUncertainty(difficulty));
        return this;
    }

    public static double getDifficultyUncertainty(Difficulty difficulty) {
        return 16 - difficulty.ordinal() * 4;
    }
}
