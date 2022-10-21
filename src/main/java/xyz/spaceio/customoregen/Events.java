package xyz.spaceio.customoregen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
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
	private boolean useLevelledClass;
	private Method legacyBlockPlaceMethod;
	
	private boolean enableStoneGenerator;
	private boolean enableSoundEffect;
	private boolean enableParticleEffect;
	
	public Events(CustomOreGen customOreGen) {
		this.plugin = customOreGen;
		load();
	}
	
	public void load() {
		this.useLegacyBlockPlaceMethod = Arrays.stream(Block.class.getMethods()).anyMatch(method -> method.getName() == "setTypeIdAndData");
		if(this.useLegacyBlockPlaceMethod) {
			try {
				legacyBlockPlaceMethod = Block.class.getMethod("setTypeIdAndData", int.class , byte.class, boolean.class);
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.enableStoneGenerator = plugin.getConfig().getBoolean("enable-stone-generator");
		this.enableSoundEffect = plugin.getConfig().getBoolean("enable-sound-effect", false);
		this.enableParticleEffect = plugin.getConfig().getBoolean("enable-particle-effect", false);

		try {
			Class.forName("org.bukkit.block.data.Levelled");
			useLevelledClass = true;
		} catch( ClassNotFoundException e ) {
			useLevelledClass = false;
		}
		
		if(enableParticleEffect) {
			try {
				Class.forName("org.bukkit.Particle");
			} catch (ClassNotFoundException e) {
				this.plugin.getLogger().info(
						String.format("Particle effects are not supported for your bukkit version, disable 'enable-particle-effect' in %s/config.yml to get rid of this message.", this.plugin.getDataFolder().getPath()));
				this.enableParticleEffect = false;
			}
		}
		
		if(enableSoundEffect) {
			// disabling sound effects when enum value not present
			enableSoundEffect = Arrays.asList(Sound.values()).stream().map(Sound::name).anyMatch(s -> s.equals("BLOCK_FIRE_EXTINGUISH"));

		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onFromTo(BlockFromToEvent event) {
		if (plugin.getDisabledWorlds().contains(event.getBlock().getLocation().getWorld().getName())) {
			return;
		}
		
		Type fromType = this.getType(event.getBlock());

		if (fromType != null) {
			if (!enableStoneGenerator) {
				if(event.getFace() == BlockFace.DOWN || fromType == Type.WATER_STAT) {
					return;
				}
			}
			Block b = event.getToBlock();
			Type toType = this.getType(event.getToBlock());
			
			Location fromLoc = b.getLocation();
			
			// fix for (lava -> water)
			if (fromType == Type.LAVA || fromType == Type.LAVA_STAT) {
				if(!isSurroundedByWater(fromLoc)){
					return;
				}
			}
			
			if ((toType != null || b.getType() == Material.AIR) && (this.generatesCobble(fromType, b))) {
				
				if (this.getTouchingFace(fromType, b) == BlockFace.DOWN) {
					b = b.getLocation().add(0, -1, 0).getBlock();
				} else {
					event.setCancelled(true);
				}
				
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
						b.setType(Material.getMaterial(winning.getName()));
						b.getState().update(true);
				}
				
				if(enableSoundEffect)
					b.getWorld().playSound(b.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.6f + ((float) Math.random() - (float) Math.random()) * 0.8f);
				if(enableParticleEffect)
					b.getWorld().spawnParticle(Particle.SMOKE_LARGE, b.getLocation().getBlockX() + 0.5D, b.getLocation().getBlockY() + 0.25D, b.getLocation().getBlockZ() + 0.5D, 8, 0.5D, 0.25D, 0.5D, 0.0D);
				
				plugin.getSkyblockAPICached().sendBlockAcknowledge(b);
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
		if(useLevelledClass) {
			if(b.getBlockData() != null && b.getBlockData() instanceof org.bukkit.block.data.Levelled) {
				org.bukkit.block.data.Levelled level = (org.bukkit.block.data.Levelled) b.getBlockData();
				if(level.getLevel() == 0) {
					if(level.getMaterial() == Material.WATER) {
						return Type.WATER_STAT;
					}else if(level.getMaterial() == Material.LAVA) {
						return Type.LAVA_STAT;
					}
				}else {
					if(level.getMaterial() == Material.WATER) {
						return Type.WATER;
					}else if(level.getMaterial() == Material.LAVA) {
						return Type.LAVA;
					}
				}
			}
		} else {
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
	
	public BlockFace getTouchingFace(Type type, Block b) {
		Type mirrorType1 = (type == Type.WATER_STAT) || (type == Type.WATER) ? Type.LAVA_STAT : Type.WATER_STAT;
		Type mirrorType2 = (type == Type.WATER_STAT) || (type == Type.WATER) ? Type.LAVA : Type.WATER;
		for (BlockFace face : this.faces) {
			Block r = b.getRelative(face, 1);
			if ((this.getType(r) == mirrorType1) || (this.getType(r) == mirrorType2)) {
				return face;
			}
		}
		return null;
	}
	public boolean generatesCobble(Type type, Block b) {
		if (this.getTouchingFace(type, b) != null) {
			return true;
		}
		
		return false;
	}
}