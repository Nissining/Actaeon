package me.onebone.actaeon;

import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityFactory;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.plugin.PluginBase;
import me.onebone.actaeon.entity.MovingEntity;

public class Actaeon extends PluginBase implements Listener {

	private static Actaeon instance;

	public static Actaeon getInstance() {
		return instance;
	}

	@Override
	public void onLoad() {
		if (instance == null) instance = this;
	}

	public void onEnable(){
		this.saveDefaultConfig();

		//this.registerEntity("Sheep", Sheep.class, Sheep::new);
		//this.registerEntity("Cow", Cow.class, Cow::new);
		//this.registerEntity("Chicken", Chicken.class, Chicken::new);
		//this.registerEntity("Pig", Pig.class, Pig::new);
		//this.registerEntity("Zombie", Zombie.class, Zombie::new);
		//this.registerEntity("Evoker", EntityEvoker.class, EntityEvoker::new);

		//this.getServer().getPluginManager().registerEvents(this, this);
	}

	private void registerEntity(String name, Class<? extends MovingEntity> clazz, EntityFactory factory) {
		Entity.registerEntity(name, clazz, factory, true);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getBlock();
		event.getPlayer().sendMessage("XYZ=" + block.getX() + "," + block.getY() + "," + block.getZ() + " ID=" + block.getId() + " META=" + block.getDamage());
	}
}
