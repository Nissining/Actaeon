package me.onebone.actaeon.task;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.math.Mth;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.EntityEventPacket;
import me.onebone.actaeon.entity.IMovingEntity;
import me.onebone.actaeon.utils.Raycaster;

import java.util.ArrayList;
import java.util.List;

/**
 * AttackTask（Builder模式版）
 * ===============
 * author: boybook
 * ===============
 */
public class AttackTask extends MovingEntityTask {

    public static Builder builder(IMovingEntity entity) {
        return new Builder().entity(entity);
    }

    private final Entity parentEntity;
    private final Entity target;
    private final float damage;
    private final double viewAngle;
    private final boolean effectual;
    private final boolean checkBlockIterator;
    private final List<AttackCallback> callbacks;
    private final long preSwingTime;
    private final Runnable onSwingStart;

    private long swingStartTime = -1;
    private boolean hasTriggered = false;

    private AttackTask(Builder builder) {
        super(builder.entity);
        this.parentEntity = builder.parentEntity;
        this.target = builder.target;
        this.damage = builder.damage;
        this.viewAngle = builder.viewAngle;
        this.effectual = builder.effectual;
        this.checkBlockIterator = builder.checkBlockIterator;
        this.callbacks = builder.callbacks;
        this.preSwingTime = builder.preSwingTime;
        this.onSwingStart = builder.onSwingStart;
    }

    @Deprecated
    public AttackTask(IMovingEntity entity, Entity target, float damage, double viewAngle, boolean effectual) {
        this(entity, target, damage, viewAngle, effectual, false);
    }

    @Deprecated
    public AttackTask(IMovingEntity entity, Entity target, float damage, double viewAngle, boolean effectual, boolean checkBlockIterator) {
        this(entity, null, target, damage, viewAngle, effectual, checkBlockIterator);
    }

    @Deprecated
    public AttackTask(IMovingEntity entity, Entity parentEntity, Entity target, float damage, double viewAngle, boolean effectual, boolean checkBlockIterator) {
        this(entity, parentEntity, target, damage, viewAngle, effectual, checkBlockIterator, new ArrayList<>());
    }

    @Deprecated
    public AttackTask(IMovingEntity entity, Entity parentEntity, Entity target, float damage, double viewAngle, boolean effectual, boolean checkBlockIterator, List<AttackCallback> callbacks) {
        super(entity);
        this.parentEntity = parentEntity;
        this.target = target;
        this.damage = damage;
        this.viewAngle = viewAngle;
        this.effectual = effectual;
        this.checkBlockIterator = checkBlockIterator;
        this.callbacks = callbacks;
        this.preSwingTime = 0;
        this.onSwingStart = null;
    }

    @Override
    public void onUpdate(int tick) {
        if (swingStartTime == -1) {
            sendArmSwingPacket();
            // 初始化前摇
            swingStartTime = System.currentTimeMillis();
            if (preSwingTime > 0 && onSwingStart != null) {
                onSwingStart.run();
            }
        }

        // 检查前摇是否结束
        if (System.currentTimeMillis() - swingStartTime < preSwingTime) {
            return;
        }

        // 确保只执行一次
        if (hasTriggered) return;
        hasTriggered = true;

        // 实际攻击逻辑
        double angle = Mth.atan2(this.target.z - this.entity.getZ(), this.target.x - this.entity.getX());
        double yaw = ((angle * 180) / Math.PI) - 90;
        double min = this.entity.getYaw() - this.viewAngle / 2;
        double max = this.entity.getYaw() + this.viewAngle / 2;

        boolean valid = calculateValid(yaw, min, max);

        if (valid && this.effectual) {
            executeAttack();
        }

        this.entity.updateBotTask(null);
    }

    private boolean calculateValid(double yaw, double min, double max) {
        boolean valid;
        if (min < 0) {
            valid = yaw > 360 + min || yaw < max;
        } else if (max > 360) {
            valid = yaw < max - 360 || yaw > min;
        } else {
            valid = yaw < max && yaw > min;
        }

        if (checkBlockIterator && valid) {
            valid = checkLineOfSight();
        }
        return valid;
    }

    private boolean checkLineOfSight() {
        Vector3 startPoint = this.entity.getEntity().getPosition().add(0, this.entity.getEntity().getEyeHeight(), 0);
        Vector3 endPointHead = this.target.add(0, this.target.getEyeHeight(), 0);
        boolean valid = new Raycaster(this.entity.getLevel(), startPoint, endPointHead).raytraceBlocks().isEmpty();
        if (!valid) {
            valid = new Raycaster(this.entity.getLevel(), startPoint, this.target).raytraceBlocks().isEmpty();
        }
        return valid;
    }

    private void executeAttack() {
        EntityDamageByEntityEvent event = createDamageEvent();
        this.callbacks.forEach(cb -> cb.callback(target, event));
        this.target.attack(event);
    }

    private EntityDamageByEntityEvent createDamageEvent() {
        if (this.parentEntity != null) {
            return new EntityDamageByChildEntityEvent(
                this.parentEntity,
                this.getEntity().getEntity(),
                this.target,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                this.damage
            );
        }
        return new EntityDamageByEntityEvent(
            this.getEntity().getEntity(),
            this.target,
            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
            this.damage
        );
    }

    private void sendArmSwingPacket() {
        EntityEventPacket pk = new EntityEventPacket();
        pk.eid = this.entity.getEntity().getId();
        pk.event = EntityEventPacket.ARM_SWING;
        Server.broadcastPacket(this.getEntity().getEntity().getViewers().values(), pk);
    }

    @Override
    public void forceStop() {
        // 清理逻辑（如有需要）
    }

    public static class Builder {
        private IMovingEntity entity;
        private Entity parentEntity;
        private Entity target;
        private float damage;
        private double viewAngle = 60;
        private boolean effectual = true;
        private boolean checkBlockIterator = false;
        private final List<AttackCallback> callbacks = new ArrayList<>();
        private long preSwingTime = 0;
        private Runnable onSwingStart = null;

        public Builder entity(IMovingEntity entity) {
            this.entity = entity;
            return this;
        }

        public Builder parentEntity(Entity parentEntity) {
            this.parentEntity = parentEntity;
            return this;
        }

        public Builder target(Entity target) {
            this.target = target;
            return this;
        }

        public Builder damage(float damage) {
            this.damage = damage;
            return this;
        }

        public Builder viewAngle(double viewAngle) {
            this.viewAngle = viewAngle;
            return this;
        }

        public Builder effectual(boolean effectual) {
            this.effectual = effectual;
            return this;
        }

        public Builder checkBlockIterator(boolean checkBlockIterator) {
            this.checkBlockIterator = checkBlockIterator;
            return this;
        }

        public Builder addCallback(AttackCallback callback) {
            this.callbacks.add(callback);
            return this;
        }

        public Builder preSwingTime(long preSwingTime) {
            this.preSwingTime = preSwingTime;
            return this;
        }

        public Builder onSwingStart(Runnable onSwingStart) {
            this.onSwingStart = onSwingStart;
            return this;
        }

        public AttackTask build() {
            validate();
            return new AttackTask(this);
        }

        private void validate() {
            if (entity == null || target == null) {
                throw new IllegalArgumentException("Entity and target must be set");
            }
        }
    }

    @FunctionalInterface
    public interface AttackCallback {
        void callback(Entity target, EntityDamageByEntityEvent event);
    }
}
