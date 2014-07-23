package io.github.ultimatedillon.explosiverollback;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import de.diddiz.LogBlock.BlockChange;
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
			params.needId = true;
			params.needPlayer = true;
			params.needData = true;
			params.needCoords = true;
			
			entity.getWorld().createExplosion(entity.getLocation(), 0);
			entity.remove();
			
			try {
				for (BlockChange bc : logblock.getBlockChanges(params)) {
					if (!bc.getLocation().getBlock().getType().equals(Material.AIR)) {
						params.world.createExplosion(bc.getLocation(), 0);
					}
				}
			} catch (Exception ex) {
				Bukkit.getLogger().warning("[ExplosiveRollback] Could not trigger explosions!");
				Bukkit.getLogger().warning(ex.getMessage());
			}
			
			plugin.triggerRollback(params);
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		
		if (plugin.hoverpigs.contains(entity)) {
			plugin.hoverpigs.remove(entity);
			event.getDrops().clear();
		}
	}
}
