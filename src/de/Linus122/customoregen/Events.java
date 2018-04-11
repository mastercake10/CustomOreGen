package de.Linus122.customoregen;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onCobbleGen(BlockFormEvent event) {
		if (Main.disabledWorlds.contains(event.getBlock().getLocation().getWorld().getName())) {
			return;
		}

		Material newBlock = event.getNewState().getType();
		Block b = event.getBlock();
		
		if (newBlock.equals(Material.COBBLESTONE) || newBlock.equals(Material.STONE)) {

			OfflinePlayer p = Main.getOwner(b.getLocation());
			if(p == null) return;
			GeneratorConfig gc = Main.getGeneratorConfigForPlayer(p);
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
			b.setType(Material.getMaterial(winning.name));
			b.setData(winning.damage, true);
		}
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		Main.getGeneratorConfigForPlayer(e.getPlayer());
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
