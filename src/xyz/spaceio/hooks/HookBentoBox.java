package xyz.spaceio.hooks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import bentobox.addon.level.Level;
import world.bentobox.bentobox.BentoBox;

public class HookBentoBox implements SkyblockAPIHook{
	
	private BentoBox api;
	
	public HookBentoBox() {
		api = BentoBox.getInstance();
	}

	@Override
	public int getIslandLevel(UUID uuid, String onWorld) {
		int level[] = {0};
		api.getAddonsManager().getAddonByName("Level").ifPresent(addon -> {
		    Level levelAddon = (Level) addon;
		    level[0] = Math.toIntExact(levelAddon.getIslandLevel(Bukkit.getWorld(onWorld), uuid));
		});
		return level[0];
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
