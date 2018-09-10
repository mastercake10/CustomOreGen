package xyz.spaceio.hooks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import world.bentobox.bentobox.BentoBox;

public class HookBentoBox implements SkyblockAPIHook{
	
	private BentoBox api;
	
	public HookBentoBox() {
		api = BentoBox.getInstance();
	}

	@Override
	public int getIslandLevel(UUID uuid, String onWorld) {
		if(api.getIslands().getIsland(Bukkit.getWorld(onWorld), uuid) != null) {
			return api.getIslands().getIsland(Bukkit.getWorld(onWorld), uuid).getLevelHandicap();	
		}
		return 0;
	}

	@Override
	public UUID getIslandOwner(Location loc) {
		return api.getIslands().getIslandAt(loc).get().getOwner();
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return api.getIWM().getOverWorldNames().stream().toArray(String[]::new);
	}
	
}
