package me.onebone.actaeon.entity.monster;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityZombie;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.actaeon.entity.Fallable;
import me.onebone.actaeon.hook.AttackHook;
import me.onebone.actaeon.target.AreaHaterTargetFinder;

public class Zombie extends Monster implements Fallable {
    public static final int NETWORK_ID = EntityZombie.NETWORK_ID;

    public Zombie(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setTargetFinder(new AreaHaterTargetFinder(
                this, 500, 20000));
        this.addHook("attack", new AttackHook(
                this,
                this.getAttackDistance(),
                this::getDamage,
                1000,
                10,
                180));
    }

    @Override
    public String getName() {
        return "Zombie";
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getGravity() {
        return 0.05f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @Override
    public float getEyeHeight() {
        return 1.62f;
    }

    @Override
    protected double getStepHeight() {
        return 1;
    }

    @Override
    public Item[] getDrops() {
        return new Item[0];
    }

    public double getAttackDistance() {
        return 1;
    }

    @Override
    public float getDamage() {
        return 8;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void knockBack(Entity attacker, double damage, double x, double z, double base) {
        super.knockBack(attacker, damage, x, z, base);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        source.setAttackCooldown(0);
        return super.attack(source);
    }
}
