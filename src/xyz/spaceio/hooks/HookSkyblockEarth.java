package xyz.spaceio.hooks;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import me.goodandevil.skyblock.config.FileManager.Config;
import me.goodandevil.skyblock.island.Island;
import me.goodandevil.skyblock.island.IslandEnvironment;
import me.goodandevil.skyblock.island.IslandManager;
import me.goodandevil.skyblock.island.IslandWorld;
import me.goodandevil.skyblock.utils.world.LocationUtil;

public class HookSkyblockEarth implements SkyblockAPIHook {

	private me.goodandevil.skyblock.SkyBlock mainClass;
	private IslandManager api;

	public HookSkyblockEarth() {
		mainClass = (me.goodandevil.skyblock.SkyBlock) Bukkit.getPluginManager().getPlugin("SkyBlock");
		api = mainClass.getIslandManager();
		
		// disabling the default generator of this skyblock plugin
		try {
			Field f1 = mainClass.getClass().getDeclaredField("generatorManager");
			f1.setAccessible(true);
			f1.set(mainClass, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		if (api.getIsland(uuid) == null)
			return 0;
		return api.getIsland(uuid).getLevel().getLevel();
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Optional<UUID> optional = api.getIslands().values().stream().filter(i -> isIslandAtLocation(i, loc)).findFirst().map(i -> i.getOwnerUUID());
		
		return optional;
	}
	
	private boolean isIslandAtLocation(Island island, Location loc) {
		for(IslandWorld iw : IslandWorld.values()) {
			if (LocationUtil.isLocationAtLocationRadius(loc, island.getLocation(iw, IslandEnvironment.Island), island.getRadius())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		// took from source code of SkyBlock.jar
		Config var1 = mainClass.getFileManager().getConfig(new File(mainClass.getDataFolder(), "config.yml"));
		FileConfiguration var2 = var1.getFileConfiguration();
		String var3 = var2.getString("Island.World.Nether.Name");
		String var4 = var2.getString("Island.World.Normal.Name");
		String var5 = var2.getString("Island.World.End.Name");
		return new String[] {var3, var4, var5};
	}

}
