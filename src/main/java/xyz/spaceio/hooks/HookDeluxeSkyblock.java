package xyz.spaceio.hooks;

import me.sedattr.skyblockapi.cache.IslandCache;
import me.sedattr.skyblockapi.model.IIsland;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Optional;
import java.util.UUID;

public class HookDeluxeSkyblock implements SkyblockAPIHook {

	public HookDeluxeSkyblock() {
	}

	@Override
	public int getIslandLevel(UUID uuid, String onWorld) {
		IIsland island = IslandCache.getIsland(uuid);
		if (island != null) {
			return island.getLevel();
		}
		return 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		IIsland island = IslandCache.getIslandInLocation(loc);
		if (island != null) {
			return Optional.of(island.getOwnerUUID());
		}
		return Optional.empty();
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return IslandCache.getLoadedIslands().values().stream()
			.flatMap(island -> island.getLocations().values().stream())
			.map(loc -> loc.getWorld().getName())
			.distinct()
			.toArray(String[]::new);
	}

	@Override
	public void sendBlockAcknowledge(Block block) {
		// Not implemented for DeluxeSkyblock
	}

}
