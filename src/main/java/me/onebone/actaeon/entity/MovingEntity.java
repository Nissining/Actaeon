package me.onebone.actaeon.entity;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.potion.Effect;
import me.onebone.actaeon.hook.MovingEntityHook;
import me.onebone.actaeon.route.Node;
import me.onebone.actaeon.route.Router;
import me.onebone.actaeon.target.TargetFinder;
import me.onebone.actaeon.task.MovingEntityTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

abstract public class MovingEntity extends EntityCreature implements IMovingEntity {

    private boolean isKnockback = false;
    private final Router router;
    private TargetFinder targetFinder = null;
    private Vector3 target = null;
    private Entity hate = null;
    private String targetSetter = "";
    public boolean routeLeading = true;
    private final Map<String, MovingEntityHook> hooks = new HashMap<>();
    private MovingEntityTask task = null;
    protected boolean lookAtFront = true;
    protected float collidePlayerToMove = 0f;

    public MovingEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);

        this.router = new Router(this);
        this.setImmobile(false);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        // this.setDataFlag(DATA_FLAG_NO_AI, true, false);
        this.setDataFlag(ProtocolInfo.CURRENT_PROTOCOL, DATA_FLAG_CAN_CLIMB, false);
        this.setDataFlag(ProtocolInfo.CURRENT_PROTOCOL, DATA_FLAG_GRAVITY, false);
    }

    @Override
    public EntityLiving getEntity() {
        return this;
    }

    public Map<String, MovingEntityHook> getHooks() {
        return hooks;
    }

    public void addHook(String key, MovingEntityHook hook) {
        this.hooks.put(key, hook);
    }

    @Override
    public float getGravity() {
        return 0.092f;
    }

    public Entity getHate() {
        return hate;
    }

    public void setHate(Entity hate) {
        this.hate = hate;
    }

    public void jump() {
        if (this.onGround) {
            this.motionY = getJumpPower() + getJumpBoostPower();
        }
    }

    protected float getJumpPower() {
//		return 0.42f;
        return 0.35f;
    }

    public float getJumpBoostPower() {
        Effect jumpBoost = getEffect(Effect.JUMP_BOOST);
        if (jumpBoost == null) {
            return 0;
        }
        return 0.1f * (jumpBoost.getAmplifier() + 1);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        super.onUpdate(currentTick);
        return true;
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (this.closed) {
            return false;
        }

        new ArrayList<>(this.hooks.values()).forEach(hook -> hook.onUpdate(Server.getInstance().getTick()));
        if (this.task != null) this.task.onUpdate(Server.getInstance().getTick());

        boolean hasUpdate = super.entityBaseTick(tickDiff);

        if (this.isKnockback) {                   // knockback 이 true 인 경우는 맞은 직후

        } else if (this.routeLeading && this.onGround) {
            this.motionX = this.motionZ = 0;
        }

        this.motionX *= (1 - this.getDrag());
        this.motionZ *= (1 - this.getDrag());
        if (this.motionX < 0.001 && this.motionX > -0.001) this.motionX = 0;
        if (this.motionZ < 0.001 && this.motionZ > -0.001) this.motionZ = 0;

        if (this.targetFinder != null) this.targetFinder.onUpdate();

        // 如果没在寻路，但是设置了目标，达到了下次寻路计划的时间，就开始寻路
        if (this.routeLeading) {
            this.router.onTick();
            hasUpdate = true;
        }

        if (riding == null && !this.isImmobile()) {
            // 如果未在寻路，并且有寻路路径，则控制实体前往下一个节点
            if (this.routeLeading && (!this.isKnockback || this.getGravity() == 0) && !this.router.isSearching() && this.router.hasRoute() && !router.needStop()) { // entity has route to go
                hasUpdate = true;

                // 获取下一寻路的节点
                Node node = this.router.get();
                if (node != null) {
                    Vector3 vec = node.getVector3();

                    double diffX = Math.pow(vec.x - this.x, 2);
                    double diffZ = Math.pow(vec.z - this.z, 2);
                    double diffY = this.getGravity() == 0 ? Math.pow(vec.y - this.y, 2) : 0;

                    double totalDiff = diffX + diffZ + diffY; // 这里得到所有diff的和

                    // 已经达到了节点
                    if (totalDiff <= 0.15) {
                        // 那么将节点调至下一个节点，如果没有下一个节点了，则到达目的地
                        if (this.router.hasNext()) {
                            this.router.next();
                            //Server.getInstance().getLogger().warning(vec.toString());
                        } else {
                            this.router.arrived();
                        }
                    } else {
                        if (totalDiff > 0) {
                            double ratioX = diffX / totalDiff; // 使用各自的diff除以所有diff的和
                            double ratioZ = diffZ / totalDiff;
                            double ratioY = diffY / totalDiff;

                            int negX = vec.x - this.x < 0 ? -1 : 1;
                            int negZ = vec.z - this.z < 0 ? -1 : 1;
                            int negY = vec.y - this.y < 0 ? -1 : 1;

                            float movementSpeed = this.getMovementSpeed();
                            Effect speed = getEffect(Effect.SPEED);
                            if (speed != null) {
                                movementSpeed += this.getMovementSpeed() * 0.2f * (speed.getAmplifier() + 1);
                            }
                            Effect slowness = getEffect(Effect.SLOWNESS);
                            if (slowness != null) {
                                movementSpeed -= this.getMovementSpeed() * 0.15f * (slowness.getAmplifier() + 1);
                            }

                            this.motionX = Math.min(Math.abs(vec.x - this.x), ratioX * movementSpeed) * negX;
                            this.motionZ = Math.min(Math.abs(vec.z - this.z), ratioZ * movementSpeed) * negZ;

                            if (this.getGravity() == 0) {
                                this.motionY = Math.min(Math.abs(vec.y - this.y), ratioY * movementSpeed) * negY;
                            }
                        }
                        if (this.lookAtFront) {
                            double angle = Math.atan2(vec.z - this.z, vec.x - this.x);
                            this.setRotation((angle * 180) / Math.PI - 90, 0);
                        }
                    }
                }
            }

            for (Entity entity : this.getLevel().getCollidingEntities(this.boundingBox, this)) {
                if (entity.canCollide()) {
                    if (entity instanceof EntityHuman) {
//                        this.onCollideWithPlayer((EntityHuman) entity);
                        // 如果 collidePlayerToMove > 0，则应用运动向量来推动玩家
                        if (collidePlayerToMove > 0f) {
                            Vector3 playerMotion = entity.subtract(this).normalize().multiply(collidePlayerToMove);
                            playerMotion.y = 0;
                            entity.setMotion(playerMotion);
                        }
                    }
                    // 实体之间的碰撞箱
                    double collisionFactor = this.getEntityCollisionFactor();
                    Vector3 motion = this.subtract(entity).normalize();
                    this.motionX += motion.x * collisionFactor;
                    this.motionZ += motion.z * collisionFactor;
                }
            }

            if ((this.motionX != 0 || this.motionZ != 0) && this.isCollidedHorizontally) {
                this.jump();
            }

            Effect levitation = getEffect(Effect.LEVITATION);
            if (levitation != null) {
                this.motionY = 0.045f * (levitation.getAmplifier() + 1);
                resetFallDistance();
            }

            this.move(this.motionX, this.motionY, this.motionZ);

            this.checkGround();
            if (!this.onGround) {
                float gravity = this.getGravity();

                if (gravity > 0 && hasEffect(Effect.SLOW_FALLING)) {
                    gravity = Math.min(gravity, 0.01f);
                    resetFallDistance();
                }

                this.motionY -= gravity;
                //Server.getInstance().getLogger().warning(this.getId() + ": 不在地面, 掉落 motionY=" + this.motionY);
                hasUpdate = true;
            } else {
                this.isKnockback = false;
            }
        }

        return hasUpdate;
    }

    public double getRange() {
        return 100.0;
    }

    public void setTarget(Vector3 vec, String identifier) {
        this.setTarget(vec, identifier, false);
    }

    public void setTarget(Vector3 vec, String identifier, boolean immediate) {
        if (identifier == null) return;

        if (vec == null || immediate || !this.hasSetTarget() || identifier.equals(this.targetSetter)) {
            this.target = vec;
            this.targetSetter = identifier;
        }

        // 如果设置了新的目标，则按需重新开始寻路ding
        // 这边可以直接把某个实体设为Target，会被无缝传入到寻路中，自动更新寻路目标坐标
        if (vec != null) {
            this.router.setDestination(vec instanceof Position ? (Position) vec : Position.fromObject(vec, this.level), immediate || !this.router.hasRoute());
        } else {
            this.router.setDestination(null, true);
        }
    }

    public Vector3 getRealTarget() {
        return this.target;
    }

    public Vector3 getTarget() {
        if (this.target == null) return null;
        return new Vector3(this.target.x, this.target.y, this.target.z);
    }


    /**
     * Returns whether the entity has set its target
     * The entity may not follow the target if there is following target and set target is different
     * If following distance of target is too far to follow or cannot reach, set target will be the next following target
     */
    public boolean hasSetTarget() {
        double range = this.getRange();
        return this.target != null && this.distanceSquared(this.target) < range * range;
    }

    @Override
    protected void checkGroundState(double movX, double movY, double movZ, double dx, double dy, double dz) {
        this.isCollidedVertically = movY != dy;
        this.isCollidedHorizontally = (movX != dx || movZ != dz);
        this.isCollided = (this.isCollidedHorizontally || this.isCollidedVertically);

        // this.onGround = (movY != dy && movY < 0);
        // onGround 는 onUpdate 에서 확인
    }

    private void checkGround() {
        AxisAlignedBB[] list = this.level.getCollisionCubes(
                this,
                this.level.getTickRate() > 1
                        ? this.boundingBox.getOffsetBoundingBox(0, -1, 0)
                        : this.boundingBox.addCoord(0, -1, 0),
                false
        );

        double maxY = -1;
        for (AxisAlignedBB bb : list) {
            if (bb.getMaxY() > maxY) {
                maxY = bb.getMaxY();
            }
        }

        this.onGround = (maxY == this.boundingBox.getMinY());
    }

    @Override
    public void setOnFire(int seconds) {
        int level = 0;

        int ticks = (int) (seconds * 20 * (level * -0.15 + 1));

        if (ticks > 0 && (hasEffect(Effect.FIRE_RESISTANCE) || !isAlive())) {
            extinguish();
            return;
        }

        if (ticks > fireTicks) {
            fireTicks = ticks;
        }
    }

    protected int calculateEnchantmentProtectionFactor(Item item, EntityDamageEvent source) {
        if (!item.hasEnchantments()) {
            return 0;
        }

        int epf = 0;

        for (Enchantment ench : item.getEnchantments()) {
            epf += ench.getProtectionFactor(source);
        }

        return epf;
    }

    @Override
    public void knockBack(Entity attacker, double damage, double x, double z, double base) {
        this.isKnockback = true;
        super.knockBack(attacker, damage, x, z, base);
    }

    @Override
    public void knockBack(Entity attacker, double damage, double x, double z, double baseH, double baseV) {
        this.isKnockback = true;
        super.knockBack(attacker, damage, x, z);
    }

    public Router getRouter() {
        return router;
    }

    public void setTargetFinder(TargetFinder targetFinder) {
        this.targetFinder = targetFinder;
    }

    @Override
    public TargetFinder getTargetFinder() {
        return targetFinder;
    }

    public void updateBotTask(MovingEntityTask task) {
        if (this.task != null) this.task.forceStop();
        this.task = task;
        if (task != null) this.task.onUpdate(Server.getInstance().getTick());
    }

    public MovingEntityTask getTask() {
        return task;
    }

    public boolean isLookAtFront() {
        return lookAtFront;
    }

    public void setLookAtFront(boolean lookAtFront) {
        this.lookAtFront = lookAtFront;
    }

    /**
     * 实体碰撞因数，数值越小被碰撞的影响越小
     */
    public double getEntityCollisionFactor() {
        return 0.3f;
    }

}
