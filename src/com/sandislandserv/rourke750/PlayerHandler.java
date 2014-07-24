package com.sandislandserv.rourke750;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.sandislandserv.rourke750.database.Storage;

public class PlayerHandler {
	
	private Map<Player, Integer> stamina = new HashMap<Player, Integer>();
	private Map<Player, Score> sco = new HashMap<Player, Score>();

	//config options
	private int required;
	private int reTurn;
	private int timeLeft;
	private int maxStamina;
	private boolean ban;
	private int minBanStamina;
	private int deathLost;
	private int newPlayerStamina;
	
	private Storage storage;
	private FileConfiguration config;
	public PlayerHandler(Storage storage, FileConfiguration config){
		this.config = config;
		this.storage = storage;
		required = config.getInt("settings.apples.required");
		reTurn = config.getInt("settings.apples.return");
		timeLeft = (config.getInt("settings.apples.time.hour") * 3600 * 1000)
				+ (config.getInt("settings.apples.time.min") * 60 * 1000)
				+ (config.getInt("settings.apples.time.sec") * 1000);
		maxStamina = config.getInt("settings.max");
		ban = config.getBoolean("death.death-ban.enable");
		minBanStamina = config.getInt("death.death-ban.min-stamina");
		deathLost = config.getInt("death.stamina.on-death-lose");
		newPlayerStamina = config.getInt("settings.players.joining-stamina");
	}
	
	public long getPlayerTime(UUID name){
		return storage.getPlayerTime(name);
	}
	
	public int getConfigTimeModifier(){
		int multi = (config.getInt("settings.timetable.hour") * 3600 * 1000) 
				+ (config.getInt("settings.timetable.min") * 60 * 1000) 
				+ (config.getInt("settings.timetable.sec") * 1000);
		return multi;
	}
	
	public void awardPoints(Player player){
		int stam = stamina.get(player) + config.getInt("settings.timetable.amount");
		if (stam > maxStamina && maxStamina != -1){
			stam = maxStamina;
			player.sendMessage(ChatColor.GREEN + "You have reached the max amount of stamina allowed to be held.\n"
					+ "If you want to gain more convert stamina to golden apples using /lf apple <number>");
		}
		stamina.put(player, stam);
		sco.get(player).setScore(getStamina(player));
	}
	
	public void awardPoints(Player player, boolean conf){
		if (!conf) return;
		int stam = stamina.get(player) + reTurn;
		if (stam > maxStamina && maxStamina != -1){
			stam = maxStamina;
			player.sendMessage(ChatColor.GREEN + "You have reached the max amount of stamina allowed to be held.\n"
					+ "If you want to gain more convert stamina to golden apples using /lf apple <number>");
		}
		stamina.put(player, stam);
		sco.get(player).setScore(getStamina(player));
	}
	
	public void updateTimeRemaining(Player player, long time){
		storage.updateTime(player.getUniqueId(), time);
	}
	
	public void updateStamina(Player player){
		if (!stamina.containsKey(player)) return;
		storage.addStamina(player.getUniqueId(), stamina.get(player));
		stamina.remove(player);
	}
	
	public void createScoreBoard(Player player){
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Team team = board.registerNewTeam(player.getName());
		team.addPlayer(player);
		Objective objective = board.registerNewObjective("test", "stat");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Life Force");
		Score score = objective.getScore(ChatColor.GREEN + "Stamina:");
		score.setScore(getStamina(player));
		player.setScoreboard(board);
		sco.put(player, score);
	}
	
	private int getStamina(Player player){
		if (!stamina.containsKey(player)){
			stamina.put(player, storage.getStamina(player.getUniqueId()));
		}
		return stamina.get(player);
	}
	
	public boolean canEatApple(ItemStack apple){
		ItemMeta data = apple.getItemMeta();
		String time = data.getLore().get(0);
		time = time.replaceAll("§", "");
		long amount = Long.parseLong(time);
		return amount < System.currentTimeMillis();
	}
	
	public int timeLeft(ItemStack apple){
		ItemMeta data = apple.getItemMeta();
		String time = data.getLore().get(0);
		time = time.replaceAll("§", "");
		long amount = Long.parseLong(time);
		return (int) (amount - System.currentTimeMillis()) /60000;
	}
	
	public ItemStack createStaminaApple(){
		ItemStack item = new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1);
		ItemMeta data = item.getItemMeta();
		List<String> string = new ArrayList<String>();
		String x = convertToInvisibleString(String.valueOf(System.currentTimeMillis() + timeLeft));
		string.add(x);
		data.setLore(string);
		item.setItemMeta(data);
		return item;
	}
	
	public boolean hasEnoughStamina(Player player){
		return stamina.get(player) >= required;
	}
	
	public void removeStamina(Player player, String reason){
		int stam = stamina.get(player);
		if (reason.equals("apple"))
			stam = stamina.get(player) - required;
		else if(reason.equals("death"))
			stam = stamina.get(player) - deathLost;
			
		if (stam < minBanStamina && ban){
			player.kickPlayer("You ran out of stamina, you are now banned");
			player.setBanned(true);
		}
		stamina.put(player, stam);
		sco.get(player).setScore(getStamina(player));
	}
	
	public String convertToInvisibleString(String s) {
        String hidden = "";
        for (char c : s.toCharArray()) hidden += ChatColor.COLOR_CHAR+""+c;
        return hidden;
    }
	
	public void applyNegativeAffects(Player player){
		for (String potion: config.getConfigurationSection("neg-potions").getKeys(false)){
			PotionEffectType type = PotionEffectType.getByName(potion);
			int minStamina = config.getInt("neg-potions." + potion + ".min-stamina");
			int potionLevel = config.getInt("neg-potions." + potion + ".min-stamina");
			if (stamina.get(player) > minStamina)
				continue;
			player.addPotionEffect(new PotionEffect(type, potionLevel-1, -1), true);
		}
	}
	
	public void awardNewPlayersStamina(Player player){
		stamina.put(player, newPlayerStamina);
		sco.get(player).setScore(getStamina(player));
	}
}
