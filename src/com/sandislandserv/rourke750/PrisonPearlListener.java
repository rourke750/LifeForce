package com.sandislandserv.rourke750;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.untamedears.PrisonPearl.SummonEvent;
import com.untamedears.PrisonPearl.SummonEvent.Type;

public class PrisonPearlListener implements Listener{

	@EventHandler(priority = EventPriority.HIGHEST)
	public void summonEvent(SummonEvent event){
		Type ty = event.getType();
		if (ty == Type.SUMMONED)
			PlayerListeners.pearls.put(event.getPrisonPearl().getImprisonedPlayer(), event.getPrisonPearl());
		else 
			PlayerListeners.pearls.remove(event.getPrisonPearl().getImprisonedPlayer());
	}
}
