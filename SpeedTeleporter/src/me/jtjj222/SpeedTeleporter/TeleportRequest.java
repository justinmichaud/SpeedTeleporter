package me.jtjj222.SpeedTeleporter;

import org.bukkit.Location;

public class TeleportRequest {

	private Location dest, source;
	
	public int distToTeleportAt;
	public int speed;
	public boolean tp;
	
	public Location getDest() {
		return dest;
	}
	
	public Location getSource() {
		return source;
	}
	
	public TeleportRequest(Location destination, Location source, int dist, int speed, boolean tp) {
		this.tp = tp;
		this.dest = destination;
		this.source = source;
		this.speed = speed;
		this.distToTeleportAt = dist;
	}
	
}
