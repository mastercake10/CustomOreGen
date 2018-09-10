package xyz.spaceio.hooks;

import java.util.UUID;

import org.bukkit.Location;

import com.wasteofplastic.askyblock.ASkyBlockAPI;

public class AcidIslandHook implements SkyblockAPIHook {

	private ASkyBlockAPI api;

	public AcidIslandHook() {
		api = ASkyBlockAPI.getInstance();
	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		return api.getIslandLevel(uuid);
	}

	@Override
	public UUID getIslandOwner(Location loc) {
		return api.getIslandAt(loc).getOwner();
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return new String[] { api.getIslandWorld().getName() };
	}
}
