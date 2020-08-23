package xyz.spaceio.hooks;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import pl.islandworld.IslandWorld;
import pl.islandworld.entity.SimpleIsland;

public class HookIslandWorld implements SkyblockAPIHook {

	private IslandWorld api;

	public HookIslandWorld() {
		api = (IslandWorld) Bukkit.getPluginManager().getPlugin("IslandWorld");
	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		SimpleIsland is = api.getUUIDList().getOrDefault(uuid, null);
		return is != null ? is.getLevel() : 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Optional<UUID> optional = Optional.empty();
		
		SimpleIsland is = api.getCoordList().getOrDefault(api.hashMeFromLoc(loc), null);
		if(is != null) {
			optional =  Optional.of(is.getOwnerUUID());
		}
		
		return optional;
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return new String[] { api.getIslandWorld().getName() };
	}
	
	@Override
	public void sendBlockAcknowledge(Block block) {
		// TODO Auto-generated method stub
		
	}

}
