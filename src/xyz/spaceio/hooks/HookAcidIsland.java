package xyz.spaceio.hooks;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;

import com.wasteofplastic.acidisland.ASkyBlockAPI;

public class HookAcidIsland implements SkyblockAPIHook {

	private ASkyBlockAPI api;

	public HookAcidIsland() {
		api = ASkyBlockAPI.getInstance();
	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		return api.getIslandLevel(uuid);
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Optional<UUID> optional = Optional.empty();
		if(api.getIslandAt(loc) != null) {
			optional = Optional.of(api.getIslandAt(loc).getOwner());
		}
		return optional;
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return new String[] { api.getIslandWorld().getName() };
	}
}
