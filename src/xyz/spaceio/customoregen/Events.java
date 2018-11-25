package xyz.spaceio.customoregen;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerJoinEvent;


public class Events implements Listener {
	
	/*
	 * CustomOreGen main class
	 */
	private CustomOreGen plugin;
	
	public Events(CustomOreGen customOreGen) {
		this.plugin = customOreGen;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onFromTo(BlockFromToEvent event) {
		if (plugin.getDisabledWorlds().contains(event.getBlock().getLocation().getWorld().getName())) {
			return;
		}

		int id = this.getID(event.getBlock());

		if ((id >= 8) && (id <= 11) && event.getFace() != BlockFace.DOWN) {
			Block b = event.getToBlock();
			int toid = this.getID(b);
			Location fromLoc = b.getLocation();
			// fix for (lava -> water)
			if (id == 10 || id == 11) {
				if(!isSurroundedByWater(fromLoc)){
					return;
				}
			}

			if ((toid == 0 || toid == 9 || toid == 8 || toid == 10 || toid == 11) && (generatesCobble(id, b))) {
				OfflinePlayer p = plugin.getOwner(b.getLocation());
				if (p == null)
					return;
				GeneratorConfig gc = plugin.getGeneratorConfigForPlayer(p, event.getBlock().getWorld().getName());
				if (gc == null)
					return;
				if (getObject(gc) == null)
					return;
				GeneratorItem winning = getObject(gc);
				if (Material.getMaterial(winning.getName()) == null)
					return;

				if (Material.getMaterial(winning.getName()).equals(Material.COBBLESTONE) && winning.getDamage() == 0) {
					return;
				}
				event.setCancelled(true);
				//b.setType(Material.getMaterial(winning.getName()));
				// <Block>.setData(...) is deprecated, but there is no
				// alternative to it. #spigot
				if(Arrays.stream(event.getBlock().getClass().getMethods()).anyMatch(method -> method.getName() == "getTypeId")) {
					b.setTypeIdAndData(Material.getMaterial(winning.getName()).getId() , winning.getDamage(), true);
				}else {
					Bukkit.getScheduler().runTask(plugin, () -> {
						b.setType(Material.getMaterial(winning.getName()));
						b.getState().update(true);
						b.getWorld().playSound(b.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 10f);
					});
				}
				//b.setData(winning.getDamage(), true);
			}
		}

	}

	/**
	 * Checks if a block is surrounded by water
	 * @param fromLoc
	 * @return
	 */
	public boolean isSurroundedByWater(Location fromLoc) {
		Block[] blocks = {
				fromLoc.getWorld().getBlockAt(fromLoc.getBlockX() + 1, fromLoc.getBlockY(), fromLoc.getBlockZ()),
				fromLoc.getWorld().getBlockAt(fromLoc.getBlockX() - 1, fromLoc.getBlockY(), fromLoc.getBlockZ()),
				fromLoc.getWorld().getBlockAt(fromLoc.getBlockX(), fromLoc.getBlockY(), fromLoc.getBlockZ() + 1),
				fromLoc.getWorld().getBlockAt(fromLoc.getBlockX(), fromLoc.getBlockY(), fromLoc.getBlockZ() - 1) };

		for (Block b : blocks) {
			if (b.getType().toString().contains("WATER")) {
				return true;
			}
		}
		return false;

	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		plugin.getGeneratorConfigForPlayer(e.getPlayer(), e.getPlayer().getWorld().getName());
	}

	
	/**
	 * Chooses a GeneratorItem randomly
	 * @param gc
	 * @return
	 */
	public GeneratorItem getObject(GeneratorConfig gc) {

		Random random = new Random();
		double d = random.nextDouble() * 100;
		for (GeneratorItem key : gc.itemList) {
			if ((d -= key.getChance()) < 0)
				return key;
		}
		return new GeneratorItem("COBBLESTONE", (byte) 0, 0); // DEFAULT
	}

	private final BlockFace[] faces = { BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST,
			BlockFace.SOUTH, BlockFace.WEST };

	public boolean generatesCobble(int id, Block b) {
		int mirrorID1 = (id == 8) || (id == 9) ? 10 : 8;
		int mirrorID2 = (id == 8) || (id == 9) ? 11 : 9;
		for (BlockFace face : this.faces) {
			Block r = b.getRelative(face, 1);
			if ((this.getID(r) == mirrorID1) || (this.getID(r) == mirrorID2)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Method for getting the block id from a block
	 * @param b
	 * @return
	 */
	public int getID(Block b) {
	    if(Arrays.stream(b.getClass().getMethods()).anyMatch(method -> method.getName() == "getTypeId")) {
	    	return b.getTypeId();
	    }else {
	    	try {
	    		return Utils.Material113.valueOf(Utils.Material113.class, b.getType().name()).getID();
	    	} catch(IllegalArgumentException e) {
	    		return 2;
	    	}
	    }
	}
}
