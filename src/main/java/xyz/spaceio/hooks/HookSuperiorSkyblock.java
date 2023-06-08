package xyz.spaceio.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;


public class HookSuperiorSkyblock implements SkyblockAPIHook {


	public HookSuperiorSkyblock(Plugin plugin) {
		for(RegisteredListener listener : HandlerList.getRegisteredListeners(Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2"))) {
			try {
				if(listener.getListener().getClass() == Class.forName("com.bgsoftware.superiorskyblock.module.generators.listeners.GeneratorsListener")){
					HandlerList.unregisterAll(listener.getListener());
					plugin.getLogger().info(String.format("%s: Unregistered inbuilt-generator from Plugin (%s) to use this one", this.getClass().getSimpleName(), listener.getListener().getClass().getName()));
				}
			} catch (ClassNotFoundException e) {
				plugin.getLogger().warning(String.format("%s: Generator class for inbuilt-generator not found (%s)", this.getClass().getSimpleName(), listener.getListener().getClass().getName()));
			}
		}
	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		if(SuperiorSkyblockAPI.getPlayer(uuid) == null || SuperiorSkyblockAPI.getPlayer(uuid).getIsland() == null) {
			return 0;
		}else {
			return 	SuperiorSkyblockAPI.getPlayer(uuid).getIsland().getIslandLevel().intValue();
		}
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		if(SuperiorSkyblockAPI.getIslandAt(loc) != null && SuperiorSkyblockAPI.getIslandAt(loc).getOwner() != null) {
			return Optional.of(SuperiorSkyblockAPI.getIslandAt(loc).getOwner().getUniqueId());
		}else {
			return Optional.empty();
		}
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		List<World> worlds = new ArrayList<>();

		if(SuperiorSkyblockAPI.getGrid() != null && SuperiorSkyblockAPI.getGrid().getRegisteredWorlds() != null) {
			worlds.addAll(SuperiorSkyblockAPI.getGrid().getRegisteredWorlds());
		}

		if(SuperiorSkyblockAPI.getSpawnIsland() != null && SuperiorSkyblockAPI.getSpawnIsland().getVisitorsLocation() != null) {
			worlds.add(SuperiorSkyblockAPI.getSpawnIsland().getVisitorsLocation().getWorld());
		}

		return worlds.stream().map(w -> w.getName()).toArray(String[]::new);
	}
	
	@Override
	public void sendBlockAcknowledge(Block block) {
		if(SuperiorSkyblockAPI.getIslandAt(block.getLocation()) != null) {
			SuperiorSkyblockAPI.getIslandAt(block.getLocation()).handleBlockPlace(block);
		}
	}

}
