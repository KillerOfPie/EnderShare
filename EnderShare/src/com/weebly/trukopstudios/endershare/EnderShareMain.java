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
    //PDP storage values that will be sued often
	String shared = "EC.Shared", shareRequest = "EC.ShareRequest", shareReceived = "EC.ShareReceived", shareSelect = "EC.SelectedChest";
    //Permission node(s)
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
	    //No commands support console soooo ya
        if(!(sender instanceof Player))
        {
            sender.sendMessage(colorize("&4You must be a player to use that command!"));
            return true;
        }
        
        //command allows players with perm to see and edit offline players ender chests
        if(cmd.getName().equalsIgnoreCase("OfflineEnderChest"))
        {
            //should have one arg which is player name
            if(args.length == 1)
            {
                //setting arg because I'm lazy
                String ptemp = args[0];
                //checking if player exists in PDP "database"
                if(PDPmain.playerExist(ptemp))
                {
                    //setting sender to player so I can open enderchest in a bit
                    Player tempp = (Player) sender;
                    
                    //creating inventory of type enderchest and with special name I'll check for to save inventories
                    Inventory chest = Bukkit.createInventory(tempp, InventoryType.ENDER_CHEST, ptemp + "'s Ender Chest");
                    //setting contents of enderchest from file
                    chest.setContents(loadInv("Enderchest", ptemp));
                    //opening inventory for sender
                    tempp.openInventory(chest);
                    return true;
                } else {
                    //the player doesn't exist 
                    sender.sendMessage(colorize("&4I cannot find that player!"));
                    return true;
                }
            } else {
                //no args, need player name(guess this also triggers if there is too many...maybe i should put a different message for that)
                sender.sendMessage(colorize("&4I need a players name to do that!"));
                return false;
            }
        }
        
        //making strings for playernames and Yaml for loading player 
        String p1 = sender.getName(), p2 = null;
        Yaml pf1 = PDPmain.getOfflinePlayerYaml(p1), pf2 = null;
        
        //If the player has a person sharing enderchests with them
        if(pf1.get(shared) != null)
        {
            //grabbing the playername from file
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

        //making a server objest cuz i use it a couple times and ya know...lazy and all
        Server server = Bukkit.getServer();
        
        //endershare command, used for requesting to share chests
		if(cmd.getName().equalsIgnoreCase("endershare"))
		{
		    //making shure we have a player name
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
	            
	        //send message because we want people to know when things worked right?
	        sender.sendMessage(colorize("&9You have successfully sent a request!"));
	        
	        //if player 2 is online send them a message so the know too right?
	        //if they are not online we'll send them a message whenever they join
	        if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
	           server.getPlayer(p2).sendMessage(colorize("&9You have recieved an Ender Share Request from &5" + p1 + "&9!")); 
	        
            return true;
		}
		//accept command for accepting a share request
		else if(cmd.getName().equalsIgnoreCase("enderaccept"))
        {
		    //because i'm and ass and don't want people inputting more then I want
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
            
            //success message because we're nice?
            sender.sendMessage(colorize("&9You have successfully shared enderchests."));
            
            //again checking if other player is online to send message otherwise they'll get it when they next join
            if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
                server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has accepted your Ender Share Request!"));
            
            return true;  
        }
		//same as above except the no version
        else if(cmd.getName().equalsIgnoreCase("enderdeny"))
        {
            //im an ass, checking for oversharing
            if(args.length != 0)
            {
                sender.sendMessage(colorize("&4You gave me too much information!"));
                return false; 
            }
            
            //checking for ID10T error
            if(pf1.getString(shareReceived) == null)
            {
                sender.sendMessage(colorize("&4You do not have an ender share to deny."));
                return true;
            }
            //why would you deny after accepting? IDFK try asking the person that may actually hit this.
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
            
            //sending message so dem peeps know
            sender.sendMessage(colorize("&9You have denied sharing enderchests."));
            
            //checking for other player and being nice?
            if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
                server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has denied your Ender Share Request!"));
            
            return true;  
        }
		//end command for stopping a current share
        else if(cmd.getName().equalsIgnoreCase("enderend"))
        {
            //can they just not overshare? maybe if everyone used things exactly as intended i could not use these 5 lines over again -_-
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
            
            //notifying
            sender.sendMessage(colorize("&9You have ended sharing enderchests."));
            
            //notifying other if online because im just so nice right?
            if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
                server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has stopped Sharing Ender Chests!"));
            
            return true;  
        }
		//for canceling sent requests
        else if(cmd.getName().equalsIgnoreCase("endercancel"))
        {
            //STOP just STOP it i dont want this shit take it back okay?!?!?!?
            if(args.length != 0)
            {
                sender.sendMessage(colorize("&4You gave me too much information!"));
                return false; 
            }
            
            //why would someone cancel a request they never sent? IDFK. Would you leave a building you never entered?
            if(pf1.getString(shareRequest) == null)
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
            
            //notifying
            sender.sendMessage(colorize("&9You have canceled your share request."));
            
            //im super nice right?
            if(server.getOnlinePlayers().contains(server.getPlayer(p2)))
                server.getPlayer(p2).sendMessage(colorize("&5" + sender.getName() + "&9 has canceled their Ender Share Request!"));
            
            return true;  
        }
		//command to allow players to swicth the enderchest they are going to open
        else if(cmd.getName().equalsIgnoreCase("enderswitch"))
        {
            //I
            //Don't
            //Want
            //This
            //Shit
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
		//yaaaaa help menu
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
	    //Opens players PlayerDataPlus config file
	    Yaml p = PDPmain.getOfflinePlayerYaml(playerName);
        //Runs thru every item in the list
        for (int i = 0; i < itemstackList.length; i++) 
        {
            //Saves the itemstack
            p.set("Inventory." + inventoryName + "." + i, itemstackList[i]);
        }
        //Saves the config
        p.save();
    }
	
	//for loading inventories from file, funny story tho got it online and it stopped loading whenever there was a gap and caused some enderchest losses in a live test..Whoops
    public ItemStack[] loadInv(String inventoryName, String playerName)
    {
        //Opens players PlayerDataPlus config file
        Yaml p = PDPmain.getOfflinePlayerYaml(playerName);
        //Creates a blank list
        List<ItemStack> itemstackList = new ArrayList<ItemStack>();
        
        //Preparing
        int i = 0, total = 0;
        
        //Checks amount of items in inventory
        if(p.getConfigurationSection("Inventory." + inventoryName).getKeys(false).size() > 0)
            for(String s : p.getConfigurationSection("Inventory." + inventoryName).getKeys(false))
            {
                //checks if the section is an int(should be the item location in the inventory
                if(isInt(s))
                {
                    int n = Integer.parseInt(s);
                    //makes total the highest inventory location
                    if(n > total)
                        total = n;
                }
            }
        
        //Begin loading
        while (i <= total) 
        {
            //Checks if the config contains that slot
            if (p.contains("Inventory."+inventoryName+"."+i)) 
                //Adds the itemstack to the list
                itemstackList.add(p.getItemStack("Inventory." + inventoryName + "." + i));
            else
                //If it doesnt exist adds air to prevent EC crunching
                itemstackList.add(new ItemStack(Material.AIR));
                
            i++;
        }
        //Some converting and returning
        ItemStack[] toReturn = itemstackList.toArray(new ItemStack[itemstackList.size()]);
        
        return toReturn;
    }
}
