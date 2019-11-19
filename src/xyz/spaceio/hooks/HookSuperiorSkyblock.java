package xyz.spaceio.hooks;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;


public class HookSuperiorSkyblock implements SkyblockAPIHook {


	public HookSuperiorSkyblock() {

	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		if(SuperiorSkyblockAPI.getPlayer(uuid) == null || SuperiorSkyblockAPI.getPlayer(uuid).getIsland() == null) {
			return 0;
		}else {
			return 	SuperiorSkyblockAPI.getPlayer(uuid).getIsland().getIslandLevelAsBigDecimal().intValue();
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
		return new String[] {SuperiorSkyblockAPI.getIslandsWorld().getName()};
	}

}
