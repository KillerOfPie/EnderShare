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

public class EShareListener implements Listener
{
    EnderShareMain plugin;
	String shared = "EC.Shared", shareRequest = "EC.ShareRequest", shareReceived = "EC.ShareReceived", shareSelect = "EC.SelectedChest";
	
	public EShareListener(EnderShareMain plugin)
	{
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	//join event for loading ec from file or saving too file if not done already
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
	    Player p = e.getPlayer();
	    
	    Yaml pf = PDPmain.getOfflinePlayerYaml(p.getName());
        pf.add(shared, null);
        pf.add(shareReceived, null);
        pf.add(shareRequest, null);
        pf.add(shareSelect, p.getName());
        pf.save();
        
        if(pf.getString(shared) != null)
            p.sendMessage(colorize("&9You are sharing Ender Chests with &5" + pf.getString(shared) + "&9!")); 
        else if(pf.getString(shareReceived) != null)
            p.sendMessage(colorize("&9You have recieved an Ender Share Request from &5" + pf.getString(shareReceived) + "&9!")); 
        else
            p.sendMessage(colorize("&9You are not sharing Ender Chests with anyone&9!")); 
       
        if(plugin.loadInv("Enderchest", p.getName()) == null && p.getEnderChest().getContents().length > 0)
        {
            plugin.saveInv(e.getPlayer().getEnderChest().getContents(), "Enderchest", e.getPlayer().getName());
        }
        else
        {
            p.getEnderChest().setContents(plugin.loadInv("Enderchest", e.getPlayer().getName())); 
        }
	}

	//checking for enderchest opening(maybe do this on an inventory open event rather the interect?)
    @EventHandler
    public void onECopen(PlayerInteractEvent e)
    {
        if(e.getAction() != null)  
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
                if(e.getClickedBlock().getType() == Material.ENDER_CHEST) 
                {
                    Player p = e.getPlayer(); 
                    Yaml pf = PDPmain.getOfflinePlayerYaml(p.getName()); 
                    Inventory chest = null;
                    Server server = Bukkit.getServer();
                    String p2 = pf.getString(shareSelect);
                    
                    if(server.getPlayerExact(p2) != null)
                    {
                        Player target = server.getPlayer(p2);
                        chest = target.getEnderChest();
                    }
                    else
                    {
                        chest = Bukkit.createInventory(p, InventoryType.ENDER_CHEST, p2 + "'s Ender Chest");
                        chest.setContents(plugin.loadInv("Enderchest", p2));
                    }
                    
                    e.setCancelled(true);
                    p.openInventory(chest);
                }
    }
    
    //just saving ec to file when the player leaves
    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        plugin.saveInv(e.getPlayer().getEnderChest().getContents(), "Enderchest", e.getPlayer().getName());
    }
    
    //trigger on every inventory click(for conbstant saving to prevent duplicating items
    @EventHandler
    public void onECEdit(InventoryClickEvent e)
    {
        if(e.getInventory().getType() == InventoryType.ENDER_CHEST && e.getCurrentItem() != null && (e.getClickedInventory().getType() == InventoryType.ENDER_CHEST || e.isShiftClick()))
            if(e.getInventory().getName().contains("'s Ender Chest"))
                plugin.saveInv(e.getInventory().getContents(), "Enderchest", e.getInventory().getName().replace("'s Ender Chest", ""));
    }
    
    //trigger on inventory close, am i just saying the obvious stuff at this point?
    @EventHandler
    public void onECClose(InventoryCloseEvent e)
    {
         if(e.getInventory().getType() == InventoryType.ENDER_CHEST)
            if(e.getInventory().getName().contains("'s Ender Chest"))
                plugin.saveInv(e.getInventory().getContents(), "Enderchest", e.getInventory().getName().replace("'s Ender Chest", ""));
    }
    
    //replaces &x with color codes to allow colored text --- only here because im to lazy to do 'plugin.'
    public String colorize(String msg)
    {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        return msg;
    }
}
