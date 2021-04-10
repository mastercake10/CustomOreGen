package xyz.spaceio.hooks;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;


public class HookSuperiorSkyblock implements SkyblockAPIHook {


	public HookSuperiorSkyblock() {

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
		
		List<World> worlds = SuperiorSkyblockAPI.getGrid().getRegisteredWorlds();
		worlds.add(SuperiorSkyblockAPI.getSpawnIsland().getVisitorsLocation().getWorld());
		
		return worlds.stream().map(w -> w.getName()).toArray(String[]::new);
	}
	
	@Override
	public void sendBlockAcknowledge(Block block) {
		if(SuperiorSkyblockAPI.getIslandAt(block.getLocation()) != null) {
			SuperiorSkyblockAPI.getIslandAt(block.getLocation()).handleBlockPlace(block);	
		}
	}

}
