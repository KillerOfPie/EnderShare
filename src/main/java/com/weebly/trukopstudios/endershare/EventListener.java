package com.weebly.trukopstudios.endershare;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import com.weebly.trukopstudios.PlayerDataPlus.PDPmain;
import com.weebly.trukopstudios.PlayerDataPlus.Yaml;

public class EventListener implements Listener
{
	private EnderSharePlugin plugin;
	String shared = "EC.Shared", shareRequest = "EC.ShareRequest", shareReceived = "EC.ShareReceived", shareSelect = "EC.SelectedChest"; //sections in players PDP file
	
	public EShareListener(EnderSharePlugin plugin)
	{
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	//join event for loading ec from file or saving too file if not done already
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		
		//making yaml and making sure everything is there
		Yaml pf = PDPmain.getOfflinePlayerYaml(p.getName());
		pf.add(shared, null);
		pf.add(shareReceived, null);
		pf.add(shareRequest, null);
		pf.add(shareSelect, p.getName());
		pf.save();
		
		//tell player what's happening with their endershare
		if(pf.getString(shared) != null)
			p.sendMessage(colorize("&9You are sharing Ender Chests with &5" + pf.getString(shared) + "&9!"));
		else if(pf.getString(shareReceived) != null)
			p.sendMessage(colorize("&9You have recieved an Ender Share Request from &5" + pf.getString(shareReceived) + "&9!"));
		else
			p.sendMessage(colorize("&9You are not sharing Ender Chests with anyone&9!"));
		
		//Checks if stroed enderchest is empty and if players enderchest contains items
		if(plugin.loadInv("Enderchest", p.getName()) == null && p.getEnderChest().getContents().length > 0)
		{
			//Saves players enderchest into storage
			plugin.saveInv(e.getPlayer().getEnderChest().getContents(), "Enderchest", e.getPlayer().getName());
		}
		else
		{
			//Otherwise loads enderchest from storage into active enderchest
			p.getEnderChest().setContents(plugin.loadInv("Enderchest", e.getPlayer().getName()));
		}
	}
	
	//checking for enderchest opening(maybe do this on an inventory open event rather the interect?)
	@EventHandler
	public void onECopen(PlayerInteractEvent e)
	{
		//making sure the action exists?
		if(e.getAction() != null)
			//check for use on block
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
				//check if block is an enderchest
				if(e.getClickedBlock().getType() == Material.ENDER_CHEST)
				{
					//get player and share info
					Player p = e.getPlayer(); //get player
					Yaml pf = PDPmain.getOfflinePlayerYaml(p.getName()); //get players PDP file for dat sweet juicy info
					Inventory chest = null; //create inventory to be filled later
					Server server = Bukkit.getServer(); //making server obj because im lazy...duuuuhhhh
					String p2 = pf.getString(shareSelect); //setting p2 to whichever view the player has selected
					
					//checking if player is online
					if(server.getPlayerExact(p2) != null)
					{
						//setting the chest to whichever player is selected
						Player target = server.getPlayer(p2);
						chest = target.getEnderChest();
					}
					else
					{
						//otherwise opening other players chest from file and giving inventory special name that will be checked for later
						chest = Bukkit.createInventory(p, InventoryType.ENDER_CHEST, p2 + "'s Ender Chest");
						chest.setContents(plugin.loadInv("Enderchest", p2));
					}
					
					//cancelling event(should i do this earlier, idk whatever)
					e.setCancelled(true);
					//opening inventory for player
					p.openInventory(chest);
				}
	}
	
	//just saving ec to file when the player leaves
	@EventHandler
	public void onLeave(PlayerQuitEvent e)
	{
		//again just saving ec when the player leaves
		plugin.saveInv(e.getPlayer().getEnderChest().getContents(), "Enderchest", e.getPlayer().getName());
	}
	
	//trigger on every inventory click(for conbstant saving to prevent duplicating items
	@EventHandler
	public void onECEdit(InventoryClickEvent e)
	{
		//checking to make sure inventory contains enderchest, that player clicked in enderchest or shift clicked, and that the click was not on air
		if(e.getInventory().getType() == InventoryType.ENDER_CHEST && e.getCurrentItem() != null && (e.getClickedInventory().getType() == InventoryType.ENDER_CHEST || e.isShiftClick()))
			//checks name of inventory
			if(e.getInventory().getName().contains("'s Ender Chest"))
				//only triggers if we made from file so it needs to be re saved
				plugin.saveInv(e.getInventory().getContents(), "Enderchest", e.getInventory().getName().replace("'s Ender Chest", ""));
	}
	
	//trigger on inventory close, am i just saying the obvious shit at this point?
	@EventHandler
	public void onECClose(InventoryCloseEvent e)
	{
		//make sure its an enderchest
		if(e.getInventory().getType() == InventoryType.ENDER_CHEST)
			//check for the name we made
			if(e.getInventory().getName().contains("'s Ender Chest"))
				//save dat shiz
				plugin.saveInv(e.getInventory().getContents(), "Enderchest", e.getInventory().getName().replace("'s Ender Chest", ""));
	}
	
	//replaces &x with color codes to allow colored text --- only here because im to lazy to do 'plugin.'
	public String colorize(String msg)
	{
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}
}