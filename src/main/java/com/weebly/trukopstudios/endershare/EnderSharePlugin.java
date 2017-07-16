package com.weebly.trukopstudios.endershare;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.weebly.trukopstudios.PlayerDataPlus.PDPmain;
import com.weebly.trukopstudios.PlayerDataPlus.Yaml;

public class EnderSharePlugin extends JavaPlugin
{
	//PDP storage values that will be sued often
	String shared = "EC.Shared", shareRequest = "EC.ShareRequest", shareReceived = "EC.ShareReceived", shareSelect = "EC.SelectedChest";
	//Permission node(s)
	String permission = "EnderShare.OEC";
	
	@Override
	public void onEnable()
	{
		this.saveDefaultConfig();
		new EventListener(this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		// No commands support console
		if(!(sender instanceof Player))
		{
			sender.sendMessage(colorize("&4You must be a player to use that command!"));
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("OfflineEnderChest"))
		{
			// no args, need player name
			if(args.length != 1) {
				sender.sendMessage(colorize("&4I need a players name to do that!"));
				return false;
				
			} else {
				String ptemp = args[0];
				//checking if player exists in PDP "database"
				if (PDPmain.playerExist(ptemp)) {
					Player tempp = (Player) sender;
					
					Inventory chest = Bukkit.createInventory(tempp, InventoryType.ENDER_CHEST, ptemp + "'s Ender Chest");
					chest.setContents(loadInv("Enderchest", ptemp));
					tempp.openInventory(chest);
					return true;
					
				} else {
					//the player doesn't exist
					sender.sendMessage(colorize("&4I cannot find that player!"));
					return true;
					
				}
			}
		}
		
		//making strings for playernames and Yaml for loading player
		String p1 = sender.getName(), p2 = null;
		Yaml pf1 = PDPmain.getOfflinePlayerYaml(p1), pf2 = null;
		
		//If the player has a person sharing enderchests with them
		if(pf1.get(shared) != null)
		{
			p2 = pf1.getString(shared);
		}
		//otherwise if there is a player name for the command
		else if(args.length == 1)
		{
			//set arg to var because why tf not?
			String name = args[0];
			
			//checking if player exists through PDP again
			if(PDPmain.playerExist(name))
				//player does exist use dat shiz
				p2 = name;
			else
			{
				//player doesn't exist
				sender.sendMessage(colorize("&4That's not a user!"));
				return false;
			}
		}
		
		//if player 2 name exists load the yaml files and make shure everything is at least null otherwise errors out the wazoo
		if(p2 != null)
		{
			pf2 = PDPmain.getOfflinePlayerYaml(p2);
			
			//add only sets the config file if there is nothing
			pf2.add(shared, null);
			pf2.add(shareReceived, null);
			pf2.add(shareRequest, null);
			pf2.add(shareSelect, null);
			pf1.save();
		}
		
		//setting player 1 values to avoid errors
		pf1.add(shared, null);
		pf1.add(shareReceived, null);
		pf1.add(shareRequest, null);
		pf1.add(shareSelect, null);
		pf1.save();
		
		Server server = Bukkit.getServer();
		
		//endershare command, used for requesting to share chests
		if(cmd.getName().equalsIgnoreCase("endershare"))
		{
			//making share we have a player name
			if(pf2 == null)
			{
				sender.sendMessage(colorize("&4I need another player's name before I can send a request."));
				return false;
			}
			
			//if the player is already sharing chests
			if(pf1.get(shared) != null)
			{
				sender.sendMessage(colorize("&4You are already sharing your enderchest with &5" + p2 + "&4!"));
				return false;
			}
			
			//if player 2 is already sharing enderchests
			if(pf2.get(shared) != null)
			{
				sender.sendMessage(colorize("&5" + p2 + "&4 is already sharing their enderchest!"));
				return false;
			}
			
			//if the player already has a request out
			if(pf1.getString(shareRequest) != null)
			{
				sender.sendMessage(colorize("&4You already have a request out, please cancel that request before making a new one."));
				return true;
			}
			
			//if player 2 already has a request out
			if(pf2.getString(shareReceived) != null)
			{
				sender.sendMessage(colorize("&4That player already has a pending request, please ask them to handle that request."));
				return true;
			}
			
			//setting player 1's request sent
			pf1.set(shareRequest, p2);
			pf1.save();
			
			//setting player 2's request recieved
			pf2.set(shareReceived, p1);
			pf2.save();
			
			sender.sendMessage(colorize("&9You have successfully sent a request!"));
			
			//if target player is are not online we'll send them a message whenever they join
			if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
				server.getPlayer(p2).sendMessage(colorize("&9You have recieved an Ender Share Request from &5" + p1 + "&9!"));
			
			return true;
		}
		// command for accepting a share request
		else if(cmd.getName().equalsIgnoreCase("enderaccept"))
		{
			// Wrong args
			if(args.length != 0)
			{
				sender.sendMessage(colorize("&4You gave me too much information!"));
				return false;
			}
			
			//checks if the player has a share sent to them
			if(pf1.getString(shareReceived) == null)
			{
				sender.sendMessage(colorize("&4You do not have an ender share to accept."));
				return true;
			}
			//checks if the player has already accepted the request
			else if(pf1.get(shared) == (pf1.get(shareReceived)))
			{
				sender.sendMessage(colorize("&9You have already accepted the ender share."));
				return true;
			}
			else
			{
				//getting the player name and setting p2
				p2 = pf1.getString(shareReceived);
				pf2 = PDPmain.getOfflinePlayerYaml(p2);
			}
			
			//setting new information
			pf1.set(shared, p2);
			pf1.set(shareRequest, null);
			pf1.set(shareReceived, null);
			pf1.set(shareSelect, sender.getName());
			pf1.save();
			
			pf2.set(shared, sender.getName());
			pf2.set(shareRequest, null);
			pf2.set(shareReceived, null);
			pf2.set(shareSelect, p2);
			pf2.save();
			
			sender.sendMessage(colorize("&9You have successfully shared enderchests."));
			
			//again checking if other player is online to send message otherwise they'll get it when they next join
			if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
				server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has accepted your Ender Share Request!"));
			
			return true;
		}
		//same as above except the no version
		else if(cmd.getName().equalsIgnoreCase("enderdeny"))
		{
			// checking for oversharing
			if(args.length != 0)
			{
				sender.sendMessage(colorize("&4You gave me too much information!"));
				return false;
			}
			
			if(pf1.getString(shareReceived) == null)
			{
				sender.sendMessage(colorize("&4You do not have an ender share to deny."));
				return true;
			}
			// Player already accepted the request
			else if(pf1.get(shared) == (pf1.get(shareReceived)))
			{
				sender.sendMessage(colorize("&9You have already accepted the ender share."));
				return true;
			}
			else
			{
				//getting and setting needed values
				p2 = pf1.getString(shareReceived);
				pf2 = PDPmain.getOfflinePlayerYaml(p2);
			}
			
			//setting info
			pf1.set(shareReceived, null);
			pf1.save();
			
			pf2.set(shareRequest, null);
			pf2.save();
			
			sender.sendMessage(colorize("&9You have denied sharing enderchests."));
			
			if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
				server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has denied your Ender Share Request!"));
			
			return true;
		}
		//end command for stopping a current share
		else if(cmd.getName().equalsIgnoreCase("enderend"))
		{
			if(args.length != 0)
			{
				sender.sendMessage(colorize("&4You gave me too much information!"));
				return false;
			}
			
			//why would you end a share you dont have? Whatever, its here anyways
			if(pf1.getString(shared) == null)
			{
				sender.sendMessage(colorize("&4You do not have an ender share to end."));
				return true;
			}
			else
			{
				//getting and setting stuff
				p2 = pf1.getString(shared);
				pf2 = PDPmain.getOfflinePlayerYaml(p2);
			}
			
			//setting new infor
			pf1.set(shared, null);
			pf1.set(shareReceived, null);
			pf1.set(shareRequest, null);
			pf1.set(shareSelect, null);
			pf1.save();
			
			pf2.set(shared, null);
			pf2.set(shareReceived, null);
			pf2.set(shareRequest, null);
			pf2.set(shareSelect, null);
			pf2.save();
			
			sender.sendMessage(colorize("&9You have ended sharing enderchests."));
			
			if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
				server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has stopped Sharing Ender Chests!"));
			
			return true;
		}
		//for canceling sent requests
		else if(cmd.getName().equalsIgnoreCase("endercancel"))
		{
			if(args.length != 0)
			{
				sender.sendMessage(colorize("&4You gave me too much information!"));
				return false;
			}
			
			if(pf1.getString(shareRequest) == null) // Someone tried to cancel a request they never sent
			{
				sender.sendMessage(colorize("&4You do not have an share request sent."));
				return true;
			}
			else
			{
				//getting and setting stuff
				p2 = pf1.getString(shareRequest);
				pf2 = PDPmain.getOfflinePlayerYaml(p2);
			}
			
			//setting new info
			pf1.set(shareRequest, null);
			pf1.save();
			
			pf2.set(shareReceived, null);
			pf2.save();
			
			sender.sendMessage(colorize("&9You have canceled your share request."));
			
			if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
				server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has canceled their Ender Share Request!"));
			
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("enderswitch"))
		{
			if(args.length != 0)
			{
				sender.sendMessage(colorize("&4You gave me too much information!"));
				return false;
			}
			
			//YOU CAN'T SWITCH SOMETHING YOU HAVNT SHARED!!!!
			if(pf1.getString(shared) == null)
			{
				sender.sendMessage(colorize("&4You have not shared your enderchest with anyone."));
				return true;
			}
			else
			{
				//getting and setting
				p2 = pf1.getString(shared);
				pf2 = PDPmain.getOfflinePlayerYaml(p2);
			}
			
			//checking which view is active
			if(pf1.getString(shareSelect).equalsIgnoreCase(sender.getName()))
			{
				//if its your own set to other player
				pf1.set(shareSelect, p2);
				pf1.save();
				sender.sendMessage(colorize("&9You will now see &5" + p2 + "&9's enderchest."));
				return true;
			}
			else
			{
				//otherwise set to own
				pf1.set(shareSelect, sender.getName());
				pf1.save();
				sender.sendMessage(colorize("&9You will now see your own enderchest."));
				return true;
			}
		}
		//command allows players to view information about request status' or current shares
		else if(cmd.getName().equalsIgnoreCase("enderinfo"))
		{
			//checks if currently sharing, i actually want this for once
			if(pf1.get(shared) != null)
			{
				//says who you are sharing with
				sender.sendMessage(colorize("&9You are currently sharing your enderchest with &5" + p2 + "&9."));
				
				//checks selected chest and throws that in too
				if(pf1.getString(shareSelect).equalsIgnoreCase(p2))
					sender.sendMessage(colorize("&9You currently have &5" + p2 + "&9's enderchest selected."));
				else
					sender.sendMessage(colorize("&9You currently have &5your &9own enderchest selected."));
			}
			else
			{
				//if not sharing
				//check if player has request and tell them who or no
				if(pf1.get(shareReceived) != null)
					sender.sendMessage(colorize("&9You have received an EnderShare request from &5" + pf1.get(shareReceived) + "&9."));
				else
					sender.sendMessage(colorize("&9You have not received any EnderShare requests&9."));
				
				//checks if player has sent request, tells who or no
				if(pf1.get(shareRequest) != null)
					sender.sendMessage(colorize("&9You are awaiting &5" + pf1.get(shareReceived) + "&9 to accept your EnderShare request."));
				else
					sender.sendMessage(colorize("&9You have not sent any EnderShare requests&9."));
			}
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("enderhelp"))
		{
			showHelp();
			return true;
		}
		
		return false;
	}
	
	//////////////
	//Utilities //
	//////////////
	
	/**
	 * Utility method. Shortens {@link ChatColor#translateAlternateColorCodes(char, String)}
	 * @param msg the string to be coloured.
	 * @return the coloured string.
	 */
	public String colorize(String msg)
	{
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}
	
	/**
	 * Utility method. Checks twhether a string is an integer.
	 * @param str Some string
	 * @return true if it is a number.
	 */
	public static boolean isInt(String str)
	{
		try{
			Integer.parseInt(str);
			return true
		}catch(Exception e){
			return false;
		}
	}
	
	/**
	 * Serializes inventories.
	 * @param itemstackList the list of items to save.
	 * @param inventoryName the name of the inventory.
	 * @param playerName the owner's name.
	 */
	public void saveInv(ItemStack[] itemstackList, String inventoryName, String playerName)
	{
		Yaml p = PDPmain.getOfflinePlayerYaml(playerName);
		for (int i = 0; i < itemstackList.length; i++)
		{
			p.set("Inventory." + inventoryName + "." + i, itemstackList[i]);
		}
		p.save();
	}
	
	/**
	 * Loads serialized inventory data.
	 * @param inventoryName the name of the inventory.
	 * @param playerName the player's name.
	 * @return an array of {@link ItemStack}.
	 */
	public ItemStack[] loadInv(String inventoryName, String playerName)
	{
		Yaml p = PDPmain.getOfflinePlayerYaml(playerName);
		List<ItemStack> itemstackList = new ArrayList<ItemStack>();
		int i = 0, total = 0;
		if(p.getConfigurationSection("Inventory." + inventoryName).getKeys(false).size() > 0)
			for(String s : p.getConfigurationSection("Inventory." + inventoryName).getKeys(false))
			{
				if(isInt(s))
				{
					int n = Integer.parseInt(s);
					if(n > total) total = n;
				}
			}
		
		while (i <= total)
		{
			if (p.contains("Inventory."+inventoryName+"."+i))
				itemstackList.add(p.getItemStack("Inventory." + inventoryName + "." + i));
			else
				itemstackList.add(new ItemStack(Material.AIR));
			
			i++;
		}
		ItemStack[] toReturn = itemstackList.toArray(new ItemStack[itemstackList.size()]);
		return toReturn;
	}
	
	/**
	 * Shows the help menu.
	 */
	public void showHelp() {
		Server server = getServer();
		ConsoleCommandSender console = server.getConsoleSender();
		
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"Welcome to \",\"color\":\"blue\"},{\"text\":\"EnderHelp\",\"color\":\"dark_purple\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"An \",\"color\":\"blue\"},{\"text\":\"EnderShare\",\"color\":\"dark_purple\"},{\"text\":\" initiative!\",\"color\":\"blue\"}]}}},{\"text\":\"!\",\"color\":\"blue\"}]");
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"Hover over the commands to see what they do!\",\"color\":\"blue\"}]");
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"/endershare <playername>\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/endershare \"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Use to send a share request to another player!\",\"color\":\"blue\"}]}}}]");
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"/enderswitch\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/enderswitch\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Use to switch which enderchest you will open\",\"color\":\"blue\"}]}}}]");
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"/enderaccept\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/enderaccept\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Use to accept and EnderRequest\",\"color\":\"blue\"}]}}}]");
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"/enderdeny\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/enderdeny\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Use to deny an EnderRequest\",\"color\":\"blue\"}]}}}]");
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"/endercancel\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/endercancel\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Use to cancel a sent EnderRequest\",\"color\":\"blue\"}]}}}]");
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"/enderend\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/enderend\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Use to stop sharing your enderchest with someone\",\"color\":\"blue\"}]}}}]");
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"/enderinfo\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/enderinfo\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Use to show information about your EnderShares\",\"color\":\"blue\"}]}}}]");
		server.dispatchCommand(console, "tellraw " + sender.getName() + " [\"\",{\"text\":\"/enderhelp\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/enderhelp\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Use to show this information\",\"color\":\"blue\"}]}}}]");
	}
}