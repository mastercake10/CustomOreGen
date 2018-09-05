package xyz.spaceio.hooks;

import java.util.UUID;

import org.bukkit.Location;

import com.wasteofplastic.acidisland.ASkyBlockAPI;

public class ASkyBlockHook implements SkyblockAPIHook{
	
	private ASkyBlockAPI api;
	
	public ASkyBlockHook() {
		api = ASkyBlockAPI.getInstance();
	}

	@Override
	public int getIslandLevel(UUID uuid) {
		return api.getIslandLevel(uuid);
	}

	@Override
	public UUID getIslandOwner(Location loc) {
		return api.getIslandAt(loc).getOwner();
	}

	@Override
	public String getSkyBlockWorldName() {
		return api.getIslandWorld().getName();
	}
	
}
