package com.weebly.trukopstudios.endershare;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.weebly.trukopstudios.PlayerDataPlus.PDPmain;
import com.weebly.trukopstudios.PlayerDataPlus.Yaml;

public class EnderShareMain extends JavaPlugin 
{
	Plugin plugin;
	String shared = "EC.Shared", shareRequest = "EC.ShareRequest", shareReceived = "EC.ShareReceived", shareSelect = "EC.SelectedChest";
    String ECOperm = "EnderShare.OEC";
	
	@Override
	public void onEnable()
	{
		this.saveDefaultConfig();
		new EShareListener(this);
		plugin = this;
		getLogger().info("EnderShare Enabled!--------------");
	}
	
	@Override
	public void onLoad() 
	{
		getLogger().info("EnderShare Loaded!--------------");
	}
	
	@Override
	public void onDisable()
	{
		getLogger().info("EnderShare Disabled!--------------");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
        if(!(sender instanceof Player))
        {
            sender.sendMessage(colorize("&4You must be a player to use that command!"));
            return true;
        }
        
        //command allows players with perm to see and edit offline players ender chests
        if(cmd.getName().equalsIgnoreCase("OfflineEnderChest"))
        {
            if(args.length == 1)
            {
                String ptemp = args[0];
                if(PDPmain.playerExist(ptemp))
                {
                    Player tempp = (Player) sender;
                    
                    Inventory chest = Bukkit.createInventory(tempp, InventoryType.ENDER_CHEST, ptemp + "'s Ender Chest");
                    chest.setContents(loadInv("Enderchest", ptemp));
                    tempp.openInventory(chest);
                    return true;
                } else {
                    sender.sendMessage(colorize("&4I cannot find that player!"));
                    return true;
                }
            } else {
                sender.sendMessage(colorize("&4I need a players name to do that!"));
                return false;
            }
        }
        
        String p1 = sender.getName(), p2 = null;
        Yaml pf1 = PDPmain.getOfflinePlayerYaml(p1), pf2 = null;
        
        if(pf1.get(shared) != null)
        {
            p2 = pf1.getString(shared);
        }
        else if(args.length == 1)
        {
            String name = args[0];
            
            if(PDPmain.playerExist(name))
                p2 = name;
            else
            {
                sender.sendMessage(colorize("&4That's not a user!"));
                return false;
            }
        }
        
        if(p2 != null)
        {
            pf2 = PDPmain.getOfflinePlayerYaml(p2);

            pf2.add(shared, null);
            pf2.add(shareReceived, null);
            pf2.add(shareRequest, null);
            pf2.add(shareSelect, null);
            pf1.save();
        }
        
        pf1.add(shared, null);
        pf1.add(shareReceived, null);
        pf1.add(shareRequest, null);
        pf1.add(shareSelect, null);
        pf1.save();

        Server server = Bukkit.getServer();
        
        //endershare command, used for requesting to share chests
		if(cmd.getName().equalsIgnoreCase("endershare"))
		{
		    if(pf2 == null)
		    {
		        sender.sendMessage(colorize("&4I need another player's name before I can send a request."));
		        return false;
		    }
		    
	        if(pf1.get(shared) != null)
	        {
	            sender.sendMessage(colorize("&4You are already sharing your enderchest with &5" + p2 + "&4!"));
	            return false;
	        }
	        
	        if(pf2.get(shared) != null)
            {
                sender.sendMessage(colorize("&5" + p2 + "&4 is already sharing their enderchest!"));
                return false;
            }
	        
	        if(pf1.getString(shareRequest) != null)
	        {
	            sender.sendMessage(colorize("&4You already have a request out, please cancel that request before making a new one."));
                return true;
	        }
	        
	        if(pf2.getString(shareReceived) != null)
            {
                sender.sendMessage(colorize("&4That player already has a pending request, please ask them to handle that request."));
                return true;
            }
	        
	        pf1.set(shareRequest, p2);
	        pf1.save();
            
	        pf2.set(shareReceived, p1);
	        pf2.save();
	            
	        sender.sendMessage(colorize("&9You have successfully sent a request!"));
	        
	        if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
	           server.getPlayer(p2).sendMessage(colorize("&9You have recieved an Ender Share Request from &5" + p1 + "&9!")); 
	        
            return true;
		}
		//accept command for accepting a share request
		else if(cmd.getName().equalsIgnoreCase("enderaccept"))
        {
		    if(args.length != 0)
            {
		        sender.sendMessage(colorize("&4You gave me too much information!"));
                return false; 
            }
            
		    if(pf1.getString(shareReceived) == null)
            {
                sender.sendMessage(colorize("&4You do not have an ender share to accept."));
                return true;
            }
            else if(pf1.get(shared) == (pf1.get(shareReceived)))
            {
                sender.sendMessage(colorize("&9You have already accepted the ender share."));
                return true;
            }
            else
            {
                p2 = pf1.getString(shareReceived);
                pf2 = PDPmain.getOfflinePlayerYaml(p2); 
            }
            
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
            
            if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
                server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has accepted your Ender Share Request!"));
            
            return true;  
        }
		//same as above except the no version
        else if(cmd.getName().equalsIgnoreCase("enderdeny"))
        {
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
            else if(pf1.get(shared) == (pf1.get(shareReceived)))
            {
                sender.sendMessage(colorize("&9You have already accepted the ender share."));
                return true;
            }
            else
            {
                p2 = pf1.getString(shareReceived);
                pf2 = PDPmain.getOfflinePlayerYaml(p2); 
            }
            
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
            
            if(pf1.getString(shared) == null)
            {
                sender.sendMessage(colorize("&4You do not have an ender share to end."));
                return true;
            }
            else
            {
                p2 = pf1.getString(shared);
                pf2 = PDPmain.getOfflinePlayerYaml(p2); 
            }
            
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
            
            if(pf1.getString(shareRequest) == null)
            {
                sender.sendMessage(colorize("&4You do not have an share request sent."));
                return true;
            }
            else
            {
                p2 = pf1.getString(shareRequest);
                pf2 = PDPmain.getOfflinePlayerYaml(p2); 
            }
            
            pf1.set(shareRequest, null);
            pf1.save();
            
            pf2.set(shareReceived, null);
            pf2.save();
            
            sender.sendMessage(colorize("&9You have canceled your share request."));
            
            if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
                server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has canceled their Ender Share Request!"));
            
            return true;  
        }
		//command to allow players to switch the enderchest they are going to open
        else if(cmd.getName().equalsIgnoreCase("enderswitch"))
        {
            if(args.length != 0)
            {
                sender.sendMessage(colorize("&4You gave me too much information!"));
                return false; 
            }
            
            if(pf1.getString(shared) == null)
            {
                sender.sendMessage(colorize("&4You have not shared your enderchest with anyone."));
                return true;
            }
            else
            {
                p2 = pf1.getString(shared);
                pf2 = PDPmain.getOfflinePlayerYaml(p2); 
            }
            
            if(pf1.getString(shareSelect).equalsIgnoreCase(sender.getName()))
            {
                pf1.set(shareSelect, p2);
                pf1.save(); 
                sender.sendMessage(colorize("&9You will now see &5" + p2 + "&9's enderchest."));
                return true;
            }
            else
            {
                pf1.set(shareSelect, sender.getName());
                pf1.save(); 
                sender.sendMessage(colorize("&9You will now see your own enderchest."));
                return true;
            } 
        }
		//command allows players to view information about request status' or current shares
        else if(cmd.getName().equalsIgnoreCase("enderinfo"))
        {
             if(pf1.get(shared) != null)
            {
                sender.sendMessage(colorize("&9You are currently sharing your enderchest with &5" + p2 + "&9."));
                
                if(pf1.getString(shareSelect).equalsIgnoreCase(p2))
                    sender.sendMessage(colorize("&9You currently have &5" + p2 + "&9's enderchest selected."));
                else
                    sender.sendMessage(colorize("&9You currently have &5your &9own enderchest selected."));
            }
            else
            {
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
            //Json help menu for hover descriptions and click suggest
            CommandSender console = server.getConsoleSender();
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
            return true;
        }
		
		return false;
 	}
	
			//////////////
			//Utilities //
			//////////////
	
	//replaces &x with color codes to allow colored text because im lazy and dont want to do it the long way every time
	public String colorize(String msg)
	{
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}
	
	//checks to see if a string is an integer, IDK if i use this in this plugin but its here
	public static boolean isInt(String str)
	{
		try{
			Integer.parseInt(str);
		}catch(Exception e){
			return false;
		}
		
		return true;
	}
	
	//for saving inventories to file
	public void saveInv(ItemStack[] itemstackList, String inventoryName, String playerName) 
	{
	    Yaml p = PDPmain.getOfflinePlayerYaml(playerName);
        for (int i = 0; i < itemstackList.length; i++) 
        {
             p.set("Inventory." + inventoryName + "." + i, itemstackList[i]);
        }
         p.save();
    }
	
	//for loading inventories from file, funny story tho got it online and it stopped loading whenever there was a gap and caused some enderchest losses in a live test..Whoops
    public ItemStack[] loadInv(String inventoryName, String playerName)
    {
        try{
        Yaml p = PDPmain.getOfflinePlayerYaml(playerName);
        List<ItemStack> itemstackList = new ArrayList<ItemStack>();
        
        int i = 0, total = 0;
        
        if(p.getConfigurationSection("Inventory." + inventoryName).getKeys(false).size() > 0)
            for(String s : p.getConfigurationSection("Inventory." + inventoryName).getKeys(false))
            {
               if(isInt(s))
                {
                    int n = Integer.parseInt(s);
                    if(n > total)
                        total = n;
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
        }catch(NullPointerException e){
            return null;
        }
        
    }
}