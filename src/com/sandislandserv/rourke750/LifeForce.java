package com.sandislandserv.rourke750;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.sandislandserv.rourke750.database.Storage;

public class LifeForce extends JavaPlugin{

	private PlayerHandler ph;
	private TimeTracker tt;
	public void onEnable(){
		configHandler(); // Initiates the generation for the config
		Storage storage = new Storage(getConfig(), this); // load the database
		ph = new PlayerHandler(storage, getConfig());
		tt = new TimeTracker(ph);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, tt, 0, 
				getConfig().getLong("settings.timetable.clock"));
		registerEvent();
		getCommand("lf").setExecutor(new CommandHandler(ph));
	}
	
	public void onDisable(){
		tt.flushAll();
	}
	
	// Incase I want to do something more advanced in the future, it's nice and organized
	public void configHandler(){
		if (!(new File(getDataFolder() + File.separator + "config.yml").exists()))
			saveDefaultConfig(); // generate the pregenerated config
	}
	
	public void registerEvent(){
		getServer().getPluginManager().registerEvents(new PlayerListeners(ph, tt), this);
		if (getServer().getPluginManager().getPlugin("PrisonPearl").isEnabled())
			getServer().getPluginManager().registerEvents(new PrisonPearlListener(), this);
	}
}
