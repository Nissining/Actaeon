package me.onebone.actaeon.hook;

import cn.nukkit.entity.Entity;
import me.onebone.actaeon.entity.IMovingEntity;
import me.onebone.actaeon.task.AttackTask;
import me.onebone.actaeon.task.MovingEntityTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * AttackHook
 * ===============
 * author: boybook
 * ===============
 */
public class AttackHook extends MovingEntityHook {

    public interface AttackTaskSupplier {
        MovingEntityTask get(Entity target);
    }

    private final Entity parentEntity;
    private long lastAttack = 0;
    private final double attackDistanceSq;
    private long coolDown;
    private final int effectual;  //攻击成功率 0~10
    private final double viewAngle;  //机器人视野范围（攻击有效范围）
    private boolean jump;  //是否自动跳劈
    private Supplier<Float> damage;
    private final List<AttackTask.AttackCallback> callbacks = new ArrayList<>();
    private AttackTaskSupplier attackTaskSupplier;

    public AttackHook(IMovingEntity entity) {
        this(entity, 2.6, () -> 2f, 250, 6, 75);
    }

    public AttackHook(IMovingEntity bot, double attackDistance, float damage, long coolDown, int effectual, double viewAngle) {
        this(bot, attackDistance, () -> damage, coolDown, effectual, viewAngle);
    }

    public AttackHook(IMovingEntity bot, double attackDistance, Supplier<Float> damage, long coolDown, int effectual, double viewAngle) {
        this(bot, null, attackDistance, damage, coolDown, effectual, viewAngle);
    }

    public AttackHook(IMovingEntity bot, Entity parentEntity, double attackDistance, float damage, long coolDown, int effectual, double viewAngle) {
        this(bot, parentEntity, attackDistance, () -> damage, coolDown, effectual, viewAngle);
    }

    public AttackHook(IMovingEntity bot, Entity parentEntity, double attackDistance, Supplier<Float> damage, long coolDown, int effectual, double viewAngle) {
        super(bot);
        this.parentEntity = parentEntity;
        this.attackDistanceSq = attackDistance * attackDistance;
        this.damage = damage;
        this.coolDown = coolDown;
        this.effectual = effectual;
        this.viewAngle = viewAngle;
        this.attackTaskSupplier = (target) -> new AttackTask(this.entity, this.parentEntity, target, this.damage.get(), this.viewAngle, ThreadLocalRandom.current().nextInt(10) < this.effectual, this.callbacks);
    }

    public AttackHook setAttackTaskSupplier(AttackTaskSupplier attackTaskSupplier) {
        this.attackTaskSupplier = attackTaskSupplier;
        return this;
    }

    public float getDamage() {
        return damage.get();
    }

    public void setDamage(float damage) {
        this.damage = () -> damage;
    }

    public void setDamage(Supplier<Float> damage) {
        this.damage = damage;
    }

    public long getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(long coolDown) {
        this.coolDown = coolDown;
    }

    public long getLastAttack() {
        return lastAttack;
    }

    public AttackHook setLastAttack(long lastAttack) {
        this.lastAttack = lastAttack;
        return this;
    }

    public boolean canJump() {
        return jump;
    }

    public AttackHook setJump(boolean jump) {
        this.jump = jump;
        return this;
    }

    public AttackHook addAttackCallback(AttackTask.AttackCallback callback) {
        this.callbacks.add(callback);
        return this;
    }

    @Override
    public void onUpdate(int tick) {
        if (this.entity.getHate() != null) {
            Entity hate = this.entity.getHate();
            if (this.entity.distanceSquared(hate) <= this.attackDistanceSq) {
                if (System.currentTimeMillis() - this.lastAttack > this.coolDown) {
                    if (this.entity.getTask() == null) {
                        this.entity.updateBotTask(this.attackTaskSupplier.get(hate));
                    }
                    this.lastAttack = System.currentTimeMillis();
                    if (this.jump && ThreadLocalRandom.current().nextBoolean()) this.entity.jump();
                }
            }
        }
    }
}
