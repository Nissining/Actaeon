package me.onebone.actaeon.target;

import cn.nukkit.math.Mth;
import cn.nukkit.math.Vector3;
import me.onebone.actaeon.entity.IMovingEntity;
import me.onebone.actaeon.route.AdvancedRouteFinder;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * StrollingTargetFinder
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public class StrollingTargetFinder extends TargetFinder {

    public int needFind = ThreadLocalRandom.current().nextInt(3);
    public int needFindResetMax = 8;
    public int needFindResetUntil = 0;
    private final double radius;
    public int findHighest = -1;  //<0为关闭寻找最高，0为至虚空，>0为向下方块格数限制

    public StrollingTargetFinder(IMovingEntity entity) {
        this(entity, 5);
    }

    public StrollingTargetFinder(IMovingEntity entity, double radius) {
        this(entity, radius, 1000 * (ThreadLocalRandom.current().nextInt(2) + 2));
    }

    public StrollingTargetFinder(IMovingEntity entity, double radius, long inter) {
        super(entity, inter);
        this.radius = radius;
    }

    @Override
    protected void find() {
        if (this.needFind > 0) {
            Random random = ThreadLocalRandom.current();
            Vector3 base = this.entity.getRealTarget() != null ? this.entity.getTarget() : this.getEntity().getPosition();
            //for (int i = 0; i < 5; i++) {
                float r = random.nextFloat() * 360;
                double x = this.radius * Mth.cos(Math.toRadians(r));
                double z = this.radius * Mth.sin(Math.toRadians(r));
                double y = base.getY();
                if (this.findHighest >= 0 && this.getEntity().getRouter().getRouteFinder() instanceof AdvancedRouteFinder) {
                    Vector3 highest = AdvancedRouteFinder.getHighestUnder(entity.getEntity(), x, y + 2, z, this.findHighest == 0 ? (int)y + 2 : this.findHighest);
                    if (highest == null)
                        return;  //不可走, 重新尝试选点
                    y = highest.getY() + 1;
                }
                this.entity.setTarget(new Vector3(base.getX() + x, y, base.getZ() + z), this.getEntity().getName());
                //break;
            //}
            this.needFind--;
            if (random.nextInt(10) < 2) {
                this.entity.getEntity().setSprinting(true);
            } else {
                this.entity.getEntity().setSprinting(false);
            }
        } else {
            this.entity.setTarget(null, this.getEntity().getName(), true);
        }
        if (this.needFind <= this.needFindResetUntil) this.needFind = this.needFindResetMax;
    }

}
