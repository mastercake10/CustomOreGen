package xyz.spaceio.hooks;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import xyz.spaceio.skyblock.SpaceSkyblock;
import xyz.spaceio.skyblock.islands.Island;

public class HookSpaceSkyblock implements SkyblockAPIHook{
	
	private SpaceSkyblock api;
	
	public HookSpaceSkyblock() {
		api = (SpaceSkyblock) Bukkit.getPluginManager().getPlugin("SpaceSkyblock");
	}

	@Override
	public int getIslandLevel(UUID uuid, String onWorld) {
		Optional<Island> island = api.getIslandManager().getIslandOf(uuid, onWorld);
		if(island.isPresent()) {
			return island.get().getLevel();
		}
		return 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Optional<Island> optIsland = api.getIslandManager().getIslandByLocation(loc);
		Optional<UUID> optional = Optional.empty();
		
		if(optIsland.isPresent() && optIsland.get().getOwner() != null) {
			optional = Optional.of(optIsland.get().getOwner());
		}
		return optional;
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return api.getIslandManager().getSkyWorlds().stream().toArray(String[]::new);
	}
	
	@Override
	public void sendBlockAcknowledge(Block block) {
		// TODO Auto-generated method stub
		
	}
	
}
