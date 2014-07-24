package com.sandislandserv.rourke750;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CommandHandler implements CommandExecutor {

	private PlayerHandler ph;
	public CommandHandler(PlayerHandler ph){
		this.ph = ph;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] arg3) {
		if (arg3.length == 0){
			sender.sendMessage(ChatColor.RED+"/lf apple <amount>: converts stamina to apples.");
			return true;
		}
		if (arg3[0].equalsIgnoreCase("apple")){
			if (!(sender instanceof Player))
				sender.sendMessage("Must be a player to use this command!");
			Player player = (Player) sender;
			int amount = 1;
			if (arg3.length > 1){
				String x = arg3[1];
				try{
					amount = Integer.parseInt(x);
				}catch(Exception ex){
					player.sendMessage(ChatColor.RED + "You must enter only a number as the second arg!");
					return true;
				}
				Inventory inv = player.getInventory();
				for (int a = 0; a < amount; a++){
					if (ph.hasEnoughStamina(player)){
						inv.addItem(ph.createStaminaApple());
						ph.removeStamina(player, "apple");
					}
					else 
						return true;
				}
			}
			else
				if (ph.hasEnoughStamina(player)){
					Inventory inv = player.getInventory();
					inv.addItem(ph.createStaminaApple());
					ph.removeStamina(player, "apple");
				}
			return true;
		}
		return false;
	}
}
