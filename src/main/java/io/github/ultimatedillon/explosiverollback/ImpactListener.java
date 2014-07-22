package io.github.ultimatedillon.explosiverollback;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;

public class ImpactListener implements Listener {
	private ExplosiveRollback plugin;
	private LogBlock logblock;

	public ImpactListener(ExplosiveRollback plugin) {
		this.plugin = plugin;
		logblock = (LogBlock) plugin.getServer().getPluginManager().getPlugin("LogBlock");
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (plugin.hoverpigs.contains(entity)) {
			plugin.hoverpigs.remove(entity);
			
			Location targLoc = new Location(entity.getWorld(),
											plugin.locMap.get("x"),
											plugin.locMap.get("y"),
											plugin.locMap.get("z"));
			
			QueryParams params = new QueryParams(logblock);
			params.setPlayer(plugin.paramsMap.get("player"));
			params.world = entity.getWorld();
			params.radius = Integer.valueOf(plugin.paramsMap.get("radius"));
			params.loc = targLoc;
			params.silent = true;
			
			entity.getWorld().createExplosion(entity.getLocation(), 0);
			entity.remove();
			
			plugin.triggerRollback(params);
		}
	}
}
