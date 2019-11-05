package xyz.spaceio.customoregen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	
	private boolean useLegacyBlockPlaceMethod;
	private Method legacyBlockPlaceMethod;
	
	public Events(CustomOreGen customOreGen) {
		this.plugin = customOreGen;
		this.useLegacyBlockPlaceMethod = Arrays.stream(Block.class.getMethods()).anyMatch(method -> method.getName() == "setTypeIdAndData");
		if(this.useLegacyBlockPlaceMethod) {
			try {
				legacyBlockPlaceMethod = Block.class.getMethod("setTypeIdAndData", int.class , byte.class, boolean.class);
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onFromTo(BlockFromToEvent event) {
		if (plugin.getDisabledWorlds().contains(event.getBlock().getLocation().getWorld().getName())) {
			return;
		}

		Type fromType = this.getType(event.getBlock());
		
		if (fromType != null && event.getFace() != BlockFace.DOWN) {
			Block b = event.getToBlock();
			Type toType = this.getType(event.getToBlock());
			
			Location fromLoc = b.getLocation();
			
			// fix for (lava -> water)
			if (fromType == Type.LAVA || fromType == Type.LAVA_STAT) {
				if(!isSurroundedByWater(fromLoc)){
					return;
				}
			}

			if (toType != null || b.getType() == Material.AIR && (generatesCobble(fromType, b))) {
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
				if(useLegacyBlockPlaceMethod) {
					try {
						legacyBlockPlaceMethod.invoke(b, Material.getMaterial(winning.getName()).getId() , winning.getDamage(), true);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
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

	private BlockFace[] blockFaces = { BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH };
	
	/**
	 * Checks if a block is surrounded by water
	 * @param fromLoc
	 * @return
	 */
	public boolean isSurroundedByWater(Location fromLoc) {

		for(BlockFace blockFace : blockFaces) {
			if(this.getType(fromLoc.getBlock().getRelative(blockFace)) == Type.WATER || this.getType(fromLoc.getBlock().getRelative(blockFace)) == Type.WATER_STAT) {
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
	
	private enum Type {
		WATER, WATER_STAT, LAVA, LAVA_STAT
	}
	
	public Type getType(Block b) {
		try {
			Class.forName("org.bukkit.block.data.Levelled");
			if(b.getBlockData() != null && b.getBlockData() instanceof org.bukkit.block.data.Levelled) {
				org.bukkit.block.data.Levelled level = (org.bukkit.block.data.Levelled) b.getBlockData();
				if(level.getLevel() == 0) {
					if(level.getMaterial() == Material.WATER) {
						return Type.WATER_STAT;
					}else {
						return Type.LAVA_STAT;
					}
				}else {
					if(level.getMaterial() == Material.WATER) {
						return Type.WATER;
					}else {
						return Type.LAVA;
					}
				}
			}
		} catch( ClassNotFoundException e ) {
			switch(b.getType().name()) {
				case "WATER":
					return Type.WATER;
				case "STATIONARY_WATER":
					return Type.WATER_STAT;
				case "LAVA":
					return Type.LAVA;
				case "STATIONARY_LAVA":
					return Type.LAVA_STAT;
			}
		}
		return null;
	}

	private final BlockFace[] faces = { BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST,
			BlockFace.SOUTH, BlockFace.WEST };

	public boolean generatesCobble(Type type, Block b) 
	{
		Type mirrorType1 = (type == Type.WATER_STAT) || (type == Type.WATER) ? Type.LAVA_STAT : Type.WATER_STAT;
		Type mirrorType2 = (type == Type.WATER_STAT) || (type == Type.WATER) ? Type.LAVA : Type.WATER;
		for (BlockFace face : this.faces) {
			Type r = this.getType(b.getRelative(face, 1));
			if (r == mirrorType1 || r == mirrorType2) {
				return true;
			}
		}
		return false;
	}
}
