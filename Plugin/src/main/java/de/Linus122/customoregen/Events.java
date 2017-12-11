package de.Linus122.customoregen;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

import java.util.Random;

public class Events implements Listener {
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onCobbleGen(BlockFormEvent event) {
		if (Main.disabledWorlds.contains(event.getBlock().getLocation().getWorld().getName())) {
			return;
		}
		
		Material newBlock = event.getNewState().getType();
		Block b = event.getBlock();
		
		//System.out.println("From: " + event.getBlock().getType().name());
		//System.out.println("To: " + newBlock.name());
		
		if (newBlock.equals(Material.COBBLESTONE) || newBlock.equals(Material.STONE)) {
			GeneratorConfig gc = null;
			
			Player p = Main.getOwner(b.getLocation());
			if (p == null) {
				gc = Main.generatorConfigs.get(0);
			} else {
				int islandLevel = Main.getLevel(p);
				
				if (Main.activeInWorld.getName().equals(b.getWorld().getName())) {
					for (GeneratorConfig gc2 : Main.generatorConfigs) {
						if (gc2 == null) {
							continue;
						}
						if ((p.hasPermission(gc2.permission) || gc2.permission.length() == 0) && islandLevel >= gc2.unlock_islandLevel) {
							// Weiter
							gc = gc2;
						}
						
					}
				}
			}
			if (gc == null)
				return;
			if (getObject(gc) == null)
				return;
			GeneratorItem winning = getObject(gc);
			if (Material.getMaterial(winning.name) == null)
				return;
			
			if (Material.getMaterial(winning.name).equals(Material.COBBLESTONE) && winning.damage == 0) {
				return;
			}
			event.setCancelled(true);
			b.setTypeIdAndData(Material.getMaterial(winning.name).getId(), winning.damage, true);
		}
	}
	
	
	public GeneratorItem getObject(GeneratorConfig gc) {
		
		Random random = new Random();
		double d = random.nextDouble() * 100;
		for (GeneratorItem key : gc.itemList) {
			if ((d -= key.chance) < 0)
				return key;
		}
		return new GeneratorItem("COBBLESTONE", (byte) 0, 0); // DEFAULT
	}
}
