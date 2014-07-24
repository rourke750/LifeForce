package com.sandislandserv.rourke750;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import com.untamedears.PrisonPearl.PrisonPearl;

public class PlayerListeners implements Listener{
	
	private PlayerHandler ph;
	private TimeTracker tt;
	public static Map<Player, PrisonPearl> pearls = new HashMap<Player, PrisonPearl>();
	
	public PlayerListeners(PlayerHandler ph, TimeTracker tt){
		this.ph = ph;
		this.tt = tt;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerLoginEvent(PlayerJoinEvent event){
		Player player = event.getPlayer();
		tt.addPlayer(player);
		ph.createScoreBoard(player);
		if (!player.hasPlayedBefore())
			ph.awardNewPlayersStamina(player);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void playerQuitEvent(PlayerQuitEvent event){
		Player player = event.getPlayer();
		tt.flushPlayer(player);
		ph.updateStamina(player);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void playerKickEvent(PlayerKickEvent event){
		Player player = event.getPlayer();
		tt.flushPlayer(player);
		ph.updateStamina(player);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerEatEvent(PlayerItemConsumeEvent event){
		ItemStack item = event.getItem();
		if (item.getType() != Material.GOLDEN_APPLE || item.getDurability() != 1 || !item.hasItemMeta())
			return;
		if (!ph.canEatApple(item)){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "This apple is not old enough to eat!\nTime remaining: "
					+ ph.timeLeft(item) +" minutes.");
			return;
		}
		Player player = event.getPlayer();
		ph.awardPoints(player, true);
		for(PotionEffect effect : player.getActivePotionEffects())
		    player.removePotionEffect(effect.getType());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void craftEvent(CraftItemEvent event){
		if (Material.GOLDEN_APPLE == event.getRecipe().getResult().getType() && 1 == 
				event.getRecipe().getResult().getDurability())
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent event){
		Player player = event.getEntity();
		if (pearls.containsKey(player))
			return;
		ph.removeStamina(player, "death");
	}
}
