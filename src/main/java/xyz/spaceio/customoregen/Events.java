package xyz.spaceio.customoregen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;


public class Events implements Listener {
	
	/*
	 * CustomOreGen main class
	 */
	private CustomOreGen plugin;
	
	private boolean useLegacyBlockPlaceMethod;
	private boolean useLevelledClass;
	private Method legacyBlockPlaceMethod;
	
	private boolean enableStoneGenerator;
	private Optional<Sound> soundEffect = Optional.empty();
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
		
		if(plugin.getConfig().getBoolean("enable-sound-effect", false)) {
			// disabling sound effects when enum value not present
			soundEffect = Arrays.stream(Sound.values()).filter(s -> {
				return s.name().equals("BLOCK_FIRE_EXTINGUISH") || s.name().equals("FIZZ");
			}).findAny();
		}
	}

	/**
	 *	This is used in minecraft versions >= 1.12 and does nothing on other version.
	 */
	@EventHandler
	public void blockFormEvent(BlockFormEvent event) {
		if (plugin.getDisabledWorlds().contains(event.getBlock().getLocation().getWorld().getName())) {
			return;
		}
		if(event.getNewState().getType().equals(Material.COBBLESTONE)
				|| enableStoneGenerator && event.getNewState().getType().equals(Material.STONE)) {
			event.setCancelled(true);

			GeneratorConfig generatorConfig = this.getGeneratorConfigAtLocation(event.getBlock().getLocation());
			if (generatorConfig != null) {
				GeneratorItem generatorItem = generatorConfig.getRandomItem();
				Material material = Material.getMaterial(generatorItem.getName());

				if(material != null) {
					// set actual block

					placeBlock(event.getBlock(), material, generatorItem.getDamage());
				}
			}
		}
	}

	/**
	 *	Used for older mc versions
	 */
	@EventHandler
	public void onBlockFromToEvent(BlockFromToEvent event) {
		if(this.isGenerator(event)) {
			event.setCancelled(true);
			GeneratorConfig generatorConfig = this.getGeneratorConfigAtLocation(event.getBlock().getLocation());
			if (generatorConfig != null) {
				GeneratorItem generatorItem = generatorConfig.getRandomItem();
				Material material = Material.getMaterial(generatorItem.getName());

				if(material != null) {
					// set actual block

					placeBlock(event.getToBlock(), material, generatorItem.getDamage());
				}
			}
			event.getBlock().getState().update();
		}
	}

	private void placeBlock(Block block, Material material, byte damage) {
		if(useLegacyBlockPlaceMethod) {
			try {
				legacyBlockPlaceMethod.invoke(block, material.getId(), damage, true);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			block.setType(material);
			block.getState().update(true);
		}

		soundEffect.ifPresent(sound -> block.getWorld().playSound(block.getLocation(),
				sound,
				0.5f, 2.6f + ((float) Math.random() - (float) Math.random()) * 0.8f));

		if(enableParticleEffect) {
			block.getWorld().spawnParticle(Particle.SMOKE_LARGE,
					block.getLocation().getBlockX() + 0.5D,
					block.getLocation().getBlockY() + 0.25D,
					block.getLocation().getBlockZ() + 0.5D,
					8, 0.5D, 0.25D, 0.5D, 0.0D);
		}

		plugin.getSkyblockAPICached().sendBlockAcknowledge(block);
	}

	@Nullable
	private GeneratorConfig getGeneratorConfigAtLocation(Location location) {
		OfflinePlayer player = plugin.getApplicablePlayer(location);
		if (player == null)
			return null;
		GeneratorConfig gc = plugin.getGeneratorConfigForPlayer(player, location.getWorld().getName());

		if (gc == null)
			return null;

		return gc;
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

	private enum Type {
		WATER, WATER_STAT, LAVA, LAVA_STAT
	}
	
	private Type getType(Block b) {
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

	private boolean generatesCobble(Type type, Block b) {
		Type mirrorType1 = (type == Type.WATER_STAT) || (type == Type.WATER) ? Type.LAVA_STAT : Type.WATER_STAT;
		Type mirrorType2 = (type == Type.WATER_STAT) || (type == Type.WATER) ? Type.LAVA : Type.WATER;
		for (BlockFace face : this.faces) {
			Block r = b.getRelative(face, 1);
			if ((this.getType(r) == mirrorType1) || (this.getType(r) == mirrorType2)) {
				return true;
			}
		}
		return false;
	}

	private boolean isGenerator(BlockFromToEvent event) {
		Type type = this.getType(event.getBlock());

		if (type != null && (!type.equals(Type.WATER_STAT))
				&& (event.getFace() != BlockFace.DOWN)) {

			if (!enableStoneGenerator) {
				if(event.getFace() == BlockFace.DOWN) {
					return false;
				}
			}

			Block b = event.getToBlock();

			Location fromLoc = b.getLocation();
			// fix for (lava <-> water)
			if ((type.equals(Type.LAVA) || type.equals(Type.LAVA_STAT))) {
				if (!isSurroundedByWater(fromLoc)) {
					return false;
				}
			}

			if ((b.getType() == Material.AIR || this.getType(b) == Type.WATER || this.getType(b) == Type.WATER_STAT)
					&& (generatesCobble(type, b))) {
				return true;
			}
		}
		return false;
	}
}