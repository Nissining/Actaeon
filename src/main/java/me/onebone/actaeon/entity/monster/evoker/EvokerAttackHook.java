package me.onebone.actaeon.entity.monster.evoker;

import cn.nukkit.entity.Entity;
import me.onebone.actaeon.entity.MovingEntity;
import me.onebone.actaeon.hook.MovingEntityHook;

/**
 * AttackHook
 * ===============
 * author: boybook
 * ===============
 */
public class EvokerAttackHook extends MovingEntityHook {
    private Entity target;
    private int nextAttackTick;

    private final double attackDistanceSq;
    private int coolDownTicks;
    private float damage;
    private float knockBack = Float.NaN;

    public EvokerAttackHook(MovingEntity entity) {
        this(entity, 20, 6, 20 * 5);
    }

    public EvokerAttackHook(MovingEntity bot, double attackDistance, float damage, int coolDownTicks) {
        super(bot);
        this.attackDistanceSq = attackDistance * attackDistance;
        this.damage = damage;
        this.coolDownTicks = coolDownTicks;
    }

    public EvokerAttackHook setKnockBack(float knockBack) {
        this.knockBack = knockBack;
        return this;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public int getCoolDownTicks() {
        return coolDownTicks;
    }

    public EvokerAttackHook setCoolDownTicks(int coolDownTicks) {
        this.coolDownTicks = coolDownTicks;
        return this;
    }

    public long getNextAttackTick() {
        return nextAttackTick;
    }

    public EvokerAttackHook setNextAttackTick(int nextAttackTick) {
        this.nextAttackTick = nextAttackTick;
        return this;
    }

    @Override
    public void onUpdate(int tick) {
        if (this.entity.getHate() != null) {
            Entity hate = this.entity.getHate();
            if (this.entity.distanceSquared(hate) <= this.attackDistanceSq) {
                entity.setLookAtFront(false);
                entity.getEntity().lookAt((target != null ? target : hate).getEyePosition());

                int now = entity.getEntity().getServer().getTick();
                if (now >= this.nextAttackTick) {
                    if (this.entity.getTask() == null) {
                        target = hate;
                        this.entity.updateBotTask(new EvokerAttackTask(this.entity, hate, this.damage) {
                            @Override
                            public void forceStop() {
                                super.forceStop();
                                target = null;
                            }
                        }.setKnockBack(knockBack));
                    }
                    this.nextAttackTick = now + coolDownTicks + 20 * 2;
                }
            } else {
                entity.setLookAtFront(true);
            }
        }
    }
}
