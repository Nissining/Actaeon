package me.onebone.actaeon.route;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import me.onebone.actaeon.Actaeon;
import me.onebone.actaeon.entity.Climbable;
import me.onebone.actaeon.entity.Fallable;
import net.easecation.eccommons.promise.AsyncPromise;
import net.easecation.eccommons.promise.AsyncTransientScheduler;

import java.util.*;

public class AdvancedRouteFinder implements IRouteFinder {

	@Override
	public AsyncPromise<List<Node>> search(Entity entity, Position start, Position destination) {
		Set<Node> open = new HashSet<>();
		Grid grid = new Grid();
		long forceStopTime = System.currentTimeMillis() + 1000 * 5;  // 寻路超过5秒则强制停止
		AsyncPromise<List<Node>> promise = new AsyncTransientScheduler<List<Node>>("Actaeon route finder (Advanced)", handler -> {
			try {
				Node startNode = new Node(start.floor());
				Node endNode = new Node(destination.floor());
				try {
					startNode.f = startNode.g = 0;
					open.add(startNode);
					grid.putNode(startNode.getVector3(), startNode);
					grid.putNode(endNode.getVector3(), endNode);
				} catch (Exception e) {
					Actaeon.getInstance().getLogger().alert("Failed to put start node or end node to grid", e);
					return;
				}

				int limit = 500;
				while (!open.isEmpty() && limit-- > 0) {
					if (System.currentTimeMillis() > forceStopTime) {
						Actaeon.getInstance().getLogger().alert("Route finder (Advanced) force stopped");
						return;
					}
					Node node = null;

					double f = Double.MAX_VALUE;
					try {
						for (Node cur : open) {
							if (cur.f < f && cur.f != -1){
								node = cur;
								f = cur.f;
							}
						}
					} catch (Exception e) {
						Actaeon.getInstance().getLogger().alert("Failed to find node with lowest f", e);
						return;
					}

					if (endNode.equals(node)) {
						List<Node> nodes = new ArrayList<>();
						nodes.add(node);
						Node last = node;
						while ((node = node.getParent()) != null) {
							Node lastNode = nodes.get(nodes.size() - 1);
							node.add(0.5, 0, 0.5);
							Vector3 direction = new Vector3(node.getX() - lastNode.getX(), node.getY() - lastNode.getY(), node.getZ() - lastNode.getZ()).normalize().divide(4);
							if (lastNode.getY() == node.getY() && direction.lengthSquared() > 0) {  //Y不改变
								WalkableIterator iterator = new WalkableIterator(this, entity, entity.getLevel(), lastNode.getVector3(), direction, entity.getWidth(), (int) lastNode.getVector3().distance(node.getVector3()) + 1);
								if (iterator.hasNext()) {  //无法直接到达
									//Block block = iterator.next();
									//Server.getInstance().getLogger().info(block.toString());
									//level.addParticle(new cn.nukkit.level.particle.HappyVillagerParticle(node.getVector3()));
									nodes.add(last);
									//Server.broadcastPacket(level.getPlayers().values().stream().toArray(Player[]::new), new cn.nukkit.level.particle.CriticalParticle(node.getVector3(), 3).encode()[0]);
									nodes.add(node);
								} else {
									//Server.broadcastPacket(level.getPlayers().values().stream().toArray(Player[]::new), new cn.nukkit.level.particle.AngryVillagerParticle(node.getVector3()).encode()[0]);
								}
							} else {  //Y变了直接放入list
								//Server.broadcastPacket(level.getPlayers().values().stream().toArray(Player[]::new), new cn.nukkit.level.particle.CriticalParticle(node.getVector3(), 3).encode()[0]);
								nodes.add(node);
							}
							last = node;
							if (System.currentTimeMillis() > forceStopTime) {
								Actaeon.getInstance().getLogger().alert("Route finder (Advanced) force stopped");
								return;
							}
						}

						Collections.reverse(nodes);

						nodes.remove(nodes.size() - 1);
						Vector3 highestUnder = getHighestUnder(entity, destination.getX(), destination.getY(), destination.getZ());
						if (highestUnder != null) {
							Node realDestinationNode = new Node(new Vector3(destination.getX(), highestUnder.getY() + 1, destination.getZ()));
							realDestinationNode.setParent(node);
							nodes.add(realDestinationNode);
						}
						handler.handle(nodes);
						return;
					}

					node.closed = true;
					open.remove(node);

					for (Node neighbor : this.getNeighbors(entity, grid, node)) {
						if(neighbor.closed) continue;

						double tentative_gScore = node.g + neighbor.getVector3().distance(node.getVector3());

						if (!open.contains(neighbor)) open.add(neighbor);
						else if (neighbor.g != -1 && tentative_gScore >= neighbor.g) continue;

						neighbor.setParent(node);
						neighbor.g = tentative_gScore;
						neighbor.f = neighbor.g + this.heuristic(neighbor.getVector3(), endNode.getVector3());

						if (System.currentTimeMillis() > forceStopTime) {
							Actaeon.getInstance().getLogger().alert("Route finder (Advanced) force stopped");
							return;
						}
					}
				}

			} catch (Exception e) {
				Server.getInstance().getLogger().logException(e);
			}
		}).schedule();

		return promise;
	}

