package io.github.ultimatedillon.explosiverollback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.CommandsHandler.CommandRollback;
import de.diddiz.LogBlock.QueryParams;

@SuppressWarnings({ "unused", "deprecation" })
public final class ExplosiveRollback extends JavaPlugin {
	private Consumer lbconsumer = null;
	private LogBlock logblock;
	
	public Map<String, String> paramsMap = new HashMap<>();
	public Map<String, Integer> locMap = new HashMap<>();
	private Map<Object, Object> targets = new HashMap<>();
	private Map<Object, Object> targetsbypig = new HashMap<>();
	protected Set<Entity> hoverpigs = new HashSet<>();
	
	Player player;
	
	@Override
	public void onEnable() {
		final PluginManager pm = getServer().getPluginManager();
		logblock = (LogBlock) pm.getPlugin("LogBlock");
		
		if (logblock != null) {
			lbconsumer = ((LogBlock) logblock).getConsumer();
		}
		
		pm.registerEvents(new ImpactListener(this), this);
	}
	
	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
	}
	
	public void triggerRollback(QueryParams params) {
		try {
			logblock.getCommandsHandler().new CommandRollback(player, params, true);
		} catch (Exception ex) {
			getLogger().warning("[ExplosiveRollback] Could not trigger rollback!");
			ex.printStackTrace();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("explode")) {
			player = (Player) sender;
			final World world = player.getWorld();
			Location ploc = player.getLocation();
			double pyaw = Math.toRadians((ploc.getYaw() + 90.0F) % 360.0F);
    	    double ppitch = Math.toRadians(ploc.getPitch() * -1.0F);
    	    
			final Location targLoc = player.getTargetBlock(null, 200).getLocation();
			paramsMap.put("player", args[0]);
			paramsMap.put("radius", args[1]);
			locMap.put("x", targLoc.getBlockX());
			locMap.put("y", targLoc.getBlockY());
			locMap.put("z", targLoc.getBlockZ());
			
			Location loc = player.getLocation().add(2.0D * Math.cos(pyaw), 2.0D, 2.0D * Math.sin(pyaw));
    		final Entity mob = world.spawnEntity(loc, EntityType.PIG);
    		
    		hoverpigs.add(mob);
    		targets.put(player, targLoc);
    		targetsbypig.put(mob, Integer.valueOf(targets.get(player).hashCode()));
    		
    		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
    			public void run() {
	            	int targtemp = targetsbypig.get(mob).hashCode();
	            	boolean temp = true;
	            	
	            	if ((temp) && (world.getLivingEntities().contains(mob))) {
	            		Vector pvec = new Vector();
	
	            		pvec.setX(targLoc.getBlockX() 
	            				- mob.getLocation().getX());
	            		pvec.setZ(targLoc.getBlockZ() 
	            				- mob.getLocation().getZ());
	            		pvec.setY(targLoc.getBlockY() 
	            				- mob.getLocation().getY());
	            		
	            		if (pvec.length() <= 1.0D) {
	            			mob.setFireTicks(20);
	            		} else {
	            			mob.setVelocity(pvec.normalize().multiply(1.25D));
	            			mob.setFallDistance(20.0F);
	            		}
	            	}
	            }
    		}, 1L, 1L);
    		
			return true;
		}
		
		return false;
	}
}
