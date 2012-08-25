package me.jtjj222.SpeedTeleporter;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class SpeedTeleporter extends JavaPlugin implements Listener{
	
	HashMap<String, TeleportRequest> queue = new HashMap<String, TeleportRequest>();
	
	HashMap<String,Location> locations = new HashMap<String,Location>();
		
	public void onEnable() {
		//delay the loading until the worlds are loaded
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			   public void run() {
				   
				   System.out.println("[SpeedTeleporter] Loading locations");
				   
				   reloadConfig();
					boolean deep = false;
					ConfigurationSection configLocations = getConfig().getConfigurationSection("locations.");

					Set<String> keys = configLocations.getKeys(deep);
					
					for (String key: keys) {
						getConfig().getConfigurationSection("locations."+key);
			            double x = getConfig().getDouble("locations."+key+".x");
			            double y = getConfig().getDouble("locations."+key+".y");
			            double z = getConfig().getDouble("locations."+key+".z");
			            float yaw = (float) getConfig().getDouble("locations."+key+".yaw");
			            float pitch = (float) getConfig().getDouble("locations."+key+".pitch");
			            String worldname = getConfig().getString("locations."+key+".world");
			            worldname.trim();
			            
			            World world = getServer().getWorld(worldname);
			            
			            Location loc = new Location(world,x,y,z,yaw,pitch);
			            loc.getChunk().load();
			            
			            locations.put(key, loc);
					}
					
			   }
			}, 200L);
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	public void onDisable() {
        for(String mapKey: locations.keySet()){
            getConfig().createSection("locations."+mapKey);
            getConfig().set("locations."+mapKey+".x", locations.get(mapKey).getX());
            getConfig().set("locations."+mapKey+".y", locations.get(mapKey).getY());
            getConfig().set("locations."+mapKey+".z", locations.get(mapKey).getZ());
            getConfig().set("locations."+mapKey+".yaw", locations.get(mapKey).getYaw());
            getConfig().set("locations."+mapKey+".pitch", locations.get(mapKey).getPitch());
            getConfig().set("locations."+mapKey+".world", locations.get(mapKey).getWorld().getName());
        }
        this.saveConfig();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
				
		Block block = e.getClickedBlock();
		
		if (block.getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) block.getState();
			
			String isAPortalSign = sign.getLine(0);
			
			if (isAPortalSign.contains("[warp]")) {
				
				String destName = sign.getLine(1);
				
				boolean tp = true;
				
				if (destName.contains("notp")) tp = false;
				
				Location dest = null;
				
				if (tp) dest = locations.get(destName);
				int distanceToTeleportAt = Integer.parseInt(sign.getLine(2));
				int speed = Integer.parseInt(sign.getLine(3));
								
				this.queue.put(e.getPlayer().getName(), new TeleportRequest(dest,block.getLocation(), distanceToTeleportAt, speed, tp));
				
			}
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (queue.isEmpty() == true) return;
		
		if (this.queue.containsKey(e.getPlayer().getName())) {
			TeleportRequest req = queue.get(e.getPlayer().getName());
			
			double dist = e.getPlayer().getLocation().distance(req.getSource());
			
			if (dist > req.distToTeleportAt) {
				queue.remove(e.getPlayer().getName());
				
				req.getDest().getChunk().load();
				e.getPlayer().teleport(req.getDest());
			} else {
				makeGoFaster(e.getPlayer(), req.speed);
			}
		}
	}
	
	private void makeGoFaster(Player p, int speed) {
		Vector velocity = p.getLocation().getDirection().normalize();
		velocity.setY(0);
		velocity.setX(velocity.getX() * speed);
		velocity.setZ(velocity.getZ() * speed);
		p.setVelocity(velocity);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to use that command, sorry");
			return false;
		}
		if (args.length >= 1) {
			if (args[0].contains("set") && sender.hasPermission("SpeedTeleporter.addLocation")) {
				if (args.length == 2) {
					String name = args[1];
					Location location = sender.getServer().getPlayer(sender.getName()).getLocation();
					
					locations.put(name, location);
					sender.sendMessage("Added " + name + " to the locations list.");
				}
			} else if(args[0].contains("list")) {
				for (String key : locations.keySet()) {
					sender.sendMessage(ChatColor.GOLD + "- " + key);
					sender.sendMessage(ChatColor.GOLD + "-- " + locations.get(key).getX());
					sender.sendMessage(ChatColor.GOLD + "-- " + locations.get(key).getY());
					sender.sendMessage(ChatColor.GOLD + "-- " + locations.get(key).getZ());
					sender.sendMessage(ChatColor.GOLD + "world: " + locations.get(key).getWorld().getName());
				}
			} else {
				sender.sendMessage(ChatColor.GOLD + "[SpeedTeleporter]" + ChatColor.RESET +"Please enter a valid command");
			}

		} else {
			sender.sendMessage(ChatColor.GOLD + "[SpeedTeleporter]" + ChatColor.RESET +"Too few arguments");
		}
		
		return false;
	}
}
