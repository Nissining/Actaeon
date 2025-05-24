package me.onebone.actaeon.route;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SimpleRouteFinder implements IRouteFinder {

	public long getRouteFindCooldown() {
		return 100;
	}

	@Override
	public ObjectArrayList<Node> search(Entity entity, Position start, Position destination) {
		ObjectArrayList<Node> result = new ObjectArrayList<>();
		result.add(new Node(destination));  // just go straight
		return result;
	}

}