	@Override
	public long getRouteFindCooldown() {
		return 250;
	}

	public static Set<Node> getNeighbors(Entity entity, Grid grid, Node node) {
		Set<Node> neighbors = new HashSet<>();

		Vector3 vec = node.getVector3();
		boolean s1, s2, s3, s4;

		double y;
		if(s1 = (y = isWalkableAt(entity, vec.add(1, 0, 0))) != -256){
			neighbors.add(grid.getNode(vec.add(1, y, 0)));
		}

		if(s2 = (y = isWalkableAt(entity, vec.add(-1, 0, 0))) != -256){
			neighbors.add(grid.getNode(vec.add(-1, y, 0)));
		}

		if(s3 = (y = isWalkableAt(entity, vec.add(0, 0, 1))) != -256){
			neighbors.add(grid.getNode(vec.add(0, y, 1)));
		}

		if(s4 = (y = isWalkableAt(entity, vec.add(0, 0, -1))) != -256){
			neighbors.add(grid.getNode(vec.add(0, y, -1)));
		}

		if(s1 && s3 && (y = isWalkableAt(entity, vec.add(1, 0, 1))) != -256){
			neighbors.add(grid.getNode(vec.add(1, y, 1)));
		}

		if(s1 && s4 && (y = isWalkableAt(entity, vec.add(1, 0, -1))) != -256){
			neighbors.add(grid.getNode(vec.add(1, y, -1)));
		}

		if(s2 && s3 && (y = isWalkableAt(entity, vec.add(-1, 0, 1))) != -256){
			neighbors.add(grid.getNode(vec.add(-1, y, 1)));
		}

		if(s2 && s4 && (y = isWalkableAt(entity, vec.add(-1, 0, -1))) != -256){
			neighbors.add(grid.getNode(vec.add(-1, y, -1)));
		}

		return neighbors;
	}

	public static Vector3 getHighestUnder(Entity entity, double x, double dy, double z) {
		return getHighestUnder(entity, x, dy, z, (int)dy);
	}

	public static Vector3 getHighestUnder(Entity entity, double x, double dy, double z, int limit) {
		int minY = Math.max((int) dy - limit, 0);
		for(int y = (int)dy; y >= minY; y--){
			int blockId = entity.getLevel().getBlock((int)x, y, (int)z, false).getId();

			if(!canWalkOn(blockId)) return new Vector3(x, y, z);
			if(!Router.canPassThrough(blockId)) return new Vector3(x, y, z);
		}
		return null;
	}

	public static double isWalkableAt(Entity entity, Vector3 vec) {
		Vector3 block = getHighestUnder(entity, vec.x, vec.y + 2, vec.z);
		if (block == null) {
			return -256;
		}

		double diff = (block.y - vec.y) + 1;

		if ((entity instanceof Fallable || -4 < diff)
				&& (entity instanceof Climbable || diff <= 1)
				&& canWalkOn(entity.getLevel().getBlock((int)block.x, (int)block.y, (int)block.z, false).getId())
		) {
			return diff;
		}
		return -256;
	}

	private static boolean canWalkOn(int blockId) {
		return !(blockId == Block.FLOWING_LAVA || blockId == Block.LAVA);
	}

	private static double heuristic(Vector3 one, Vector3 two) {
		double dx = Math.abs(one.x - two.x);
		double dy = Math.abs(one.y - two.y);
		double dz = Math.abs(one.z - two.z);

		double max = Math.max(dx, dz);
		double min = Math.min(dx, dz);

		return 0.414 * min + max + dy;
	}

	private static class Grid {
		private Map<Double, Map<Double, Map<Double, Node>>> grid = new HashMap<>();

		public void clear(){
			grid.clear();
		}

		public void putNode(Vector3 vec, Node node){
			vec = vec.floor();

			if(!grid.containsKey(vec.x)){
				grid.put(vec.x, new HashMap<>());
			}

			if(!grid.get(vec.x).containsKey(vec.y)){
				grid.get(vec.x).put(vec.y, new HashMap<>());
			}

			grid.get(vec.x).get(vec.y).put(vec.z, node);
		}

		public Node getNode(Vector3 vec){
			vec = vec.floor();

			if(!grid.containsKey(vec.x) || !grid.get(vec.x).containsKey(vec.y) || !grid.get(vec.x).get(vec.y).containsKey(vec.z)){
				Node node = new Node(vec.x, vec.y, vec.z);
				this.putNode(node.getVector3(), node);
				return node;
			}

			return grid.get(vec.x).get(vec.y).get(vec.z);
		}
	}
}
