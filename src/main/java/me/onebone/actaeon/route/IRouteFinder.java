package me.onebone.actaeon.route;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.concurrent.CompletableFuture;

public interface IRouteFinder {

    CompletableFuture<ObjectArrayList<Node>> searchAsync(Entity entity, Position start, Position destination);

    /**
     * 寻路的主要运算逻辑
     */
    ObjectArrayList<Node> search(Entity entity, Position start, Position destination);

    /**
     * @return 每次寻路开始到下次寻路的冷却时间
     */
    long getRouteFindCooldown();

}
