package com.sandislandserv.rourke750;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sandislandserv.rourke750.database.Storage;

public class TimeTracker implements Runnable{

	private PlayerHandler ph;
	
	public TimeTracker(PlayerHandler ph){
		this.ph = ph;
	}
	
	private static Map<Player, Long> time = new HashMap<Player, Long>();
	@Override
	public void run() {
		// method used to award stamina
		for (Player player: time.keySet()){ 
			long ti = time.get(player);
			if (ti < System.currentTimeMillis()){
				ph.awardPoints(player);
				time.put(player, System.currentTimeMillis() + ph.getConfigTimeModifier());
			}
			ph.applyNegativeAffects(player);
		}
	}

	public void addPlayer(Player player){
		long ti = ph.getPlayerTime(player.getUniqueId());
		ti += System.currentTimeMillis();
		time.put(player, ti);
	}
	
	public void flushPlayer(Player player){
		if (!time.containsKey(player)) return;
		long remain = time.get(player) - System.currentTimeMillis();
		time.remove(player);
		ph.updateTimeRemaining(player, remain);
	}
	
	public void flushAll(){
		for (Player player: Bukkit.getOnlinePlayers()){
			long remain = time.get(player) - System.currentTimeMillis();
			ph.updateStamina(player);
			ph.updateTimeRemaining(player, remain);
		}
	}
}
